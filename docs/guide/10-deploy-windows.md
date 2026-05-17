# Windows Server 部署指南

## 公共环境要求

| 依赖    | 版本要求                  |
| ------- | ------------------------- |
| JDK     | 21+（如 Eclipse Temurin） |
| MySQL   | 8.0+                      |
| Maven   | 3.9+                      |
| Node.js | 18+（仅构建前端用）       |
| Nginx   | 最新稳定版                |

---

## 方案一：Docker 部署（仅 Windows Server 2019）

适合 **Windows Server 2019**，Docker 支持完整，安装简单。

### 目录结构

```
D:\GCSC\
├── docker-compose.yml
├── backend\
│   └── Dockerfile
├── frontend\
│   ├── Dockerfile
│   └── nginx.conf
└── uploads\
```

### 步骤 1：安装 Docker Engine（Windows Server 2019）

以管理员身份打开 PowerShell，运行：

```powershell
Install-Module DockerMsftProvider -Force
Install-Package Docker -ProviderName DockerMsftProvider -Force
Restart-Computer -Force
```

重启后验证：`docker --version`

> 如果上述命令失败，可能是 **Containers 功能未启用**，先运行：
>
> ```powershell
> Install-WindowsFeature Containers
> Restart-Computer -Force
> ```

### 步骤 2：配置 Docker 镜像加速

以管理员身份打开 PowerShell，运行：

```powershell
# 创建配置目录（如果不存在）
New-Item -Path "C:\ProgramData\Docker\config" -ItemType Directory -Force

# 写入镜像加速配置
@"
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.cn",
    "https://docker.rainbond.cc"
  ]
}
"@ | Out-File -FilePath "C:\ProgramData\Docker\config\daemon.json" -Encoding UTF8

# 重启 Docker 服务
Restart-Service Docker
```

配置完成后验证：`docker info | Select-String "Registry Mirrors"` 或 `docker run --rm hello-world`

### 步骤 3：上传项目

将项目完整上传到服务器，例如 `D:\GCSC\`，并创建以下文件。

### 步骤 4：编写 docker-compose.yml

在 `D:\GCSC\docker-compose.yml`：

```yaml
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
      - "5173:80"
```

### 步骤 5：编写后端 Dockerfile

在 `backend/` 目录下创建 `Dockerfile`：

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src/ ./src/
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 步骤 6：编写前端 Dockerfile

在 `frontend/` 目录下创建 `Dockerfile`：

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . ./
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 步骤 7：编写 Nginx 配置

在 `frontend/` 目录下创建 `nginx.conf`：

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

### 步骤 8：启动服务

```powershell
cd D:\GCSC
docker compose up -d --build
```

访问：`http://服务器IP:5173`

### 步骤 9：验证服务状态

```powershell
docker compose ps        # 查看容器状态
docker compose logs -f   # 查看所有容器实时日志
docker compose logs -f backend  # 只看后端日志
```

---

## 方案二：直接部署（JAR + Nginx）

适合 **Windows Server 2016**，也适用于 **Server 2019 无法使用 Docker** 的环境。

### 依赖安装步骤

#### 安装 JDK 21

