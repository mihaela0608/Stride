# STRIDE 🏃‍♀️
### Your running journey, built around your goals.

**STRIDE** is a running coaching application designed to help runners turn ambitions into realistic goals, understand their current performance, and work towards measurable progress.

Instead of treating every running goal as just a distance and a finish time, STRIDE aims to connect goal setting, performance assessment, personalized training, and progress tracking in one place.

The project is being developed as a backend-focused application with an emphasis on clean architecture, secure authentication, data validation, and practical software engineering principles.

---

## ✨ Vision

Running progress is not just about running faster. It is about understanding where you are, deciding where you want to go, and building a sustainable path between the two.

STRIDE aims to make that process more structured and personal by helping runners:

- Define meaningful running goals.
- Understand how ambitious their goals are relative to their current performance.
- Build a structured path towards their targets.
- Track their progress over time.
- Adapt their training as their circumstances and performance change.

**The goal is not simply to run more. It is to train with purpose.**

## 🚀 Features

### ✅ Implemented

- **User registration and authentication** — account creation and login with Spring Security.
- **Secure password storage** — passwords are stored using a password-hashing mechanism rather than as plain text.
- **Runner profiles** — a dedicated profile for storing running-related information separately from general account details.
- **Goal management** — create running goals, replace an active goal, complete it, and retrieve existing goals.
- **Goal assessment** — evaluate a goal using available running-performance data and calculate an assessment that includes required improvement, estimated time to progress, a projected date, difficulty, and an explanation.
- **Input validation and error handling** — validate incoming requests and return meaningful HTTP error responses.
- **REST API** — expose application functionality through HTTP endpoints, independently of a specific frontend.

*The implementation is evolving, and the feature list will be updated as additional functionality is completed and tested.*

### 🛠️ In development

- **Personalized training plans** — generate structured training plans based on a runner's profile, current performance, and target goal.
- **Training adaptation** — explore ways to adjust planned workouts when a runner misses a session or needs to change their schedule.
- **Run logging and progress tracking** — record running sessions and use them to monitor progress towards goals.
- **Performance insights** — make running data more useful through meaningful summaries and comparisons.

### 🔭 Future direction

- Interactive frontend built with React and TypeScript.
- Running activity integrations where API access is available.
- More advanced training recommendations and adaptive coaching.
- Progress visualizations and training analytics.

---

## 🧠 Goal Assessment

A running goal should be ambitious enough to motivate a runner, but grounded in their current capabilities.

STRIDE's goal assessment uses available runner-performance data, including:

- Longest recent run.
- Average running pace.
- Average weekly running distance.
- Target distance.
- Target finish time.
- Target date.

The assessment calculates the improvement required to reach the target and estimates the time needed to progress. It also assigns a difficulty level and provides an explanation to help the runner understand the result.

The target date matters: a performance improvement that may be unrealistic in a few weeks could be more achievable over a longer period.

The intention is to provide **transparent, data-informed guidance**, rather than treating every goal as equally achievable or relying on distance alone.

> These assessments are estimates, not guarantees of athletic performance or substitutes for professional coaching or medical advice.

---

## 🏗️ Architecture

STRIDE follows a layered backend architecture that separates HTTP handling, business logic, data access, and persistence.

```text
Client / Postman
       |
       v
REST Controllers
       |
       v
Service Layer
       |
       v
Repositories
       |
       v
MySQL Database
```

### Main layers

| Layer | Responsibility |
|---|---|
| `web` | REST endpoints and HTTP responses |
| `service` | Business logic and application workflows |
| `repository` | Database access through Spring Data JPA |
| `model/entity` | Persistent domain entities |
| `model/dto` | Request and response objects |
| `model/enums` | Defined domain states and categories |
| `service/session` | Authentication-related services and current-user access |

The separation of responsibilities makes the application easier to maintain, test, extend, and integrate with a separate frontend.

### Key design decisions

