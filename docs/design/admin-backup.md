# 管理面板与备份

## 概述


管理后台将上传限制、审核策略、用户管理和数据备份/恢复四个管理域整合到一个受 RBAC 保护的选项卡式界面中。

## 架构概览

管理子系统遵循垂直切片模式：

- 前端：`AdminView.vue`
- API 模块：`admin.js`
- 控制器：`AdminController.java`
- 服务：`BackupService.java`、`UserService.java`

> 来源：AdminView.vue、AdminController.java、BackupService.java

## 管理后台板块

| 选项卡键名 | 标签 | 主要 API 调用 | 后端服务 |
|-----------|------|--------------|----------|
| upload | 上传限制 | fetchUploadSettings / saveUploadSettings | AchievementUploadSettingsService |
| review | 审核策略 | fetchReviewSettings / saveReviewSettings | ReviewSettingsService |
| users | 用户管理 | getUserList / updateUser / deleteUser | UserService |
| backup | 备份与恢复 | downloadBackupDb / restoreBackupDb / downloadBackupAttachments / restoreBackupAttachments | BackupService |

> 来源：AdminView.vue

## 用户管理

### 用户分页列表

`GET /api/admin/users` 接受 `page`、`size`、`search`、`role` 和 `className` 查询参数。

后端 `UserService.listUsersPaginated()` 构建动态 JPA Specification，在四个字段上应用 LIKE 谓词。

### 用户更新与删除

- 编辑模态框预填用户名和角色，密码留空表示"不修改"
- 删除操作通过 `confirm()` 对话框防护

> 来源：UserService.java、AdminView.vue

## 备份与恢复系统

### 数据库备份（SQL 导出）

使用 `mysqldump` 命令：

```bash
mysqldump --single-transaction --quick --lock-tables=false \
  --add-drop-table --add-drop-trigger
```

| 标志 | 用途 |
|------|------|
| --single-transaction | 获取 InnoDB 一致性快照 |
| --quick | 逐行流式传输避免大内存缓冲区 |
| --lock-tables=false | 显式禁用表锁定 |
| --add-drop-table | 包含 DROP TABLE IF EXISTS |

> 来源：BackupService.java

### 数据库恢复（SQL 导入）

- 接受 `.sql` 文件上传，验证扩展名
- 通过 `mariadb CLI` 执行恢复
- **完全破坏性**：删除并重建所有表

### 附件备份（ZIP 导出）

使用 `Files.walk()` 遍历 `uploads/` 目录，通过 `ZipOutputStream` 打包。

### 附件恢复（ZIP 导入）

- **增量补充**：现有文件被覆盖，不存在的文件保留
- 包含 **Zip Slip 漏洞防护**

> 来源：BackupService.java

## 备份 API 参考

| 端点 | 方法 | 请求 | 响应 | 行为 |
|------|------|------|------|------|
| /api/admin/backup/db | GET | — | .sql 文件 | 完整数据库转储 |
| /api/admin/restore/db | POST | .sql 文件 | JSON | 数据库恢复 |
| /api/admin/backup/attachments | GET | — | .zip 或 JSON 错误 | 上传目录 ZIP |
| /api/admin/restore/attachments | POST | .zip 文件 | JSON | 增量解压 |

## 配置依赖项

- `spring.datasource.url` — 解析以提取主机、端口、数据库名
- `app.upload-dir` — 上传附件的路径（默认 `./uploads`）

::: warning 前提条件
服务器必须安装并可访问 `mysqldump` 和 `mariadb` CLI。
:::

> 来源：application.yml

## 下一步阅读

- [JWT 身份验证流程](./jwt-auth) — 与 JWT 身份验证流程集成
- [基于角色的访问控制](./rbac) — ADMIN 角色强制执行
- [REST API 设计与控制器](./rest-api) — REST API 模式
- [审核与批准工作流](./review-workflow) — 审核策略设置
