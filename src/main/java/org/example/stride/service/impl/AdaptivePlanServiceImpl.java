package org.example.stride.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.stride.model.entity.Goal;
import org.example.stride.model.entity.Run;
import org.example.stride.model.entity.RunnerProfile;
import org.example.stride.model.entity.TrainingPlan;
import org.example.stride.model.entity.TrainingWeek;
import org.example.stride.model.entity.Workout;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.model.enums.Focus;
import org.example.stride.model.enums.Status;
import org.example.stride.model.enums.Type;
import org.example.stride.repository.RunRepository;
import org.example.stride.repository.TrainingWeekRepository;
import org.example.stride.repository.WorkoutRepository;
import org.example.stride.service.AdaptivePlanService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdaptivePlanServiceImpl implements AdaptivePlanService {

    private static final int WINDOW_WEEKS = 2;
    private static final int RECOVERY_WEEK_INTERVAL = 4;
    private static final int BASE_WEEKS = 3;

    private static final double LOADING_PROGRESSION = 1.05;
    private static final double RECOVERY_FACTOR = 0.75;
    private static final double TAPER_NEAR_FACTOR = 0.70;
    private static final double TAPER_FAR_FACTOR = 0.85;
    private static final double RACE_WEEK_VOLUME_SHARE = 0.25;

    private static final double QUALITY_VOLUME_SHARE = 0.20;
    private static final double MIN_VOLUME_FOR_QUALITY = 15.0;
    private static final double TEMPO_HARD_SHARE = 0.50;
    private static final double MIN_TEMPO_KM = 3.0;
    private static final double INTERVAL_HARD_SHARE = 0.40;
    private static final double INTERVAL_REP_KM = 0.8;
    private static final double INTERVAL_JOG_KM = 0.4;
    private static final double MIN_WARM_COOL_KM = 1.5;
    private static final int MIN_INTERVAL_REPS = 3;
    private static final int MAX_INTERVAL_REPS = 8;

    private static final double MIN_RUN_KM = 2.0;
    private static final double MAX_LONG_RUN_KM = 32.0;
    private static final double MAX_LONG_RUN_SECONDS = 9000.0;

    private static final double RECOVERY_PACE = 1.12;
    private static final double EASY_PACE = 1.03;
    private static final double LONG_PACE = 1.08;
    private static final double TEMPO_PACE = 0.92;
    private static final double INTERVAL_PACE = 0.85;

    private static final int FATIGUE_LOOKBACK_DAYS = 14;
    private static final int MIN_RUNS_FOR_FATIGUE = 3;
    private static final double HIGH_EFFORT = 8.0;
    private static final double ELEVATED_EFFORT = 7.0;
    private static final double HIGH_FATIGUE_FACTOR = 0.85;
    private static final double ELEVATED_FATIGUE_FACTOR = 0.92;

    private record QualitySession(Type type, double distanceKm, double hardKm, int reps, double hardPace) {
    }

    private final TrainingWeekRepository trainingWeekRepository;
    private final WorkoutRepository workoutRepository;
    private final RunRepository runRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public void generateOrUpdateNextWindow(TrainingPlan plan) {
        if (plan == null || plan.getGoal() == null) {
            throw new IllegalArgumentException("Training plan and goal are non-negotiable");
        }

        RunnerProfile profile = currentUserService.getCurrentUser().getRunnerProfile();

        if (profile == null) {
            throw new IllegalStateException("Create a runner profile before generating a training plan.");
        }

        if (!Objects.equals(plan.getGoal().getRunnerProfile().getId(), profile.getId())) {
            throw new IllegalArgumentException("You can only generate a plan for your own profile.");
        }

        if (plan.getWeeks() < 1 || plan.getStartDate() == null) {
            throw new IllegalStateException("The training plan must have a start date and a positive number of weeks.");
        }

        if (profile.getAverageWeeklyDistance() <= 0 || profile.getAveragePaceSeconds() <= 0) {
            throw new IllegalStateException("Add a positive average weekly distance and average pace to your profile.");
        }

        if (profile.getPreferredRunningDays() == null || profile.getPreferredRunningDays().isEmpty()) {
            throw new IllegalStateException("Select at least one preferred running day.");
        }

        LocalDate today = LocalDate.now();
        int currentWeekNumber = determineCurrentWeek(plan, today);
        double fatigueFactor = calculateFatigueFactor(profile, today);

        for (int offset = 0; offset < WINDOW_WEEKS; offset++) {
            int weekNumber = currentWeekNumber + offset;

            if (weekNumber > plan.getWeeks()) {
                break;
            }

            if (trainingWeekRepository.findByTrainingPlanAndWeekNumber(plan, weekNumber).isPresent()) {
                continue;
            }

            TrainingWeek week = trainingWeekRepository.save(createWeek(plan, weekNumber));
            double volume = calculateTargetVolume(plan, profile, week, fatigueFactor);

            workoutRepository.saveAll(buildWorkouts(week, profile, volume, today));
        }
    }

    private TrainingWeek createWeek(TrainingPlan plan, int weekNumber) {
        LocalDate startDate = firstWeekStart(plan).plusWeeks(weekNumber - 1L);
        LocalDate endDate = startDate.plusDays(6);

        TrainingWeek week = new TrainingWeek();
        week.setTrainingPlan(plan);
        week.setWeekNumber(weekNumber);
        week.setStartDate(startDate);
        week.setEndDate(endDate);
        week.setFocus(determineFocus(plan.getGoal(), weekNumber, startDate));

        return week;
    }

    private Focus determineFocus(Goal goal, int weekNumber, LocalDate weekStart) {
        long weeksToRace = weeksUntilRaceWeek(goal, weekStart);

        if (weeksToRace == 0) {
            return Focus.RACE;
        }

        if (weeksToRace > 0 && weeksToRace <= taperWeeksBeforeRaceWeek(goal.getTargetDistance())) {
            return Focus.TAPER;
        }

        if (weekNumber % RECOVERY_WEEK_INTERVAL == 0) {
            return Focus.RECOVERY;
        }

        return weekNumber <= BASE_WEEKS ? Focus.BASE : Focus.BUILD;
    }

    private long weeksUntilRaceWeek(Goal goal, LocalDate weekStart) {
        LocalDate raceDate = goal.getTargetDate();

        if (raceDate == null) {
            return -1;
        }

        LocalDate raceWeekStart = raceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return ChronoUnit.WEEKS.between(weekStart, raceWeekStart);
    }

    private int taperWeeksBeforeRaceWeek(double distanceKm) {
        if (distanceKm <= 10.5) {
            return 0;
        }

        if (distanceKm <= 22.0) {
            return 1;
        }

        return 2;
    }

    private double calculateTargetVolume(
            TrainingPlan plan,
            RunnerProfile profile,
            TrainingWeek week,
            double fatigueFactor
    ) {
        double baseline = profile.getAverageWeeklyDistance();
        OptionalDouble reference = lastLoadingVolume(plan, week.getWeekNumber());
        double referenceVolume = reference.orElse(baseline);

        double volume = switch (week.getFocus()) {
            case RECOVERY -> referenceVolume * RECOVERY_FACTOR;
            case TAPER -> referenceVolume * taperFactor(weeksUntilRaceWeek(plan.getGoal(), week.getStartDate()));
            case RACE -> referenceVolume;
            default -> loadingVolume(plan.getGoal(), baseline, reference, fatigueFactor);
        };

        return roundToOneDecimal(volume);
    }

    private double loadingVolume(
            Goal goal,
            double baseline,
            OptionalDouble reference,
            double fatigueFactor
    ) {
        double peak = Math.max(peakWeeklyVolume(goal.getTargetDistance()), baseline);

        double progressed = reference.isPresent()
                ? Math.min(reference.getAsDouble() * LOADING_PROGRESSION, peak)
                : baseline;

        return progressed * fatigueFactor;
    }

    private double taperFactor(long weeksToRace) {
        return weeksToRace >= 2 ? TAPER_FAR_FACTOR : TAPER_NEAR_FACTOR;
    }

    private double peakWeeklyVolume(double targetDistanceKm) {
        if (targetDistanceKm <= 5.5) {
            return 40.0;
        }

        if (targetDistanceKm <= 10.5) {
            return 50.0;
        }

        if (targetDistanceKm <= 22.0) {
            return 65.0;
        }

        if (targetDistanceKm <= 43.0) {
            return 80.0;
        }

        return 90.0;
    }

    private OptionalDouble lastLoadingVolume(TrainingPlan plan, int weekNumber) {
        for (int number = weekNumber - 1; number >= 1; number--) {
            Optional<TrainingWeek> previous =
                    trainingWeekRepository.findByTrainingPlanAndWeekNumber(plan, number);

            if (previous.isEmpty()) {
                return OptionalDouble.empty();
            }

            Focus focus = previous.get().getFocus();

            if (focus == Focus.BASE || focus == Focus.BUILD) {
                double volume = weeklyVolume(previous.get());

                if (volume > 0) {
                    return OptionalDouble.of(volume);
                }
            }
        }

        return OptionalDouble.empty();
    }

    private double weeklyVolume(TrainingWeek week) {
        return workoutRepository.findAllByTrainingWeekOrderByDateAsc(week).stream()
                .filter(workout -> workout.getType() != Type.RACE)
                .mapToDouble(Workout::getDistance)
                .sum();
    }

    private List<Workout> buildWorkouts(
            TrainingWeek week,
            RunnerProfile profile,
            double weeklyVolume,
            LocalDate today
    ) {
        LocalDate raceDate = week.getTrainingPlan().getGoal().getTargetDate();
        boolean raceWeek = week.getFocus() == Focus.RACE;

        List<LocalDate> dates = getWorkoutDates(week, profile.getPreferredRunningDays());

        if (raceWeek) {
            dates.removeIf(date -> !date.isBefore(raceDate));
            dates.add(raceDate);
        }

        int plannedRuns = dates.size();
        dates.removeIf(date -> date.isBefore(today));

        if (dates.isEmpty()) {
            return new ArrayList<>();
        }

        double volume = weeklyVolume * dates.size() / plannedRuns;

        return raceWeek
                ? buildRaceWeek(week, profile, dates, volume)
                : buildRegularWeek(week, profile, dates, volume);
    }

    private List<Workout> buildRaceWeek(
            TrainingWeek week,
            RunnerProfile profile,
            List<LocalDate> dates,
            double volume
    ) {
        Goal goal = week.getTrainingPlan().getGoal();
        LocalDate raceDate = goal.getTargetDate();

        long shortRuns = dates.stream().filter(date -> !date.equals(raceDate)).count();
        double shortRunDistance = shortRuns > 0
                ? Math.max(MIN_RUN_KM, roundToOneDecimal(volume * RACE_WEEK_VOLUME_SHARE / shortRuns))
                : 0;
        double easyPace = profile.getAveragePaceSeconds() * EASY_PACE;

        List<Workout> workouts = new ArrayList<>();

        for (LocalDate date : dates) {
            if (date.equals(raceDate)) {
                workouts.add(createRaceWorkout(week, goal, profile, date));
                continue;
            }

            workouts.add(createWorkout(
                    week,
                    date,
                    Type.RECOVERY,
                    shortRunDistance,
                    toDuration(shortRunDistance, easyPace),
                    2,
                    String.format(
                            Locale.ROOT,
                            "Short easy run during race week: %.1f km at around %s/km. Save your energy.",
                            shortRunDistance,
                            formatPace(easyPace)
                    )
            ));
        }

        return workouts;
    }

    private Workout createRaceWorkout(
            TrainingWeek week,
            Goal goal,
            RunnerProfile profile,
            LocalDate date
    ) {
        double distance = goal.getTargetDistance();
        boolean hasTargetTime = goal.getTargetTimeSeconds() > 0;

        int duration = hasTargetTime
                ? goal.getTargetTimeSeconds()
                : toDuration(distance, profile.getAveragePaceSeconds());

        String description = hasTargetTime
                ? String.format(
                Locale.ROOT,
                "Race Day: Start evenly at target pace %s/km and avoid burning out in the first few kilometers.",
                formatPace((double) goal.getTargetTimeSeconds() / distance)
        )
                : "Race Day: Start at a comfortable pace and accelerate only if you feel strong.";

        return createWorkout(week, date, Type.RACE, distance, duration, 8, description);
    }

    private List<Workout> buildRegularWeek(
            TrainingWeek week,
            RunnerProfile profile,
            List<LocalDate> dates,
            double volume
    ) {
        int runs = dates.size();
        boolean recoveryWeek = week.getFocus() == Focus.RECOVERY;
        double averagePace = profile.getAveragePaceSeconds();
        double easyPace = averagePace * EASY_PACE;
        double longPace = averagePace * LONG_PACE;

        boolean withQuality = isQualityAllowed(profile, dates, volume, recoveryWeek);
        QualitySession session = withQuality
                ? planQualitySession(volume, week.getWeekNumber(), averagePace)
                : null;

        double longDistance = roundToOneDecimal(
                Math.min(volume * longRunShare(runs), maxLongRunKm(longPace))
        );
        double qualityDistance = session == null ? 0 : session.distanceKm();
        int easyRuns = runs - 1 - (withQuality ? 1 : 0);
        double easyDistance = easyRuns > 0
                ? Math.max(MIN_RUN_KM, roundToOneDecimal((volume - longDistance - qualityDistance) / easyRuns))
                : 0;

        List<Workout> workouts = new ArrayList<>();

        for (int index = 0; index < runs; index++) {
            LocalDate date = dates.get(index);

            if (index == runs - 1) {
                workouts.add(createWorkout(
                        week,
                        date,
                        Type.LONG_RUN,
                        longDistance,
                        toDuration(longDistance, longPace),
                        4,
                        String.format(
                                Locale.ROOT,
                                "Long run for aerobic endurance: %.1f km at an easy pace around %s/km.",
                                longDistance,
                                formatPace(longPace)
                        )
                ));
            } else if (session != null && index == 1) {
                workouts.add(createQualityWorkout(week, date, session, easyPace));
            } else if (recoveryWeek && index == 0) {
                double recoveryPace = averagePace * RECOVERY_PACE;

                workouts.add(createWorkout(
                        week,
                        date,
                        Type.RECOVERY,
                        easyDistance,
                        toDuration(easyDistance, recoveryPace),
                        2,
                        String.format(
                                Locale.ROOT,
                                "Very easy recovery run: %.1f km at around %s/km.",
                                easyDistance,
                                formatPace(recoveryPace)
                        )
                ));
            } else {
                workouts.add(createWorkout(
                        week,
                        date,
                        Type.EASY,
                        easyDistance,
                        toDuration(easyDistance, easyPace),
                        3,
                        String.format(
                                Locale.ROOT,
                                "Easy run at a conversational pace: %.1f km at around %s/km.",
                                easyDistance,
                                formatPace(easyPace)
                        )
                ));
            }
        }

        return workouts;
    }

    private boolean isQualityAllowed(
            RunnerProfile profile,
            List<LocalDate> dates,
            double volume,
            boolean recoveryWeek
    ) {
        return !recoveryWeek
                && volume >= MIN_VOLUME_FOR_QUALITY
                && dates.size() >= 3
                && profile.getExperienceLevel() != ExperienceLevel.BEGINNER
                && ChronoUnit.DAYS.between(dates.get(1), dates.get(dates.size() - 1)) >= 2;
    }

    private QualitySession planQualitySession(double volume, int weekNumber, double averagePace) {
        double target = volume * QUALITY_VOLUME_SHARE;

        if (weekNumber % 2 == 0) {
            int reps = Math.max(
                    MIN_INTERVAL_REPS,
                    Math.min(
                            MAX_INTERVAL_REPS,
                            (int) Math.round(target * INTERVAL_HARD_SHARE / INTERVAL_REP_KM)
                    )
            );
            double distance = Math.max(
                    target,
                    reps * (INTERVAL_REP_KM + INTERVAL_JOG_KM) + MIN_WARM_COOL_KM
            );

            return new QualitySession(
                    Type.INTERVAL,
                    roundToOneDecimal(distance),
                    reps * INTERVAL_REP_KM,
                    reps,
                    averagePace * INTERVAL_PACE
            );
        }

        double distance = roundToOneDecimal(Math.max(target, MIN_TEMPO_KM));

        return new QualitySession(
                Type.TEMPO,
                distance,
                roundToOneDecimal(distance * TEMPO_HARD_SHARE),
                0,
                averagePace * TEMPO_PACE
        );
    }

    private Workout createQualityWorkout(
            TrainingWeek week,
            LocalDate date,
            QualitySession session,
            double easyPace
    ) {
        double easyKm = session.distanceKm() - session.hardKm();
        int duration = (int) Math.round(
                session.hardKm() * session.hardPace() + easyKm * easyPace
        );
        int intensity = session.type() == Type.INTERVAL ? 7 : 6;

        return createWorkout(
                week,
                date,
                session.type(),
                session.distanceKm(),
                duration,
                intensity,
                describeQuality(session)
        );
    }

    private String describeQuality(QualitySession session) {
        if (session.type() == Type.INTERVAL) {
            double warmUpAndCoolDown = Math.max(
                    0,
                    session.distanceKm() - session.reps() * (INTERVAL_REP_KM + INTERVAL_JOG_KM)
            );

            return String.format(
                    Locale.ROOT,
                    "Intervals: %d × 800 m at %s/km pace with 400 m slow recovery jog between reps. Warm-up and cool-down: around %.1f km.",
                    session.reps(),
                    formatPace(session.hardPace()),
                    warmUpAndCoolDown
            );
        }

        return String.format(
                Locale.ROOT,
                "Tempo run: around %.1f km at %s/km pace (comfortably hard effort). Remaining %.1f km is warm-up and cool-down.",
                session.hardKm(),
                formatPace(session.hardPace()),
                session.distanceKm()
        );
    }

    private double longRunShare(int runs) {
        return switch (runs) {
            case 1 -> 1.0;
            case 2 -> 0.55;
            case 3 -> 0.40;
            case 4 -> 0.35;
            default -> 0.30;
        };
    }

    private double maxLongRunKm(double longPaceSecondsPerKm) {
        return Math.min(MAX_LONG_RUN_KM, MAX_LONG_RUN_SECONDS / longPaceSecondsPerKm);
    }

    private List<LocalDate> getWorkoutDates(TrainingWeek week, Set<Day> preferredDays) {
        List<LocalDate> dates = preferredDays.stream()
                .map(day -> DayOfWeek.valueOf(day.name()))
                .map(day -> week.getStartDate().plusDays(day.getValue() - 1L))
                .filter(date -> !date.isBefore(week.getStartDate())
                        && !date.isAfter(week.getEndDate()))
                .sorted()
                .toList();

        return new ArrayList<>(dates);
    }

    private LocalDate firstWeekStart(TrainingPlan plan) {
        return plan.getStartDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    private int determineCurrentWeek(TrainingPlan plan, LocalDate today) {
        LocalDate firstMonday = firstWeekStart(plan);

        if (today.isBefore(firstMonday)) {
            return 1;
        }

        return (int) ChronoUnit.WEEKS.between(firstMonday, today) + 1;
    }

    private double calculateFatigueFactor(RunnerProfile profile, LocalDate today) {
        List<Run> recentRuns = runRepository.findAllByRunnerProfileAndDateAfter(
                profile,
                today.minusDays(FATIGUE_LOOKBACK_DAYS)
        );

        if (recentRuns.size() < MIN_RUNS_FOR_FATIGUE) {
            return 1.0;
        }

        double averageEffort = recentRuns.stream()
                .mapToInt(Run::getPerceivedEffort)
                .average()
                .orElse(0);

        if (averageEffort >= HIGH_EFFORT) {
            return HIGH_FATIGUE_FACTOR;
        }

        if (averageEffort >= ELEVATED_EFFORT) {
            return ELEVATED_FATIGUE_FACTOR;
        }

        return 1.0;
    }

    private Workout createWorkout(
            TrainingWeek week,
            LocalDate date,
            Type type,
            double distanceKm,
            int durationSeconds,
            int intensity,
            String description
    ) {
        Workout workout = new Workout();
        workout.setTrainingWeek(week);
        workout.setDate(date);
        workout.setStatus(Status.ACTIVE);
        workout.setType(type);
        workout.setDistance(distanceKm);
        workout.setDurationSeconds(durationSeconds);
        workout.setIntensity(intensity);
        workout.setDescription(description);
        return workout;
    }

    private int toDuration(double distanceKm, double paceSecondsPerKm) {
        return (int) Math.round(distanceKm * paceSecondsPerKm);
    }

    private String formatPace(double secondsPerKm) {
        int total = (int) Math.round(secondsPerKm);
        return String.format(Locale.ROOT, "%d:%02d", total / 60, total % 60);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}