# 审核与审批工作流

## 概述


BDAI-SC 学生中心实现了一套双轨审核系统，用于管控学生对成就记录和个人信息的修改。

## 架构概述

审核工作流跨越四个架构层：实体持久化、服务编排、REST 控制器和前端组合式函数。两条审核轨道（成就和个人信息）共享相同的结构模式。

> 来源：AchievementReviewRequestService.java、ProfileReviewRequestService.java

## 实体模型

### 成就审核请求 (AchievementReviewRequest)

| 列名 | 类型 | 用途 |
|------|------|------|
| requester_id | FK → app_user | 提交请求的学生 |
| reviewer_id | FK → app_user | 裁定的教师/管理员 |
| category | VARCHAR(32) | 成就类别键 |
| action | VARCHAR(16) | create 或 update |
| status | VARCHAR(16) | pending、approved、rejected |
| record_id | BIGINT | 更新操作的目标记录 ID |
| payload_json | LONGTEXT | 序列化的 DTO——批准时应用 |
| payload_snapshot_json | LONGTEXT | 审核 UI 渲染优化的结构化快照 |
| changes_json | LONGTEXT | 差异条目数组 |

> 来源：AchievementReviewRequest.java

### 个人信息审核请求 (ProfileReviewRequest)

更精简，缺少 category、action 和 record_id 字段。

> 来源：ProfileReviewRequest.java

## 审核设置与关卡控制

| 标志 | 默认值 | 效果 |
|------|--------|------|
| profileReviewEnabled | true | 禁用时拒绝提交 |
| profileReviewAutoApprove | false | 立即批准（需启用） |
| achievementReviewEnabled | true | 禁用时拒绝提交 |
| achievementReviewAutoApprove | false | 立即批准（需启用） |

> 来源：ReviewSettingsService.java

## 请求生命周期

### 提交流程

1. 角色检查（STUDENT）
2. 审核启用检查
3. 对于 update：验证目标记录存在性
4. 载荷验证
5. 持久化（pending 或立即批准）

### 批准与副作用

- `applyApprovedRequest()` 将 `payload_json` 反序列化
- 根据 action 路由到 `AchievementService.create()` 或 `update()`
- 在 `@Transactional` 边界内执行

### 驳回与取消

- 驳回需要非空的理由
- 取消是硬删除，仅限原始请求者

> 来源：AchievementReviewRequestService.java

## 可见性作用域

| 用户角色 | 成就查询 | 个人信息查询 |
|----------|----------|-------------|
| STUDENT | findAllByRequester_Username... | 同左 |
| TEACHER / ADMIN | findAllByOrderByUpdatedAtDesc() | 同左 |

> 来源：AchievementReviewRequestRepository.java

## REST API 层

| 方法 | 端点 | 鉴权 | 描述 |
|------|------|------|------|
| GET | /api/achievement-review-requests | 任何已认证用户 | 列出可见请求 |
| POST | /api/achievement-review-requests | STUDENT | 提交新的成就审核 |
| POST | .../{id}/approve | TEACHER, ADMIN | 批准 |
| POST | .../{id}/reject | TEACHER, ADMIN | 驳回 |
| DELETE | .../{id} | STUDENT (所有者) | 取消 |
| GET | /api/profile-review-requests | ... | 同上结构 |
| GET | /api/settings/review | 任何已认证用户 | 获取审核设置 |
| PUT | /api/settings/review | ADMIN | 更新审核设置 |

> 来源：AchievementReviewRequestController.java

## 前端通知中心

### useNotifications 组合式函数

统一管理两条审核轨道与系统通知：

```javascript
状态键:
- notifications: Array       // 系统通知
- achievementReviewRequests: Array
- profileReviewRequests: Array
- processedReadIds: Set      // 已查看的已处理条目
```

### 条目分类

| 类别键 | 条件 | 徽标文本 |
|--------|------|----------|
| pending | status === "pending" 且 < 3天 | "待处理" |
| delayed | status === "pending" 且 ≥ 3天 | "已滞后N天" |
| approved | status === "approved" | "已通过" |
| rejected | status === "rejected" | "已驳回" |

> 来源：useNotifications.js

## 跨轨道对比

| 维度 | 成就审核 | 个人信息审核 |
|------|----------|-------------|
| 支持操作 | create、update | 仅隐式更新 |
| 类别字段 | 有（9个类别） | 无 |
| 批准时的写服务 | AchievementService | StudentProfileService |
| 前端差异渲染 | 并排快照卡片 | 字段级变更列表 |

## 下一步阅读

- [多实体成就系统](./achievement-system) — 九个成就类别及实体模型
- [REST API 设计与控制器](./rest-api) — 审核控制器如何融入 API 拓扑
- [基于角色的访问控制](./rbac) — TEACHER、ADMIN 和 STUDENT 角色强制执行
