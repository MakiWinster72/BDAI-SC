# API 参考

GCSC Student Center REST API。Base URL 为 `http://localhost:8080`，所有请求默认 `Content-Type: application/json`，除非注明为 `multipart/form-data`。

认证方式为 JWT Bearer Token，登录成功后返回的 `token` 字段放在请求头 `Authorization: Bearer <token>` 中。

公开接口（无需认证）：`/api/auth/register`、`/api/auth/login`、`/uploads/**`

---

## 认证 `/api/auth`

### POST `/api/auth/register` — 注册

注册新用户账号。

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 用户名，4-32字符，仅字母/数字/下划线 |
| `password` | string | 是 | 密码，6-32字符 |
| `displayName` | string | 是 | 显示名称，2-64字符 |
| `role` | string | 否 | 角色，`STUDENT`（默认）/ `TEACHER` / `ADMIN` |
| `studentNo` | string | 否 | 学号 |
| `className` | string | 否 | 班级 |
| `college` | string | 否 | 学院 |

```json
{
  "username": "zhangsan",
  "password": "password123",
  "displayName": "张三",
  "role": "STUDENT",
  "studentNo": "20214567",
  "className": "计科21-1班",
  "college": "计算机学院"
}
```

**响应** `AuthResponse`

```json
{
  "success": true,
  "message": "注册成功",
  "username": "zhangsan",
  "displayName": "张三",
  "role": "STUDENT",
  "studentNo": "20214567",
  "className": "计科21-1班",
  "college": "计算机学院",
  "avatarUrl": null,
  "token": "<jwt-token>",
  "tokenType": "Bearer"
}
```

---

### POST `/api/auth/login` — 登录

```json
{
  "username": "zhangsan",
  "password": "password123"
}
```

**响应** `AuthResponse`，同注册接口返回结构，包含 JWT token。

---

### GET `/api/auth/me` — 获取当前用户信息

需要认证。返回当前登录用户的基本信息。

**响应** `UserProfileResponse`

```json
{
  "username": "zhangsan",
  "displayName": "张三",
  "role": "STUDENT",
  "studentNo": "20214567",
  "className": "计科21-1班",
  "college": "计算机学院",
  "avatarUrl": null
}
```

---

### POST `/api/auth/change-password` — 修改密码

需要认证。

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `currentPassword` | string | 是 | 当前密码 |
| `newPassword` | string | 是 | 新密码，6-32字符 |

**响应** `200 OK`，空 body。

---

### GET `/api/auth/login-history` — 登录历史

需要认证。返回当前用户的登录记录，分页。

**Query Parameters**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | int | `0` | 页码（从 0 开始） |
| `size` | int | `20` | 每页条数 |

**响应** Spring `Page<LoginHistoryResponse>`

