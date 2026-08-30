# 🏃 STRIDE

### Your goal. Your pace. Your progress.

**STRIDE** is a personal running coach that turns a running goal into a realistic, structured training journey.

Instead of simply tracking completed runs, STRIDE starts with a question:

> **"What do I want to achieve, and is it realistically possible?"**

The application analyzes a runner's current performance, target distance, target time and available preparation period. It then evaluates the goal, estimates the runner's expected performance and generates a personalized training plan.

As the runner completes workouts, STRIDE tracks the actual progress and compares it with the original plan.

---

## 🎯 Core Idea

A running goal is more than a distance and a time.

For example:

> **Run 10 km in under 55 minutes in 12 weeks.**

STRIDE analyzes:

* Current running performance
* Recent race results
* Longest recent run
* Weekly running volume
* Training frequency
* Target distance
* Target time
* Time available until the target date

Then it produces a **Goal Assessment**.

```text
┌─────────────────────────────────────┐
│          🎯 GOAL ASSESSMENT         │
├─────────────────────────────────────┤
│                                     │
│  Target:       10 km                │
│  Time:         55:00                │
│  Preparation:  12 weeks             │
│                                     │
│  Predicted:    1:01:20              │
│  Improvement:  10.3%                │
│                                     │
│  Difficulty:   CHALLENGING          │
│                                     │
│  Result:       🟢 ACHIEVABLE        │
│                                     │
└─────────────────────────────────────┘
```

If the goal is unrealistic, STRIDE should not simply reject it.

It should explain **why** and, when possible, suggest a more realistic alternative.

---

# ✨ Features

## 👤 Runner Profile

Users can create a running profile containing:

* Experience level
* Runs per week
* Average weekly distance
* Longest recent run
* Preferred running days
* Personal bests

---

## 🎯 Goals

Users can create running goals based on:

* Target distance
* Target time
* Target date

Goals can have the following statuses:

* `ACTIVE`
* `COMPLETED`
* `FAILED`
* `CANCELLED`

---

## 🧮 Goal Assessment

STRIDE evaluates whether a goal is realistic based on the runner's current performance.

The assessment includes:

* Predicted finish time
* Required improvement
* Difficulty
* Time available
* Overall assessment
* Explanation

Possible results:

* 🟢 `REALISTIC`
* 🟡 `CHALLENGING`
* 🟠 `VERY_AMBITIOUS`
* 🔴 `UNREALISTIC`

---

## 📈 Performance Prediction

STRIDE can estimate a runner's performance over a different distance based on previous results.

The initial version uses the **Riegel race prediction formula**:

```text
T₂ = T₁ × (D₂ / D₁)¹·⁰⁶
```

For example:

```text
Current result:
5 km — 30:00

Predicted:
10 km — ~61:00
```

The prediction is used as one of several inputs in the goal assessment.

---

## 🗓️ Training Plan Generator

Once a goal has been assessed, STRIDE generates a structured training plan.

A plan consists of:

```text
Training Plan
    │
    ├── Week 1
    │     ├── Easy Run
    │     ├── Tempo Run
    │     └── Long Run
    │
    ├── Week 2
    │     ├── Easy Run
    │     ├── Intervals
    │     └── Long Run
    │
    └── ...
```

Workout types may include:

* Easy Run
* Long Run
* Tempo Run
* Interval Training
* Recovery Run
* Race

---

## 🏃 Run Tracking

After completing a workout, the runner can record:

* Date
* Distance
* Duration
* Average pace
* Average heart rate
* Elevation gain
* Run type
* Perceived effort
* Notes

The completed run can then be compared with the planned workout.

---

## 📊 Progress

STRIDE provides basic statistics such as:

* Total distance
* Number of runs
* Average pace
* Longest run
* Personal bests
* Weekly mileage
* Goal progress
* Planned vs. completed workouts

---

# 🏗️ Architecture

The application follows a layered architecture:

```text
                ┌───────────────┐
                │   Controller  │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │    Service    │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │  Repository   │
                └───────┬───────┘
                        │
                        ▼
                    Database
```

The main business logic is separated from the web and persistence layers.

