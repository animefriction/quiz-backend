# Progress Log — Quiz Tournament Backend

## Day 1 (26 March 2026)

### Session 1: Project Setup & Clean Architecture
- Analysed the original codebase and identified gaps against assignment requirements
- Removed dead `Quiz` entity, controller, service, and repository (leftover code)
- Rewrote `User` entity with all required fields: firstName, lastName, role (ADMIN/PLAYER), profilePicture, phoneNumber, dateOfBirth, bio
- Added `Role` enum for admin/player distinction
- Rewrote `QuizTournament` entity with startDate, endDate, minPassingScore — status now derived from dates (UPCOMING/ONGOING/PAST)
- Rewrote `Question` entity to support both multiple-choice (4 options) and true/false (2 options) from OpenTDB
- Created all DTOs: RegisterRequest, LoginRequest, UserUpdateRequest, QuizTournamentCreateRequest/UpdateRequest/Response, QuestionResponse, QuizSubmissionRequest (answers as map), ScoreResponse
- Implemented BCrypt password hashing via spring-security-crypto
- Created AdminUserInitializer to seed default admin (username: admin, password: op@1234)
- Configured dual database profiles: MySQL (production) + H2 (dev)
- Added .gitignore and comprehensive README
- **Commit:** `feat: initial project setup with clean architecture, entities, and OpenTDB integration`

### Session 2: Core Feature Completion
- Fixed admin question response to include correctAnswer (required by frontend rubric for "view questions" feature)
- **Commit:** `fix: expose correct answer in admin tournament question response`

- Implemented full email service:
  - PasswordResetToken entity with expiry and used flag
  - Forgot password endpoint generates UUID token, emails user
  - Reset password endpoint validates token and updates password
  - Tournament creation sends async notification to all player users (excludes admins)
- **Commit:** `feat: add email service for password reset and tournament notifications`

- Added two additional admin features:
  - Tournament analytics endpoint: total attempts, average score, highest/lowest score, pass rate, total likes
  - View all player users endpoint
- **Commit:** `feat: add tournament analytics and view all players endpoints for admin`

- Added two additional player features:
  - Player quiz history endpoint: all past attempts with scores and dates
  - Tournament search/filter by category and difficulty
- **Commit:** `feat: add player quiz history and tournament search endpoints`

- Updated README with complete API documentation covering all endpoints
- **Commit:** `docs: update API documentation with all endpoints`

### Session 3: Testing
- Created unit tests using Mockito:
  - AuthServiceTest: 8 tests (register, duplicates, login, password reset flow)
  - QuizParticipationServiceTest: 6 tests (scoring, feedback, duplicates, leaderboard)
  - UserServiceTest: 4 tests (profile CRUD, uniqueness checks)
- Created integration tests using MockMvc + H2:
  - AuthControllerTest: 7 tests (register, validation, login, logout)
  - AdminQuizTournamentControllerTest: 10 tests (CRUD, analytics, likes, validation)
- Fixed LazyInitializationException by adding @Transactional annotations to service read methods
- **Commits:**
  - `test: add unit tests for AuthService, QuizParticipationService, and UserService`
  - `test: add controller integration tests for auth and admin tournament endpoints`
  - `fix: add @Transactional to service methods to prevent LazyInitializationException`

### Session 4: Final Polish
- Added per-question feedback to quiz submission response (questionId, playerAnswer, correctAnswer, isCorrect)
- Updated unit tests to verify feedback content
- Created progress log
- **Commits:**
  - `feat: add per-question feedback to quiz submission response`
  - `docs: add progress log`

## Summary

| Metric | Count |
|--------|-------|
| Total commits | 11 |
| Java source files | 48 |
| Test files | 6 |
| Test cases | 40+ |
| API endpoints | 22 |
| Entities | 7 (User, QuizTournament, Question, QuizAttempt, QuizLike, PasswordResetToken, Role) |
