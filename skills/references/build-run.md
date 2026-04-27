# Build & Run

## Backend
```bash
cd backend
mvn spring-boot:run          # Run on port 8080
mvn clean package -DskipTests # Build JAR
```

## Frontend
```bash
cd frontend
npm install
npm run dev                  # Dev server on port 5173
npm run build                # Production build
```

## Database
```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

## Role-Based Access

| Role | Permissions |
|------|-------------|
| STUDENT | Own profile/achievements, submit reviews |
| TEACHER | View assigned class students, process reviews |
| CADRE | View own class students |
| ADMIN | Full access, user management, backup/restore |