- **DTOs instead of exposing entities directly** — API responses contain the fields intended for clients.
- **Service-layer business logic** — domain operations are kept out of controllers.
- **Authenticated user context** — user-specific operations rely on the currently authenticated account rather than trusting a user ID supplied by the client.
- **Centralized exception handling** — expected application errors can be translated into consistent HTTP responses.
- **Relational persistence** — entities and relationships are managed through JPA and MySQL.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Primary programming language |
| Spring Boot | Application framework |
| Spring MVC | REST API development |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database persistence |
| Hibernate | ORM implementation |
| MySQL | Relational database |
| Jakarta Bean Validation | Request validation |
| Lombok | Reduce repetitive Java boilerplate |
| Maven | Dependency management and build automation |
| Postman | API development and manual testing |
| Git & GitHub | Version control and collaboration |

---

## 📡 API Overview

The backend exposes REST endpoints under `/api`.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/users/register` | Register a new account |
| `GET` | `/api/users/me` | Retrieve the authenticated user's information |
| `POST` | `/api/goals` | Create a running goal |
| `POST` | `/api/goals/replace` | Replace the active goal |
| `PATCH` | `/api/goals/active/complete` | Complete the active goal |
| `GET` | `/api/goals` | Retrieve the current runner's goals |

Authentication and security configuration determine which endpoints require an authenticated session. The exact API contract may evolve during development.

---

## ⚙️ Getting Started

### Prerequisites

Make sure you have installed:

- Java 21 or a compatible JDK.
- Maven, or the project's Maven Wrapper.
- MySQL Server.
- Git.

### 1. Clone the repository

```bash
git clone https://github.com/mihaela0608/STRIDE.git
cd STRIDE
```

Replace the repository URL if the project's GitHub repository uses a different name.

### 2. Configure the database

Create a MySQL database for the application:

```sql
CREATE DATABASE stride;
```

Configure the database connection in your local Spring Boot configuration.

For example, in `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Set the corresponding environment variables for your local environment. Configure JPA and schema management according to the project's existing setup.

**Security note:** Never commit database passwords, secret keys, or other credentials to GitHub.

### 3. Run the application

Using the Maven Wrapper on Windows:

```bash
mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Alternatively, use your IDE to run the Spring Boot application class.

### 4. Test the API

Open Postman and send requests to:

```text
http://localhost:8080
```

For authenticated requests, use the application's configured login flow and maintain the required session cookies and CSRF token.

---

## 🧪 Testing

API workflows can be tested independently of the frontend using Postman.

Important scenarios include:

- Successful and unsuccessful registration and login.
- Access to protected endpoints.
- Runner profile creation and validation.
- Goal creation with valid and invalid inputs.
- Preventing multiple active goals.
- Replacing and completing goals.
- Retrieving only the authenticated runner's data.
- Correct handling of expected errors.

Automated unit and integration tests are a natural next step as the application evolves.

---

## 🗺️ Roadmap

- [x] Establish the Spring Boot backend.
- [x] Implement user registration and authentication.
- [x] Introduce runner profiles.
- [x] Implement core goal-management operations.
- [x] Introduce goal assessment logic.
- [ ] Complete and validate the authenticated-user API.
- [ ] Implement personalized training-plan generation.
- [ ] Introduce running-session logging.
- [ ] Add progress tracking and performance analytics.
- [ ] Build a React and TypeScript frontend.
- [ ] Expand automated testing.
- [ ] Explore external running-data integrations.

---

## 🎯 Project Goals

STRIDE is both a product in development and an opportunity to apply software engineering concepts to a practical problem.

The project focuses on:

- Designing maintainable Java applications.
- Building RESTful APIs with Spring Boot.
- Implementing authentication and authorization.
- Modeling domain entities and relational data.
- Separating responsibilities across architectural layers.
- Validating input and handling application errors.
- Designing business logic that can be tested independently.
- Iteratively developing, testing, and improving a real-world application.


---

*Built with Java, a love for running, and the belief that every meaningful goal deserves a plan.*

**STRIDE — Run with purpose.**
