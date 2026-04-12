# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GCSC Student Center - Full-stack web application for student achievement management.

- **Backend**: Java 21 + Spring Boot 3.3.5 + MySQL + JPA + Spring Security + JWT
- **Frontend**: Vue 3 (Composition API with `<script setup>`) + Vite + Vue Router + Axios + AG Grid
- **Ports**: Backend 8080, Frontend 5173

## Build Commands

### Backend
```bash
cd backend
mvn spring-boot:run          # Run application
mvn clean package             # Build JAR
mvn clean package -DskipTests # Build without tests
mvn test                      # Run all tests
mvn test -Dtest=ClassName     # Run single test class
mvn test -Dtest=ClassName#methodName  # Run single test method
```

### Frontend
```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Development server (http://localhost:5173)
npm run build        # Production build
npm run preview      # Preview production build
```

### Database Setup
```sql
CREATE DATABASE IF NOT EXISTS gcsc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'gcsc'@'localhost' IDENTIFIED BY 'gcsc';
GRANT ALL PRIVILEGES ON gcsc.* TO 'gcsc'@'localhost';
FLUSH PRIVILEGES;
```

## Architecture

### Backend Package Structure
```
com.gcsc.studentcenter
├── controller/   # REST controllers
├── service/      # Business logic
├── repository/   # Data access (JpaRepository)
├── entity/       # JPA entities
├── dto/          # Request/response DTOs
├── config/       # Security, CORS, JWT filter
└── exception/    # Global exception handling
```

### Achievement Entity Pattern
Each achievement type has its own entity and repository:
- `AchievementContest` - 竞赛
- `AchievementResearch` - 科研
- `AchievementPaper` - 论文
- `AchievementPatent` - 专利
- `AchievementCertificate` - 证书
- `AchievementWorks` - 作品
- `AchievementJournal` - 期刊
- `AchievementDoubleHundred` - 双百
- `AchievementIeerTraining` - 培训

All share a common structure: title, description, media attachments, user association.

### JWT Authentication Flow
1. **Login/Register**: `AuthService` generates JWT with `sub` (username), `displayName`, and `role` claims
2. **Request**: Frontend `request.js` interceptor adds `Authorization: Bearer <token>` header
3. **Filter**: `JwtAuthenticationFilter.doFilterInternal()` parses token, extracts role, sets `ROLE_<role>` authority in SecurityContext
4. **SecurityConfig**: Routes `/api/auth/register`, `/api/auth/login`, `/uploads/**`, `/api/achievements/**` are public; everything else requires authentication
5. **Username validation**: `^[a-zA-Z0-9_]{4,32}$` (4-32 chars, alphanumeric and underscore)

### Frontend Directory Structure
```
frontend/src/
├── api/          # Axios request modules (e.g., profile.js, achievement.js)
├── assets/       # Static assets
│   └── styles/   # Global CSS: dialogs.css, layout.css, achievements.css, ...
├── components/   # Shared Vue components
├── composables/  # Vue composables (useDashboardShell, etc.)
├── constants/    # Menu constants, vocabulary
├── layouts/      # DashboardLayout.vue and layout wrappers
├── router/       # Vue Router config (index.js)
├── utils/        # Export utilities, view transitions, student profile export helpers
└── views/        # Page-level components (LoginView, RegisterView, StudentInfoView, etc.)
```

### Frontend CSS Organization
Global styles in `frontend/src/assets/styles/`:
- `dialogs.css` - Dialog/sheet overlays, toasts, hints, upload queue
- `layout.css` - Dashboard shell, grid, mobile capsule, responsive breakpoints
- `achievements.css` - Achievement card layouts, form styles, media grid
- `auth.css` - Auth page layout, ghost/action button base styles

### StudentProfile Structure
- `StudentProfile` has embedded `EducationExperience` (education history) and `CadreExperience` (leadership positions) via JPA `@ElementCollection`
- Controlled vocabulary for fields like `politicalStatus`, `ethnicity`, `currentEducationLevel`

### Security Configuration
- Session: Stateless (JWT-based)
- Password: BCrypt encoding via `PasswordEncoder` bean
- CORS: Allowed origins include `localhost:5173`, `127.0.0.1:5173`, and local network patterns (`192.168.*.*`, `10.*.*.*`, `172.*.*.*`)
- JWT secret configured in `application.yml` (`security.jwt.secret`)

### File Uploads
- Max size: 200MB (configured in `application.yml`)
- Storage: `backend/uploads/` directory (not in classpath)
- Endpoint: `/api/upload`
- Supported previews: images, videos, PDFs, docx/doc, xlsx/xls

### Frontend Request Module
- Token stored as `gcsc_token` in localStorage
- 401 responses automatically clear token and redirect to `/login`
- `VITE_API_BASE` env var overrides default `http://localhost:8080`

### Embedded Mode
Views support `?embed=1` query param to hide the sidebar, footer, and top bar — used for rendering views inside iframes. The `DashboardLayout.vue` applies `dashboard-layout-embedded` class and hides `AppFooter` when `isEmbedded` is true.

## Key Conventions

### Backend
- Database tables/columns: snake_case (`student_profiles`, `display_name`)
- Foreign keys: `table_name_id` pattern
- Role names in DB become `ROLE_<name>` in Spring Security
- Use `@Valid` on DTOs for validation

### Frontend
- Views in `frontend/src/views/`
- API modules in `frontend/src/api/`
- Router in `frontend/src/router/index.js`
- CSS classes: kebab-case
- Button components use `ghost-button` (outlined) and `action-button` (filled primary) globally — override height with `!important` when needed in scoped styles

### Design & Animation Patterns
- Dialogs use `sheet-overlay` / `sheet-modal` pattern from `dialogs.css` — frosted glass backdrop with `z-index: 1000`, modal at `z-index: 1010+`
- Animation timing: `0.42s cubic-bezier(0.22, 1, 0.36, 1)` for transform, `0.38s ease` for opacity
- See `docs/animation.md` for full animation guidelines

## Git Workflow
- Features: `feat/feature-name`
- Bug fixes: `fix/bug-description`
- Pages: `page/page-name`
- Style: `style/description`
- Commit style: Present tense, verb first (e.g., "Add login page")

## Project Roadmap
See `TODO.md` for planned features including: xlsx/pdf/csv export, dark mode, mobile optimizations (capsule UI, sidebar).