---

# 🗂️ Domain Model

```text
User
 │
 └── RunnerProfile
       │
       ├── Run
       │
       ├── PersonalBest
       │
       └── Goal
             │
             ├── GoalAssessment
             │
             └── TrainingPlan
                    │
                    └── TrainingWeek
                           │
                           └── Workout
```

### Main entities

* `User`
* `RunnerProfile`
* `Run`
* `PersonalBest`
* `Goal`
* `GoalAssessment`
* `TrainingPlan`
* `TrainingWeek`
* `Workout`

---

# 🛠️ Tech Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven

### Database

* MySQL

### Frontend

* Thymeleaf
* HTML
* CSS
* JavaScript

### Testing

* JUnit
* Mockito

---

# 🔌 Example API

### Goals

```http
GET    /api/goals
POST   /api/goals
GET    /api/goals/{id}
DELETE /api/goals/{id}
```

### Runs

```http
GET    /api/runs
POST   /api/runs
GET    /api/runs/{id}
DELETE /api/runs/{id}
```

### Training

```http
GET   /api/training-plans/{id}
GET   /api/training-plans/{id}/weeks
GET   /api/workouts/{id}
PATCH /api/workouts/{id}/complete
```

---

# 🖥️ Main Pages

```text
Dashboard
│
├── Goals
│     ├── Create Goal
│     └── Goal Details
│
├── Training Plan
│     ├── Training Week
│     └── Workout Details
│
├── Runs
│     ├── Run History
│     └── Log Run
│
├── Progress
│
└── Profile
```

---

# 🧪 Testing Strategy

The most important business logic should be covered by unit tests.

Examples:

### Prediction

```text
Given:
5 km in 30:00

When:
predicting 10 km

Then:
return predicted 10K time
```

### Goal Validation

```text
Given:
10 km in 15 minutes

When:
assessing the goal

Then:
return UNREALISTIC
```

### Training Plan

```text
Given:
10-week running goal

When:
generating a plan

Then:
create 10 training weeks
```

---

# 🚀 Development Roadmap

## Phase 1 — Core Java

Build the domain model and business logic without Spring.

Focus on:

* OOP
* Collections
* Enums
* Streams
* Optional
* Exceptions
* LocalDate / Duration
* Algorithms
* Unit testing

---

## Phase 2 — Spring Boot

Introduce:

* Controllers
* Services
* Repositories
* DTOs
* Dependency Injection
* REST API

---

## Phase 3 — Persistence

Add:

* MySQL
* JPA
* Hibernate
* Entity relationships
* Custom queries

---

## Phase 4 — Application

Build the web interface using Thymeleaf.

Add:

* Dashboard
* Goal creation
* Goal assessment
* Training plan
* Run logging
* Progress statistics

---

## Phase 5 — Polish

Add:

* Validation
* Global exception handling
* Tests
* Better UI
* README
* Database diagram
* Architecture diagram

---

# 🚫 Out of Scope for V1

STRIDE intentionally does **not** try to become Strava.

The first version will not include:

* GPS tracking
* Maps
* Smartwatch integration
* Strava integration
* Social features
* Leaderboards
* Mobile application
* AI-generated coaching
* Nutrition tracking
* Microservices
* Kafka
* Complex authentication

These can be considered for future versions.

---

# 🔮 Future Ideas

Possible future improvements include:

* Adaptive training plans
* Automatic plan adjustment based on completed workouts
* Weather-aware training recommendations
* Race-day predictions
* Training load analysis
* VO₂ max estimation
* Heart-rate zones
* Running routes
* Strava integration
* Notifications
* Authentication and multiple users
* Docker deployment
* Cloud hosting

---

# 🎯 Project Goal

STRIDE is intentionally designed as a **small but meaningful full-stack project**.

The goal is not to build the most advanced running application.

The goal is to build a complete system that combines:

**Java fundamentals + Spring Boot + JPA + MySQL + business logic + algorithms + testing + a real user-facing application.**

Most importantly, STRIDE should answer one simple question:

> **"I have a goal. What will it take to get there?"**

**Every goal starts with a stride.**
