# AGENTS.md - BDAI_SC Student Center

## Project

- **Backend**: Java 21 + Spring Boot 3.3.5 + MySQL + JPA + Spring Security + JWT
- **Frontend**: Vue 3 + Vite + Vue Router + Axios (no lint/typecheck/test scripts)
- **Ports**: Backend 8080, Frontend 5173

## Build Commands

> **First run**: `cp .env.example .env` then `source .env`

### Backend
```bash
source .env          # required — .env loaded via spring.config.import
cd backend
mvn spring-boot:run            # Run application
mvn clean package -DskipTests  # Build JAR without tests
mvn test                       # Run all tests
mvn test -Dtest=ClassName      # Run single test class
mvn test -Dtest=ClassName#methodName  # Run single test method
```

### Frontend
```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
npm run build
npm run preview
```

## Database Setup
```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

## Configuration

- Config: `backend/src/main/resources/application.yml` loads `.env` via `spring.config.import` (searches `backend/` then `../`)
- **Always `source .env`** from project root before running backend — env vars are not auto-loaded by Maven
- JWT token: stored in localStorage as `bdai_sc_token`
- File uploads: POST `/api/upload`, stored under `backend/uploads/` (or `$BDAI_SC_UPLOAD_DIR`)
- JPA `ddl-auto: update` — schema auto-created on first run
- No backend tests currently exist (`src/test/` is empty)

## Architecture

- **Package**: `com.gcsc.studentcenter` (controller/service/repository/entity/dto/config/exception)
- **9 achievement types**: contest, research, paper, patent, certificate, works, journal, double-hundred, training
- **Roles**: STUDENT (default), CADRE, TEACHER, ADMIN
- **Review workflow**: achievements and profile changes require TEACHER/ADMIN approval; CADRE can only review their own class

## Git Workflow

- Branch: `feat/feature-name`, `fix/bug-description`, `page/page-name`
- Commit (Chinese): `feat: 添加XX功能`, `fix: 修复XX问题`
