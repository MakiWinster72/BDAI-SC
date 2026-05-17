# 配置参考

## 概述


BDAI-SC 的配置分为四个独立的层级：application.yml 静态属性、编译后的 @Configuration 类、前端环境变量，以及运行时数据库托管设置。

## 后端：application.yml

### 数据源与 JPA

| 键 | 默认值 | 描述 |
|---|--------|------|
| spring.datasource.url | jdbc:mysql://localhost:3306/gcsc?... | 完整 JDBC 连接字符串 |
| spring.datasource.username | gcsc | 数据库用户名 |
| spring.datasource.password | gcsc | 数据库密码 |
| spring.jpa.hibernate.ddl-auto | update | Schema 自动更新 |
| spring.jpa.open-in-view | false | 禁用 OSIV 模式 |

### 服务器与文件上传

| 键 | 默认值 | 描述 |
|---|--------|------|
| server.port | 8080 | 后端监听端口 |
| spring.servlet.multipart.max-file-size | -1 | 单文件上传限制（-1 表示无限制） |
| app.upload-dir | ./uploads | 上传文件存储目录 |

### 安全：CORS 与 JWT

| 键 | 默认值 | 描述 |
|---|--------|------|
| security.cors.allowed-origins | * | CORS 来源白名单 |
| security.jwt.secret | gcsc-student-center-jwt-secret-key... | JWT 签名密钥（生产环境必须修改） |
| security.jwt.expires-minutes | 120 | Token 有效期（分钟） |

## 前端：环境变量

### API 基础 URL

```javascript
const API_BASE =
  import.meta.env.VITE_API_BASE || `http://${window.location.hostname}:8080`
```

| 设置项 | 默认值 | 描述 |
|--------|--------|------|
| VITE_API_BASE | http://:8080 | API 基础地址 |
| timeout | 12000（12秒） | 全局请求超时 |

## 运行时管理员设置（数据库）

### 成果上传限制

| 设置键 | 默认值 | 范围 | 描述 |
|--------|--------|------|------|
| ImageMaxCount | 3 | 1-9 | 每条记录最大图片数量 |
| ImageMaxSizeMb | 10 | 1-200 MB | 每张图片最大文件大小 |
| AttachmentMaxCount | 10 | 1-20 | 每条记录最大附件数量 |
| AttachmentMaxSizeMb | 50 | 1-200 MB | 每个附件最大文件大小 |

### 审核工作流设置

| 设置键 | 默认值 | 描述 |
|--------|--------|------|
| profileReviewEnabled | true | 是否开启档案审核 |
| profileReviewAutoApprove | false | 是否自动批准档案变更 |
| achievementReviewEnabled | true | 是否开启成就审核 |
| achievementReviewAutoApprove | false | 是否自动批准成就 |

## 配置速查汇总

| 关注点 | 层级 | 是否可在运行时修改 |
|--------|------|-------------------|
| 数据库连接 | application.yml | 仅通过环境变量 |
| JWT 签名密钥 | application.yml | 仅通过环境变量 |
| CORS 允许来源 | application.yml | 仅通过环境变量 |
| 上传限制 | system_settings | 通过管理面板 |
| 审核工作流开关 | system_settings | 通过管理面板 |

## 环境变量覆盖模式

| 环境变量 | 覆盖项 |
|----------|--------|
| SPRING_DATASOURCE_URL | spring.datasource.url |
| SPRING_DATASOURCE_PASSWORD | spring.datasource.password |
| SECURITY_JWT_SECRET | security.jwt.secret |
| SERVER_PORT | server.port |
| VITE_API_BASE | 前端 API 基础地址 |
