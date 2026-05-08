# Windows Server 部署指南

本指南介绍如何在 Windows Server 上部署 BDAI-SC 学生信息管理中心，推荐使用 **Docker 部署**，适合长期运行。

## 环境准备

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| Windows Server | 2019 / 2022 | 建议使用 Azure 或物理机 |
| Docker Desktop | 最新稳定版 | 开启 WSL 2 后端 |
| MySQL | 8.0+ | 可用 Docker 容器或云数据库 |

### 安装 Docker Desktop

1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. 运行安装包，勾选 **Use WSL 2 instead of Hyper-V**
3. 安装完成后重启服务器
4. 验证：`docker --version`

---

## 方案一：Docker 部署（推荐）

### 目录结构

```
D:\GCSC\
├── docker-compose.yml
├── backend\
│   └── Dockerfile
├── frontend\
│   ├── Dockerfile
│   └── nginx.conf
└── init.sql          # 数据库初始化脚本（可选）
```

### 步骤 1：上传项目

将项目完整上传到服务器，例如 `D:\GCSC\`

### 步骤 2：编写 docker-compose.yml

在项目根目录创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    container_name: gcsc_mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: bdai2026root
      MYSQL_DATABASE: bdai_sc
      MYSQL_USER: bdai_sc
      MYSQL_PASSWORD: bdai_sc
    ports:
      - "3306:3306"
    volumes:
      - ./mysql_data:/var/lib/mysql

  backend:
    build: ./backend
    container_name: gcsc_backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      BDAI_SC_DB_HOST: db
      BDAI_SC_DB_PORT: 3306
      BDAI_SC_DB_NAME: bdai_sc
      BDAI_SC_DB_USER: bdai_sc
      BDAI_SC_DB_PASSWORD: bdai_sc
      BDAI_SC_JWT_SECRET: <替换为复杂随机字符串>
      BDAI_SC_CORS_ALLOWED_ORIGINS: "*"
      BDAI_SC_BACKEND_PORT: 8080
      BDAI_SC_UPLOAD_DIR: /app/uploads
    volumes:
      - ./uploads:/app/uploads

  frontend:
    build: ./frontend
    container_name: gcsc_frontend
    restart: always
    ports:
      - "5173:80
```

### 步骤 3：编写后端 Dockerfile

在 `backend/` 目录下创建 `Dockerfile`：

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 步骤 4：编写前端 Dockerfile 和 Nginx 配置

在 `frontend/` 目录下创建 `Dockerfile`：

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json .
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

在同一目录创建 `nginx.conf`：

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://gcsc_backend:8080/api/;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Host $host;
    }

    location /uploads/ {
        proxy_pass http://gcsc_backend:8080/uploads/;
        proxy_set_header Host $host;
    }
}
```

### 步骤 5：启动服务

```powershell
cd D:\GCSC
docker compose up -d --build
```

启动后访问：`http://服务器IP:5173`

### 步骤 6：验证服务状态

```powershell
docker compose ps        # 查看容器状态
docker compose logs -f  # 查看实时日志
```

---

## 方案二：直接运行（JAR + Nginx）

适合无法使用 Docker 的环境。

### 环境要求

- JDK 21（如 Eclipse Temurin）
- MySQL 8.0+
- Node.js 18+（仅构建前端用）
- Nginx

### 步骤 1：配置 .env 文件

复制 `.env.example` 为 `.env`，修改以下关键项：

```properties
BDAI_SC_DB_HOST=127.0.0.1
BDAI_SC_DB_PORT=3306
BDAI_SC_DB_NAME=bdai_sc
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=<你的密码>
BDAI_SC_JWT_SECRET=<更换为复杂随机字符串>
BDAI_SC_CORS_ALLOWED_ORIGINS=http://服务器IP:5173
BDAI_SC_FRONTEND_HOST=0.0.0.0
BDAI_SC_UPLOAD_DIR=D:\GCSC\uploads
```

### 步骤 2：编译后端

```powershell
cd D:\GCSC\backend
mvn clean package -DskipTests
```

生成的 JAR 文件：`backend\target\student-center-backend-0.0.1-SNAPSHOT.jar`

### 步骤 3：运行后端

```powershell
java -jar D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar
```

### 步骤 4：构建前端

```powershell
cd D:\GCSC\frontend
npm install
npm run build
```

生成的静态文件在 `frontend\dist\`

### 步骤 5：配置 Nginx

```nginx
server {
    listen 80;
    server_name 你的域名或IP;

    # 前端静态文件
    location / {
        root D:\GCSC\frontend\dist;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 上传文件
    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/;
    }
}
```

重启 Nginx：`nginx -s reload`

---

## 生产环境注意事项

### 安全配置

| 配置项 | 生产要求 |
|--------|---------|
| `BDAI_SC_JWT_SECRET` | 务必更换为至少 32 位的随机字符串 |
| `BDAI_SC_CORS_ALLOWED_ORIGINS` | 改为前端实际域名，避免 `*` |
| MySQL 密码 | 使用强密码，不要使用默认密码 |
| 数据库备份 | 定期备份 `mysql_data` 卷或使用 `mysqldump` |

### 防火墙设置

```powershell
# 开放前端端口（外部访问）
netsh advfirewall firewall add rule name="GCSC Frontend" dir=in action=allow protocol=TCP localport=5173

# 开放后端端口（仅 Nginx 需要）
netsh advfirewall firewall add rule name="GCSC Backend" dir=in action=allow protocol=TCP localport=8080
```

### 常用运维命令

#### Docker 部署

```powershell
docker compose up -d --build      # 启动并构建
docker compose down              # 停止服务
docker compose down -v           # 停止并删除数据卷
docker compose restart backend   # 重启后端
docker compose logs -f backend  # 查看后端日志
docker exec -it gcsc_mysql mysql -ubdai_sc -p  # 进入 MySQL
```

#### 直接部署

```powershell
# 后端（后台运行）
start /b java -jar D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar

# 查看端口占用
netstat -ano | findstr 8080
netstat -ano | findstr 5173

# 重启 Nginx
nginx -s reload
```

### 数据备份

#### MySQL 备份

```powershell
docker exec gcsc_mysql mysqldump -ubdai_sc -pbdai_sc bdai_sc > backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql
```

#### Docker 数据卷备份

```powershell
docker run --rm -v gcsc_mysql_data:/data -v D:\backup:/backup alpine tar czf /backup/mysql_data_$(Get-Date -Format "yyyyMMdd").tar.gz -C /data .
```

---

## 故障排查

| 问题 | 解决方案 |
|------|---------|
| 前端无法访问 | 检查防火墙是否开放 5173 端口 |
| API 请求失败 | 检查后端是否正常运行在 8080 端口 |
| 文件上传失败 | 检查 `uploads` 目录是否创建且有写入权限 |
| 数据库连接失败 | 检查 `BDAI_SC_DB_HOST` 是否正确，MySQL 容器是否启动 |
| CORS 错误 | 检查 `BDAI_SC_CORS_ALLOWED_ORIGINS` 是否包含前端地址 |