1. 下载 [Eclipse Temurin 21 LTS](https://adoptium.net/temurin/releases/?version=21)（选择 `.msi` 安装包，x64）
2. 运行安装程序，记住安装路径（如 `C:\Program Files\Eclipse Adoptium\jdk-21...`）
3. 设置环境变量：

   ```powershell
   # 系统环境变量中添加
   JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.x_x
   # 并将 %JAVA_HOME%\bin 加入 PATH
   ```

4. 验证：`java -version`

#### 安装 MySQL 8.0

1. 下载 [MySQL 8.0 Installer](https://dev.mysql.com/downloads/mysql/8.0.html)
2. 运行安装，选择 **Server only** 或 **Full**
3. 设置 root 密码，记住它
4. 安装完成后登录 MySQL 创建数据库和用户：

   ```sql
   CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
   CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY '你的密码';
   GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
   FLUSH PRIVILEGES;
   ```

#### 安装 Nginx

1. 下载 [Nginx Windows 版](https://nginx.org/en/download.html)（选择 `nginx-1.x.x.zip`）
2. 解压到 `C:\nginx`
3. 验证：双击 `nginx.exe` 或在命令行运行 `C:\nginx\nginx.exe`
4. 访问 `http://localhost` 确认 Nginx 正常运行

#### 安装 Maven（如需在服务器上编译后端）

1. 下载 [Maven 3.9+](https://maven.apache.org/download.cgi)
2. 解压到 `C:\maven`
3. 设置环境变量：

   ```powershell
   MAVEN_HOME=C:\maven\apache-maven-3.9.x
   # 将 %MAVEN_HOME%\bin 加入 PATH
   ```

4. 验证：`mvn -version`

#### 安装 Node.js（用于构建前端）

1. 下载 [Node.js 18+ LTS](https://nodejs.org/)（Windows Installer `.msi`）
2. 运行安装程序，全程默认设置即可
3. 验证：`node -v` 和 `npm -v`

### 步骤 1：配置 .env 文件

复制项目根目录的 `.env.example` 为 `.env`，修改以下关键项：

```properties
BDAI_SC_DB_HOST=127.0.0.1
BDAI_SC_DB_PORT=3306
BDAI_SC_DB_NAME=bdai_sc
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=<你的MySQL密码>
BDAI_SC_JWT_SECRET=<至少32位随机字符串>
BDAI_SC_CORS_ALLOWED_ORIGINS=http://服务器IP:5173
BDAI_SC_FRONTEND_HOST=0.0.0.0
BDAI_SC_UPLOAD_DIR=D:\GCSC\uploads
```

### 步骤 2：编译后端

```powershell
cd D:\GCSC\backend
mvn clean package -DskipTests
```

生成的 JAR：`D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar`

### 步骤 3：运行后端

```powershell
# 方式一：后台运行（无黑窗口）
start /b javaw -jar D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar

# 方式二：前台运行（可看到日志，方便调试）
java -jar D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar
```

> 首次启动会自动创建默认管理员账号（用户名 `bdai`，密码 `bdai2026`）。

### 步骤 4：构建前端

```powershell
cd D:\GCSC\frontend
npm install
npm run build
```

生成的静态文件在 `D:\GCSC\frontend\dist\`

### 步骤 5：配置 Nginx 反向代理

编辑 `C:\nginx\conf\nginx.conf`，在 `http {}` 块中加入：

```nginx
server {
    listen 80;
    server_name 你的服务器IP或域名;

    location / {
        root D:\GCSC\frontend\dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/uploads/;
    }
}
```

重载 Nginx 配置：`C:\nginx\nginx.exe -s reload`

### 步骤 6：配置防火墙

```powershell
netsh advfirewall firewall add rule name="GCSC Frontend" dir=in action=allow protocol=TCP localport=5173
```

---

## 日常运维命令

### Docker 部署（方案一）

在 `D:\GCSC\` 目录打开 PowerShell 运行：

```powershell
# 启动（首次或下次）
docker compose up -d

# 更新（代码有修改后重新构建）
docker compose up -d --build

# 关闭（保留数据）
docker compose down

# 关闭（删除 MySQL 数据）
docker compose down -v

# 查看容器状态
docker compose ps

# 查看所有容器实时日志
docker compose logs -f

# 查看指定容器日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db

# 查看最近 N 行日志（不加 -f）
docker compose logs --tail=100

# 重启后端
docker compose restart backend

# 进入 MySQL 容器
docker exec -it gcsc_mysql mysql -ubdai_sc -p
```

### 直接部署（方案二）

```powershell
# 后端（后台运行）
start /b javaw -jar D:\GCSC\backend\target\student-center-backend-0.0.1-SNAPSHOT.jar

# 查看端口占用
netstat -ano | findstr 8080
netstat -ano | findstr 5173

# 重启 Nginx
C:\nginx\nginx.exe -s reload

# 查看后端进程
tasklist | findstr java
taskkill /F /PID <PID>   # 关闭后端进程
```

---

## 文件存储说明

### 附件存储位置

用户上传的附件统一存放在项目根目录的 `uploads/` 目录下：

```
D:\GCSC\              # 项目根目录（Windows）
~/projects/GCSC/      # 项目根目录（Linux）
└── uploads/          # 用户上传的附件
    ├── avatar/       # 头像图片
    ├── achievement/  # 成果附件
    └── ...
```

**Docker 部署时**，`uploads/` 通过 volume 映射到容器内 `/app/uploads`，容器删除后文件不丢失：

```yaml
backend:
  volumes:
    - ./uploads:/app/uploads # 宿主机 ./uploads → 容器 /app/uploads
```

### 数据目录（Docker）

```bash
mysql_data/           # MySQL 数据文件（Docker volume）
uploads/              # 用户上传文件
```

---

## 数据库访问

### Docker 部署

```powershell
# 进入 MySQL 容器
docker exec -it gcsc_mysql mysql -ubdai_sc -p

# 宿主机用 mysql 客户端连接
mysql -h127.0.0.1 -P3306 -ubdai_sc -p
```

### 直接部署

```powershell
mysql -h127.0.0.1 -P3306 -ubdai_sc -p
```

### 连接信息

| 参数   | 值                                           |
| ------ | -------------------------------------------- |
| 主机   | `127.0.0.1`（Docker 部署也可用 `db` 服务名） |
| 端口   | `3306`                                       |
| 用户   | `bdai_sc`                                    |
| 密码   | `bdai_sc`                                    |
| 数据库 | `bdai_sc`                                    |

---

## 生产环境注意事项

### 安全配置

| 配置项                         | 生产要求                                   |
| ------------------------------ | ------------------------------------------ |
| `BDAI_SC_JWT_SECRET`           | 务必更换为至少 32 位的随机字符串           |
| `BDAI_SC_CORS_ALLOWED_ORIGINS` | 改为前端实际域名，避免 `*`                 |
| MySQL 密码                     | 使用强密码，不要使用默认密码               |
| 数据库备份                     | 定期备份 `mysql_data` 卷或使用 `mysqldump` |

### 防火墙设置

```powershell
netsh advfirewall firewall add rule name="GCSC Frontend" dir=in action=allow protocol=TCP localport=5173
netsh advfirewall firewall add rule name="GCSC Backend" dir=in action=allow protocol=TCP localport=8080
```

### 数据备份

#### MySQL 备份（Docker）

```powershell
docker exec gcsc_mysql mysqldump -ubdai_sc -pbdai_sc bdai_sc > backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql
```

#### MySQL 备份（直接部署）

```powershell
mysqldump -ubdai_sc -p bdai_sc > D:\GCSC\backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql
```

#### Docker 数据卷备份

```powershell
docker run --rm -v gcsc_mysql_data:/data -v D:\backup:/backup alpine tar czf /backup/mysql_data_$(Get-Date -Format "yyyyMMdd").tar.gz -C /data .
```

---

## 故障排查

| 问题                | 可能原因                                      | 解决方案                                                                                                   |
| ------------------- | --------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| 前端无法访问        | 防火墙未开放 5173 端口                        | `netsh advfirewall firewall add rule name="GCSC Frontend" dir=in action=allow protocol=TCP localport=5173` |
| API 请求失败        | 后端未启动或端口被占用                        | `netstat -ano \| findstr 8080`，确认 JAR 进程在运行                                                        |
| 文件上传失败        | `uploads` 目录不存在或无写入权限              | 手动创建 `D:\GCSC\uploads` 目录                                                                            |
| 数据库连接失败      | MySQL 未启动或密码错误                        | 检查 MySQL 服务状态，确认 `.env` 中密码正确                                                                |
| CORS 错误           | `BDAI_SC_CORS_ALLOWED_ORIGINS` 未包含前端地址 | 修改 `.env` 中的 CORS 配置，重启后端                                                                       |
| Docker 拉取镜像失败 | 国内访问 Docker Hub 受限                      | 参考"步骤 2：配置 Docker 镜像加速"配置加速器                                                               |
| Docker 服务无法启动 | Windows 容器功能未启用                        | `Install-WindowsFeature Containers`，重启后再试                                                            |
| 后端 JAR 启动报错   | JDK 版本不对（低于 21）                       | 确认 `java -version` 显示 21，JAVA_HOME 指向 JDK 而非 JRE                                                  |
| Nginx 报 502        | 后端未启动或端口不对                          | 先确认后端在 8080 端口运行 `netstat -ano \| findstr 8080`                                                  |
| Maven 编译失败      | 网络问题（下载依赖慢）                        | 配置 Maven 镜像，或在服务器上保持网络畅通                                                                  |

---

