# Quiz Tournament — Backend REST API

A Spring Boot REST API for a pub quiz tournament application. Players can sign up, participate in quiz tournaments with questions fetched dynamically from the [Open Trivia Database](https://opentdb.com/), track scores on leaderboards, and like their favourite tournaments. Admins can create, update, and delete tournaments.

## Tech Stack

- **Java 17** + **Spring Boot 3.4**
- **Spring Data JPA** (Hibernate)
- **MySQL** (production) / **H2** (dev)
- **Spring Mail** — password reset & tournament notifications
- **Lombok** — boilerplate reduction
- **Maven** — dependency management
- **OpenTDB API** — dynamic quiz questions

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+ (or use the H2 dev profile)

## Quick Start

### Option 1: H2 (zero setup)

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts at `http://localhost:8080`. An H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:quizdb`).

### Option 2: MySQL

1. Create the database:
```sql
CREATE DATABASE quiz_tournament_db;
```

2. Update `src/main/resources/application.properties` with your MySQL credentials if they differ from the defaults.

3. Run:
```bash
mvn clean install
mvn spring-boot:run
```

### Default Admin User

On first startup the application seeds a default admin:

| Field    | Value      |
|----------|------------|
| Username | `admin`    |
| Password | `op@1234`  |

## Project Architecture

```
com.quiztournament.backend
├── config/          — Configuration (CORS, BCrypt, admin seed)
├── controller/      — REST controllers (no business logic here)
├── dto/             — Request/response data transfer objects
├── entity/          — JPA entities (User, QuizTournament, Question, etc.)
├── exception/       — Global exception handler + custom exceptions
├── repository/      — Spring Data JPA repositories
├── service/         — Business logic layer
└── QuizBackendApplication.java
```

The project follows a clean **Controller → Service → Repository** layered architecture. Controllers handle HTTP concerns only; all business logic lives in the service layer.

## API Endpoints

### Authentication

| Method | Endpoint                      | Description             |
|--------|-------------------------------|-------------------------|
| POST   | `/api/auth/register/admin`    | Register an admin user  |
| POST   | `/api/auth/register/player`   | Register a player user  |
| POST   | `/api/auth/login`             | Login                   |
| POST   | `/api/auth/logout/{userId}`   | Logout                  |
| POST   | `/api/auth/forgot-password`   | Request password reset  |
| POST   | `/api/auth/reset-password`    | Reset password with token |

### User Profile

| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| GET    | `/api/users/{userId}` | Get user profile  |
| PUT    | `/api/users/{userId}` | Update profile    |

### Admin — Tournament Management

| Method | Endpoint                                      | Description              |
|--------|-----------------------------------------------|--------------------------|
| GET    | `/api/admin/tournaments`                      | List all tournaments     |
| GET    | `/api/admin/tournaments/{id}`                 | Get tournament details   |
| POST   | `/api/admin/tournaments`                      | Create tournament        |
| PUT    | `/api/admin/tournaments/{id}`                 | Update tournament        |
| DELETE | `/api/admin/tournaments/{id}`                 | Delete tournament        |
| GET    | `/api/admin/tournaments/{id}/analytics`       | Tournament analytics     |
| GET    | `/api/admin/tournaments/{id}/likes`           | View like count          |
| GET    | `/api/admin/users`                            | View all player users    |

### Player — Tournaments & Quizzes

| Method | Endpoint                                         | Description              |
|--------|--------------------------------------------------|--------------------------|
| GET    | `/api/player/tournaments/ongoing`                | Ongoing tournaments      |
| GET    | `/api/player/tournaments/upcoming`               | Upcoming tournaments     |
| GET    | `/api/player/tournaments/past`                   | Past tournaments         |
| GET    | `/api/player/tournaments/participated?userId=X`  | Player's participated    |
| GET    | `/api/player/tournaments/search?category=X&difficulty=Y` | Search/filter    |
| GET    | `/api/player/tournaments/{id}`                   | Tournament detail        |
| POST   | `/api/player/tournaments/{id}/submit`            | Submit quiz answers      |
| GET    | `/api/player/tournaments/{id}/scores`            | Leaderboard              |
| POST   | `/api/player/tournaments/{id}/like/{userId}`     | Like tournament          |
| POST   | `/api/player/tournaments/{id}/unlike/{userId}`   | Unlike tournament        |
| GET    | `/api/player/tournaments/history/{userId}`       | Player quiz history      |

## Key Design Decisions

- **Tournament status is derived**, not stored — calculated from `startDate` / `endDate` vs current time (`UPCOMING`, `ONGOING`, `PAST`).
- **Questions fetched from OpenTDB** on tournament creation — supports both multiple-choice and true/false types.
- **Server-side answer validation** — the player submits `{questionId: answer}` and the backend calculates the score.
- **BCrypt password hashing** — passwords are never stored in plain text.
- **Duplicate participation prevention** — a unique constraint on `(user_id, quiz_tournament_id)` in the attempts table.
- **Email notifications** — players are notified when new tournaments are created; password reset uses token-based email flow.
- **Admin sees correct answers** — the admin question response includes correctAnswer for review; player responses exclude it until after submission.
