package org.example.stride.service.helpClasses;

import org.example.stride.model.enums.Difficulty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Component
public class GoalAssessmentCalculator {

    private static final double MAX_IMPROVEMENT_PER_WEEK = 0.8;  // % подобрение на седмица
    private static final double MAX_WEEKLY_GROWTH = 0.06;        // +6% обем на седмица
    private static final double RECOVERY_FACTOR = 1.33;          // 1 възстановителна седмица на 4
    private static final double MAX_LONG_RUN_KM = 32;
    private static final double MAX_WEEKLY_KM = 80;
    private static final int MIN_WEEKS = 4;

    public AssessmentResult predict(double longestRunKm, double paceSecPerKm, double weeklyKm,
                                    double targetKm, int targetSeconds,
                                    LocalDate targetDate){
        double predicted = paceSecPerKm * longestRunKm * Math.pow(targetKm / longestRunKm, exponentFor(targetKm));
        double improvement = Math.max(0, (predicted - targetSeconds) / predicted * 100);

        int weeksNeeded = weeksNeeded(longestRunKm, weeklyKm, targetKm, improvement);
        LocalDate projectedDate = LocalDate.now().plusWeeks(weeksNeeded);

        long weeksAvailable = targetDate == null ? 0 : ChronoUnit.WEEKS.between(LocalDate.now(), targetDate);

        Difficulty difficulty = targetDate == null
                ? difficultyWithoutDeadline(weeksNeeded)
                : difficultyWithDeadline(weeksNeeded, weeksAvailable);

        String explanation = explain(predicted, improvement, weeksNeeded,
                projectedDate, targetDate, weeksAvailable);

        return new AssessmentResult((int) Math.round(predicted), improvement,
                weeksNeeded, projectedDate, difficulty, explanation);
    }

    private Difficulty difficultyWithDeadline(int weeksNeeded, long weeksAvailable) {
        if (weeksAvailable < 1) return Difficulty.UNREALISTIC;

        double ratio = (double) weeksNeeded / weeksAvailable;
        if (ratio <= 0.7) return Difficulty.REALISTIC;
        if (ratio <= 1.0) return Difficulty.CHALLENGING;
        if (ratio <= 1.3) return Difficulty.VERY_AMBITIOUS;
        return Difficulty.UNREALISTIC;
    }

    private Difficulty difficultyWithoutDeadline(int weeksNeeded) {
        if (weeksNeeded <= 16) return Difficulty.REALISTIC;
        if (weeksNeeded <= 30) return Difficulty.CHALLENGING;
        if (weeksNeeded <= 52) return Difficulty.VERY_AMBITIOUS;

        return Difficulty.UNREALISTIC;
    }

    private int weeksNeeded(double longestRunKm, double weeklyKm, double targetKm, double improvement) {
        double speedWeeks = improvement / MAX_IMPROVEMENT_PER_WEEK;

        double longRunWeeks = growthWeeks(longestRunKm, Math.min(targetKm, MAX_LONG_RUN_KM));
        double volumeWeeks  = growthWeeks(Math.max(weeklyKm, 1), Math.min(targetKm * 1.5, MAX_WEEKLY_KM));
        double distanceWeeks = Math.max(longRunWeeks, volumeWeeks) * RECOVERY_FACTOR;

        return Math.max(MIN_WEEKS, (int) Math.ceil(Math.max(speedWeeks, distanceWeeks)));
    }

    private double growthWeeks(double current, double required) {
        if (current >= required) return 0;
        return Math.log(required / current) / Math.log(1 + MAX_WEEKLY_GROWTH);
    }

    private double exponentFor(double targetKm) {
        return targetKm > 42 ? 1.15 : 1.06;
    }

    private String explain(double predicted, double improvement, int weeksNeeded,
                           LocalDate projectedDate, LocalDate targetDate, long weeksAvailable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Според текущата ти форма очакваното ти време е ")
                .append(formatTime((int) Math.round(predicted))).append(". ");

        if (improvement > 0) {
            sb.append(String.format(Locale.ROOT, "Нужно е подобрение от %.1f%%. ", improvement));
        }

        sb.append("Реалистичната подготовка отнема около ").append(weeksNeeded).append(" седмици");

        if (targetDate == null) {
            sb.append(", т.е. предвидена дата: ").append(projectedDate).append(".");
        } else {
            sb.append(", а до крайната дата имаш ").append(Math.max(weeksAvailable, 0)).append(".");
        }
        return sb.toString();
    }

    private String formatTime(int totalSeconds) {
        return String.format("%d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }
}
