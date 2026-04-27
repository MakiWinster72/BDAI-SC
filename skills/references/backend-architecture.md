# Backend Architecture

## Package Structure
```
com.gcsc.studentcenter
├── controller/   # REST endpoints
├── service/      # Business logic
├── repository/   # JpaRepository data access
├── entity/       # JPA entities (DB models)
├── dto/          # Request/Response objects
├── config/       # Security, CORS, WebMvc, JWT filter
└── exception/    # GlobalExceptionHandler
```

## Database Conventions
- Tables/columns: **snake_case** (`student_profiles`, `display_name`)
- Foreign keys: `table_name_id` pattern
- All timestamps: `created_at`, `updated_at`

## Key Entities

| Entity | Table | Purpose |
|--------|-------|---------|
| `AppUser` | `users` | User accounts with roles (STUDENT, TEACHER, ADMIN, CADRE) |
| `StudentProfile` | `student_profiles` | Comprehensive profile (demographics, education, cadre, family) |
| `EducationExperience` | (embedded) | Education history rows |
| `CadreExperience` | (embedded) | Leadership position rows |
| `Achievement*` (9 types) | `achievement_*` | Academic achievements |

## Achievement Entity Pattern (9 Types)

All achievement types share identical structure via polymorphic pattern:

| Category Key | Entity | Table |
|-------------|--------|-------|
| `contest` | AchievementContest | achievement_contests |
| `paper` | AchievementPaper | achievement_papers |
| `journal` | AchievementJournal | achievement_journals |
| `patent` | AchievementPatent | achievement_patents |
| `certificate` | AchievementCertificate | achievement_certificates |
| `research` | AchievementResearch | achievement_researches |
| `works` | AchievementWorks | achievement_works |
| `doubleHundred` | AchievementDoubleHundred | achievement_double_hundreds |
| `ieerTraining` | AchievementIeerTraining | achievement_ieer_trainings |

**Common fields**: `author_id`, `student_no`, `student_name`, `image_url`, `_image_urls` (JSON), `_attachments` (JSON), `created_at`, plus category-specific fields.

## Security & JWT Flow

1. **Login**: `POST /api/auth/login` → `AuthService.login()` → `JwtService.generateToken()`
2. **Token claims**: `sub` (username), `displayName`, `role`
3. **Request**: Frontend `request.js` adds `Authorization: Bearer <token>`
4. **Filter**: `JwtAuthenticationFilter.doFilterInternal()` parses token, sets `ROLE_<role>` authority
5. **Public routes**: `/api/auth/register`, `/api/auth/login`, `/uploads/**`, `/api/achievements/**`
6. **BCrypt** password encoding via `PasswordEncoder` bean

## Key Backend Files

| File | Purpose |
|------|---------|
| `config/SecurityConfig.java` | Filter chain, public routes, session stateless |
| `config/JwtAuthenticationFilter.java` | Token parsing, SecurityContext population |
| `config/GlobalCorsFilter.java` | CORS (runs before Security, @Order(HIGHEST_PRECEDENCE)) |
| `config/WebConfig.java` | Maps `/uploads/**` to upload directory |
| `service/JwtService.java` | JJWT generate/parse (HMAC-SHA, 120min default) |
| `service/AchievementService.java` | Polymorphic CRUD for all 9 achievement types |
| `service/StudentProfileService.java` | Profile CRUD with role-based class filtering |
| `service/AuthService.java` | Registration, login, auto-creates StudentProfile |
| `exception/GlobalExceptionHandler.java` | Returns `{success:false, message}` |