```json
{
  "content": [
    {
      "loginAt": "2026-04-16T10:23:45",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0 ..."
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

## 学生档案 `/api/student-profiles`

### GET `/api/student-profiles/me` — 获取我的档案

需要认证。返回当前用户的完整学生档案。

**响应** `StudentProfileResponse`

```json
{
  "id": 1,
  "fullName": "张三",
  "avatarUrl": null,
  "studentNo": "20214567",
  "classYear": 2021,
  "classMajor": "计算机科学与技术",
  "classNo": "1",
  "className": "计科21-1班",
  "college": "计算机学院",
  "enrollmentDate": "2021-09-01",
  "studentCategory": "本科生",
  "ethnicity": "汉族",
  "politicalStatus": "共青团员",
  "phone": "13800138000",
  "hkMoTw": false,
  "specialStudent": false,
  "fatherName": "张父",
  "motherName": "张母",
  "educationExperiences": [...],
  "cadreExperiences": [...]
}
```

### GET `/api/student-profiles/{id}` — 按 ID 获取档案

需要认证。教师/管理员可查看任意学生档案，学生仅可查看自己的。

**响应** 同 `StudentProfileResponse` 结构。

### PUT `/api/student-profiles/me` — 更新我的档案

需要认证。

**请求体** `StudentProfileRequest`，字段同响应中的大部分可写字段，包括：

- 基本信息：`fullName`、`avatarUrl`、`studentNo`、`classYear`、`classMajor`、`classNo`、`className`、`college`、`enrollmentDate`、`studentCategory`
- 政治面貌：`politicalStatus`、`ethnicity`
- 住宿信息：`dormCampus`、`dormBuilding`、`dormRoom`、`offCampusLiving`、`offCampusAddress`
- 联系人：`classTeacher`、`counselor`、`phone`、`backupContact`、`address`
- 证件：`idType`、`idNo`、`birthDate`、`nativePlace`
- 团组织：`leagueNo`、`leagueApplicationDate`、`leagueJoinDate`、`leagueJoined`、`leagueDeveloping`
- 入党信息：`partyApplied`、`notDeveloped`、`applicationDate`、`activistDate`、`activistDeveloping`、`partyTrainingDate`、`partyTrainingPending`、`developmentTargetDate`、`developmentTargetDeveloping`、`probationaryMemberDate`、`probationaryDeveloping`、`fullMemberDate`、`fullMemberDeveloping`
- 紧急联系人：`emergencyPhone`、`emergencyRelation`
- 港澳台/特长生：`hkMoTw`、`specialStudent`
- 父母信息：`fatherName`、`fatherPhone`、`fatherWorkUnit`、`fatherTitle`、`motherName`、`motherPhone`、`motherWorkUnit`、`motherTitle`
- 教育经历：`educationExperiences`（数组，每项含 `school`、`startDate`、`endDate`、`description`）
- 干部经历：`cadreExperiences`（数组，每项含 `position`、`startDate`、`endDate`、`description`）

**响应** 更新后的 `StudentProfileResponse`。

### PUT `/api/student-profiles/{id}` — 按 ID 更新档案

需要认证（教师/管理员）。同 `PUT /api/student-profiles/me` 请求体。

### GET `/api/student-profiles/search` — 搜索学生

需要认证（教师/管理员）。支持多条件分页搜索。

**Query Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| `page` | int | 页码，默认 `1` |
| `size` | int | 每页条数，默认 `5` |
| `classYear` | int | 入学年份 |
| `classNo` | string | 班级编号 |
| `college` | string | 学院 |
| `major` | string | 专业 |
| `hkMoTw` | boolean | 是否港澳台 |
| `specialStudent` | boolean | 是否特长生 |
| `studentCategory` | string | 学生类别 |
| `keyword` | string | 姓名/学号关键词 |

**响应** `StudentSearchResponse`

```json
{
  "content": [
    {
      "id": 1,
      "fullName": "张三",
      "studentNo": "20214567",
      "className": "计科21-1班",
      "college": "计算机学院",
      "avatarUrl": null
    }
  ],
  "totalElements": 10,
  "totalPages": 2,
  "size": 5,
  "number": 1
}
```

---

## 成就 `/api/achievements`

> 成就分类（`{category}` 路径参数）共 9 种：
>
> | category | 说明 |
> |----------|------|
> | `contest` | 竞赛 |
> | `paper` | 论文 |
> | `journal` | 期刊 |
> | `patent` | 专利 |
> | `certificate` | 证书 |
> | `research` | 科研 |
> | `works` | 作品 |
> | `doubleHundred` | 双百 |
> | `ieerTraining` | 培训 |

所有成就接口均需认证。学生只能操作自己的成就，教师/管理员可查询他人成就（通过 `studentNo` / `studentName` 参数过滤）。

### 通用请求/响应结构

**AchievementRecordRequest**

```json
{
  "imageUrl": "https://example.com/cover.jpg",
  "fields": {
    "studentNo": "20214567",
    "studentName": "张三",
    "categoryField1": "value1",
    "categoryField2": "value2",
    "_imageUrls": "url1,url2",
    "_attachments": "url3,url4"
  }
}
```

**AchievementRecordResponse**

```json
{
  "id": 1,
  "category": "contest",
  "imageUrl": "https://example.com/cover.jpg",
  "createdAt": "2026-04-16T10:00:00",
  "fields": {
    "studentNo": "20214567",
    "studentName": "张三",
    "contestName": "全国大学生程序设计竞赛",
    "awardLevel": "国家级一等奖",
    "_imageUrls": "...",
    "_attachments": "..."
  }
}
```

### GET `/api/achievements` — 列表

需要认证。

**Query Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 按分类过滤，不传则返回全部分类 |
| `studentNo` | string | 按学号过滤（教师/管理员） |
| `studentName` | string | 按姓名过滤（教师/管理员） |

**响应** `List<AchievementRecordResponse>`

---

### GET `/api/achievements/{category}/{id}` — 详情

需要认证。

**响应** `AchievementRecordResponse`

---

### POST `/api/achievements/{category}` — 创建成就

需要认证。

**响应** 创建后的 `AchievementRecordResponse`

---

### PUT `/api/achievements/{category}/{id}` — 更新成就

需要认证。

**响应** 更新后的 `AchievementRecordResponse`

---

### DELETE `/api/achievements/{category}/{id}` — 删除成就

需要认证。

**响应** `200 OK`，空 body。

---

### 各分类字段说明

**contest（竞赛）**

| fields 键 | 说明 |
|-----------|------|
| `contestName` | 竞赛名称（必填） |
| `organizer` | 主办单位 |
| `contestCategory` | 竞赛类别 |
| `awardCategory` | 奖项类别（一/二/三等奖等） |
| `awardLevel` | 奖项级别（校级/省级/国家级） |
| `contestType` | 竞赛类型 |
| `awardDate` | 获奖日期，格式 `YYYY-MM-DD` |
| `awardCount` | 获奖人数 |
| `teamMembers` | 团队成员 |
| `instructors` | 指导老师 |
| `remark` | 备注 |

**paper（论文）**

| fields 键 | 说明 |
|-----------|------|
| `paperTitle` | 论文名称（必填） |
| `journalName` | 期刊名称 |
| `publishDate` | 发表日期，格式 `YYYY-MM-DD` |
| `authorOrder` | 作者排名 |
| `indexed` | 收录情况（SCI/EI/核心等） |

**journal（期刊）**

| fields 键 | 说明 |
|-----------|------|
| `workTitle` | 作品名称（必填） |
| `publicationName` | 发表/出版单位 |
| `publishDate` | 发表日期，格式 `YYYY-MM-DD` |

**patent（专利）**

| fields 键 | 说明 |
|-----------|------|
| `patentName` | 专利名称（必填） |
| `patentType` | 专利类型（发明/实用新型/外观） |
| `grantNo` | 授权号 |
| `grantDate` | 授权日期，格式 `YYYY-MM-DD` |
| `firstInventor` | 第一发明人 |

**certificate（证书）**

| fields 键 | 说明 |
|-----------|------|
| `certificateName` | 证书名称（必填） |
| `certificateType` | 证书类型 |
| `obtainDate` | 获得日期，格式 `YYYY-MM-DD` |

**research（科研）**

| fields 键 | 说明 |
|-----------|------|
| `projectName` | 项目名称（必填） |
| `teacherNo` | 指导老师工号 |
| `projectLeader` | 项目负责人 |

**works（作品）**

| fields 键 | 说明 |
|-----------|------|
| `workName` | 作品名称（必填） |
| `workCategory` | 作品类别 |
| `workType` | 作品类型 |
| `publishDate` | 发布/出版日期 |
| `publishOccasion` | 发表场合 |
| `organizer` | 主办单位 |
| `impactScope` | 影响范围 |

**doubleHundred（双百）**

| fields 键 | 说明 |
|-----------|------|
| `projectName` | 申报作品名（必填） |
| `projectCategory` | 项目类别 |
| `projectDomain` | 项目领域 |
| `projectLeader` | 项目负责人 |
| `leaderStudentNo` | 负责人学号 |
| `educationLevel` | 学历层次 |
| `teamMembers` | 团队成员 |
| `instructors` | 指导老师 |
| `teamSize` | 团队人数 |
| `plannedLevel` | 计划等级 |
| `college` | 学院 |
| `finalLevel` | 最终等级 |

**ieerTraining（培训）**

| fields 键 | 说明 |
|-----------|------|
| `projectName` | 项目名称（必填） |
| `collegeName` | 学院名称 |
| `projectType` | 项目类型 |
| `projectLeader` | 项目负责人 |
| `instructorName` | 指导老师姓名 |
| `recommendedLevel` | 推荐等级 |
| `isKeyArea` | 是否重点领域 |
| `finalStatus` | 最终状态 |

---

## 成就审核请求 `/api/achievement-review-requests`

学生提交成就材料给教师/管理员审核。

### GET `/api/achievement-review-requests` — 列表

需要认证。返回当前用户可见的审核请求（提交者可见自己的，管理员可见全部）。

**响应** `List<AchievementReviewRequestResponse>`

```json
{
  "id": 1,
  "requestType": "ACHIEVEMENT",
  "category": "contest",
  "achievementId": 10,
  "status": "PENDING",
  "submittedAt": "2026-04-16T10:00:00",
  "reviewedAt": null,
  "reviewer": null,
  "rejectReason": null
}
```

`status` 可选值：`PENDING` / `APPROVED` / `REJECTED` / `CANCELLED`

### POST `/api/achievement-review-requests` — 提交审核

需要认证。

**请求体** `AchievementReviewSubmitRequest`

```json
{
  "category": "contest",
  "achievementId": 10
}
```

**响应** `AchievementReviewRequestResponse`

---

### POST `/api/achievement-review-requests/{id}/approve` — 批准

需要认证（教师/管理员）。

**响应** `AchievementReviewRequestResponse`

---

### POST `/api/achievement-review-requests/{id}/reject` — 拒绝

需要认证（教师/管理员）。

**请求体** `AchievementReviewDecisionRequest`

```json
{
  "reason": "材料不完整，请补充"
}
```

**响应** `AchievementReviewRequestResponse`

---

### DELETE `/api/achievement-review-requests/{id}` — 撤回

需要认证（提交者本人）。仅 `PENDING` 状态可撤回。

**响应** `204 No Content`

---

## 档案审核请求 `/api/profile-review-requests`

学生提交档案修改给教师/管理员审核。接口结构与成就审核请求完全一致。

### GET `/api/profile-review-requests` — 列表

需要认证。

**响应** `List<ProfileReviewRequestResponse>`

### POST `/api/profile-review-requests` — 提交审核

**请求体** `ProfileReviewSubmitRequest`

```json
{
  "profileId": 1
}
```

### POST `/api/profile-review-requests/{id}/approve` — 批准

### POST `/api/profile-review-requests/{id}/reject` — 拒绝

**请求体** `ProfileReviewDecisionRequest`

```json
{
  "reason": "信息有误，请核对后重新提交"
}
```

### DELETE `/api/profile-review-requests/{id}` — 撤回

---

## 设置 `/api/settings`

### GET `/api/settings/review` — 获取审核设置

需要认证（管理员）。

**响应** `ReviewSettingsResponse`

```json
{
  "profileReviewEnabled": true,
  "profileReviewAutoApprove": false,
  "achievementReviewEnabled": true,
  "achievementReviewAutoApprove": false
}
```

### PUT `/api/settings/review` — 更新审核设置

需要认证（管理员）。

**请求体** `ReviewSettingsRequest`

```json
{
  "profileReviewEnabled": true,
  "profileReviewAutoApprove": false,
  "achievementReviewEnabled": true,
  "achievementReviewAutoApprove": false
}
```

### GET `/api/settings/achievement-upload` — 获取成就上传设置

需要认证（管理员）。

**响应** `AchievementUploadSettingsResponse`

```json
{
  "imageMaxCount": 9,
  "imageMaxSizeMb": 10,
  "attachmentMaxCount": 9,
  "attachmentMaxSizeMb": 200,
  "attachmentDocumentExts": "pdf,doc,docx,xls,xlsx",
  "attachmentVideoExts": "mp4,avi,mov,wmv",
  "attachmentImageExts": "jpg,jpeg,png,gif,webp",
  "attachmentArchiveExts": "zip,rar,7z"
}
```

### PUT `/api/settings/achievement-upload` — 更新成就上传设置

需要认证（管理员）。

**请求体** `AchievementUploadSettingsRequest`，字段同响应结构。

---

## 文件上传 `/api/upload`

### POST `/api/upload` — 上传文件

需要认证。`Content-Type: multipart/form-data`。

**Form Fields**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | file | 是 | 文件，最大 200MB |
| `context` | string | 否 | 上传上下文标识 |

**响应** `UploadResponse`

```json
{
  "url": "/uploads/2026/04/16/abc123.jpg",
  "filename": "abc123.jpg",
  "size": 102400,
  "contentType": "image/jpeg"
}
```

---

## 管理后台 `/api/admin`

> 以下接口均需 `ADMIN` 角色认证。

### GET `/api/admin/users` — 用户列表

**Query Parameters**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | int | `1` | 页码 |
| `size` | int | `20` | 每页条数 |
| `search` | string | — | 按用户名/显示名搜索 |
| `role` | string | — | 按角色过滤 |
| `className` | string | — | 按班级过滤 |

**响应**

```json
{
  "content": [
    {
      "id": 1,
      "username": "zhangsan",
      "displayName": "张三",
      "role": "STUDENT",
      "studentNo": "20214567",
      "className": "计科21-1班",
      "college": "计算机学院",
      "enabled": true
    }
  ],
  "totalElements": 100,
  "totalPages": 5
}
```

---

### PUT `/api/admin/users/{id}` — 更新用户

需要 `ADMIN`。

**请求体** `UpdateUserRequest`

```json
{
  "displayName": "张三（新）",
  "role": "TEACHER",
  "enabled": true
}
```

---

### DELETE `/api/admin/users/{id}` — 删除用户

需要 `ADMIN`。

**响应**

```json
{ "success": true }
```

或错误：

```json
{ "success": false, "message": "不能删除自己" }
```

---

### GET `/api/admin/backup/db` — 导出数据库 SQL 备份

需要 `ADMIN`。返回二进制 `.sql` 文件流。

---

### POST `/api/admin/restore/db` — 恢复数据库

需要 `ADMIN`。`Content-Type: multipart/form-data`，上传 `.sql` 文件。

| 字段 | 类型 | 必填 |
|------|------|------|
| `file` | file | 是，`.sql` 格式 |

---

### GET `/api/admin/backup/attachments` — 导出附件压缩包

需要 `ADMIN`。返回二进制 `.zip` 文件流。

---

### POST `/api/admin/restore/attachments` — 恢复附件

需要 `ADMIN`。`Content-Type: multipart/form-data`，上传 `.zip` 文件。

---

## 错误响应

所有接口错误统一返回以下格式：

```json
{
  "success": false,
  "message": "错误描述信息"
}
```

HTTP 状态码说明：

| 状态码 | 说明 |
|--------|------|
| `400` | 请求参数错误或业务校验失败 |
| `401` | 未认证或 Token 失效 |
| `403` | 无权限（角色不足） |
| `404` | 资源不存在 |
| `500` | 服务器内部错误 |
