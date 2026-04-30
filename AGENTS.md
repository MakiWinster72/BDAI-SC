# AGENTS.md - BDAI_SC Student Center

## Project

- **Backend**: Java 21 + Spring Boot 3.3.5 + MySQL + JPA + Spring Security + JWT
- **Frontend**: Vue 3 + Vite + Vue Router + Axios (no lint/typecheck/test scripts)
- **Ports**: Backend 8080, Frontend 5173

## Build Commands

### Backend
```bash
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

- Config file: `backend/src/main/resources/application.yml`
- Environment: `.env` in project root, imported via `spring.config.import` in `application.yml`
- Copy `.env.example` to `.env` before first run
- JWT token stored in localStorage as `bdai_sc_token`
- File uploads: POST `/api/upload`, stored under `backend/uploads/`
- JPA `ddl-auto: update` — schema auto-created on first run

## Architecture

- **Package**: `com.gcsc.studentcenter` (controller/service/repository/entity/dto/config/exception)
- **9 achievement types**: contest, research, paper, patent, certificate, works, journal, double-hundred, training
- **Roles**: STUDENT (default), CADRE, TEACHER, ADMIN
- **Review workflow**: achievements and profile changes require TEACHER/ADMIN approval

## Git Workflow

- Branch: `feat/name`, `fix/name`, `page/name`
- Commit (Chinese): `feat: 添加XX功能`, `fix: 修复XX问题`
