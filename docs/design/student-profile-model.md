# 数据模型与 JPA 实体

本文档记录 GCSC 后端完整持久化层——每个 JPA 实体、其表映射、字段语义和关系拓扑。后端使用 **Spring Boot 3.3.5** 配合 **spring-boot-starter-data-jpa**，后端为 **MySQL**，Hibernate 的 `ddl-auto: update` 策略。所有实体位于 `com.gcsc.studentcenter.entity`，使用纯 Java Getter/Setter 编写（无 Lombok），目标 Java 21。

> 来源：[AppUser.java](backend/src/main/java/com/gcsc/studentcenter/entity/AppUser.java#L1-L141)、[StudentProfile.java](backend/src/main/java/com/gcsc/studentcenter/entity/StudentProfile.java#L1-L711)、[AchievementReviewRequest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementReviewRequest.java#L1-L186)、[ProfileReviewRequest.java](backend/src/main/java/com/gcsc/studentcenter/entity/ProfileReviewRequest.java#L1-L141)、[SystemSetting.java](backend/src/main/java/com/gcsc/studentcenter/entity/SystemSetting.java#L1-L48)、[LoginHistory.java](backend/src/main/java/com/gcsc/studentcenter/entity/LoginHistory.java#L1-L74)

## 实体全景概览

持久化模型组织为四个内聚域：**身份与访问**（用户、角色、登录历史）、**学生档案**（档案 + 教育/干部经历）、**成就**（9 种类型特定实体）、**工作流与配置**（审核请求、系统设置）。

## 身份与访问实体

### `AppUser` — `users` 表

中心身份实体。系统中的每个用户——学生、教师、管理员或干部——都在此表中对应一行。使用**自增主键**，BCrypt 哈希密码存储在 `password_hash` 中。

| 字段 | Java 类型 | 约束 | 用途 |
|------|----------|------|------|
| `id` | `Long` | PK, AUTO_INCREMENT | 内部标识符 |
| `display_name` | `String` | NOT NULL, 最大 64 | UI 中显示的人类可读名称 |
| `username` | `String` | NOT NULL, UNIQUE, 最大 32 | 登录凭据标识符 |
| `password_hash` | `String` | NOT NULL, 最大 100 | BCrypt 哈希密码 |
| `role` | `UserRole` | NOT NULL, 最大 16 | RBAC 角色枚举 (STUDENT/TEACHER/ADMIN/CADRE) |
| `student_no` | `String` | 可空, 最大 32 | 用于快速查找的反规范化学生号 |
| `class_name` | `String` | 可空, 最大 64 | 班级分配（用于范围过滤） |
| `college` | `String` | 可空, 最大 64 | 学院/系归属 |
| `avatar_url` | `String` | 可空, 最大 255 | 上传头像图片的 URL |
| `assigned_classes` | `String` | 可空, 最大 500 | 用于教师/管理员范围控制的逗号分隔班级列表 |
| `remark` | `String` | 可空, 最大 255 | 管理备注 |
| `created_at` | `LocalDateTime` | NOT NULL | 账户创建时间戳 |

> 来源：[AppUser.java](backend/src/main/java/com/gcsc/studentcenter/entity/AppUser.java#L11-L50)

### `UserRole` — 枚举

| 值 | 范围 | 描述 |
|-----|------|------|
| `STUDENT` | 仅自己的数据 | 标准学生账户 |
| `CADRE` | 自己班级学生 + 自己的档案 | 有领导职责的学生 |
| `TEACHER` | 所管理班级学生 + 自己的档案 | 可审核并查看所管理班级的学生 |
| `ADMIN` | 所有用户、所有数据 | 不受限制的管理权限 |

> 来源：[UserRole.java](backend/src/main/java/com/gcsc/studentcenter/entity/UserRole.java#L3-L8)

### `LoginHistory` — `login_histories` 表

认证事件的**仅追加审计日志**。它不使用 `@ManyToOne` 关联回 `AppUser`，而是存储一个普通的 `Long userId`。这是一种刻意的解耦模式：登录记录在用户实体被修改时仍然保留。

| 字段 | Java 类型 | 用途 |
|------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `user_id` | `Long` | 到 `users.id` 的 FK（不是 JPA 关联） |
| `ip_address` | `String` | 登录时的客户端 IP |
| `user_agent` | `String` | 浏览器/客户端 User-Agent 字符串 |
| `device_name` | `String` | 解析后的设备标识符 |
| `login_time` | `LocalDateTime` | 精确登录时间戳 |

> 来源：[LoginHistory.java](backend/src/main/java/com/gcsc/studentcenter/entity/LoginHistory.java#L11-L29)、[LoginHistoryRepository.java](backend/src/main/java/com/gcsc/studentcenter/repository/LoginHistoryRepository.java#L15-L29)

## 学生档案域

### `StudentProfile` — `student_profiles` 表

系统**最大实体**，达 711 行，代表一个全面的学生信息记录。它通过 `@OneToOne @JoinColumn(name = "user_id", nullable = false, unique = true)` 与 `AppUser` 共享**严格的一对一**关系。对子集合使用 **`cascade = CascadeType.ALL`** 和 **`orphanRemoval = true`**。

实体字段按逻辑分组：

**基本信息**：fullName、studentNo、classYear、classMajor、classNo、className、college、enrollmentDate、studentCategory

**人口统计**：ethnicity、politicalStatus、birthDate、nativePlace、idType、idNo

**住宿与联系方式**：dormCampus、dormBuilding、dormRoom、offCampusLiving、offCampusAddress、phone、backupContact、address、emergencyPhone、emergencyRelation

**共青团员追踪**：配对字段（`boolean` 标志 + `LocalDate` 里程碑）追踪共青团进度：leagueJoined/leagueJoinDate、leagueDeveloping、leagueApplicationDate、leagueNo

**党员追踪**：平行的七阶段追踪系统：partyApplied、applicationDate、activistDate/activistDeveloping、partyTrainingDate/partyTrainingPending、developmentTargetDate/developmentTargetDeveloping、probationaryMemberDate/probationaryDeveloping、fullMemberDate/fullMemberDeveloping、notDeveloped

**特殊状态标志**：isHk、isMo、isTw（港澳台身份）、specialStudent/specialStudentType/specialStudentRemark

**家庭信息**：父亲和母亲的独立块（name、phone、workUnit、title）

**"发展中"布尔模式**是一个关键设计约定：每个党员阶段使用一个布尔值表示学生是否*当前处于*该阶段，配合记录何时到达该阶段的日期。

> 来源：[StudentProfile.java](backend/src/main/java/com/gcsc/studentcenter/entity/StudentProfile.java#L18-L711)

### `EducationExperience` — `education_experiences` 表

**多对一**子实体关联到 `StudentProfile`，使用**惰性获取**（`FetchType.LAZY`）。

| 字段 | Java 类型 | 用途 |
|------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `profile_id` | `Long` | 到 `student_profiles.id` 的 FK（惰性 @ManyToOne） |
| `start_date` | `LocalDate` | 教育开始日期 |
| `end_date` | `LocalDate` | 教育结束日期 |
| `school_name` | `String` | 最大 128，机构名称 |
| `education_level` | `String` | 最大 64，教育层次 |
| `witness` | `String` | 最大 64，认证人 |
| `is_current` | `Boolean` | 是否为当前在读 |

> 来源：[EducationExperience.java](backend/src/main/java/com/gcsc/studentcenter/entity/EducationExperience.java#L15-L104)

### `CadreExperience` — `cadre_experiences` 表

在结构上镜像 `EducationExperience`，但捕获**学生干部/组织职位**。

| 字段 | Java 类型 | 用途 |
|------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `profile_id` | `Long` | 到 `student_profiles.id` 的 FK（惰性 @ManyToOne） |
| `start_date` | `LocalDate` | 职位开始日期 |
| `end_date` | `LocalDate` | 职位结束日期 |
| `department` | `String` | 最大 128，组织部门 |
| `position` | `String` | 最大 64，角色/职位名称 |
| `description` | `String` | 最大 512，职责与成就 |
| `is_current` | `Boolean` | 是否目前担任此职位 |

> 来源：[CadreExperience.java](backend/src/main/java/com/gcsc/studentcenter/entity/CadreExperience.java#L15-L104)

## 成就实体 — 九类并行模式

成就系统采用**每类型一张具体表**策略，无类继承。九种成就类型各是完全独立的 `@Entity`，映射到自己的数据库表。

### 共享字段模式

每个成就实体包含这组完全相同的跨领域字段：

| 共享字段 | Java 类型 | 描述 |
|---------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `author` | `ManyToOne → AppUser` | 记录所有者，强制非空 |
| `student_no` | `String` | 最大 32，录入时的学生证号 |
| `student_name` | `String` | 最大 64，学生显示名 |
| `image_url` | `String` | 最大 255，旧版单图字段 |
| `_image_urls` | `String` (TEXT) | JSON 序列化的多图 URL 数组 |
| `_attachments` | `String` (TEXT) | JSON 序列化的文件附件元数据数组 |
| `created_at` | `LocalDateTime` | NOT NULL，记录创建时间戳 |

对 `author` 使用**贪婪获取**是有意为之——因为成就记录几乎总是在渲染时附带作者信息，额外的连接查询优于 N+1 惰性加载查询。

`\_image\_urls\` 和 `\_attachments\` 字段使用下划线前缀，存储为包含 JSON 数组的原始 TEXT 列。Service 层通过 Jackson 的 `ObjectMapper` 处理这些字符串的解析。

> 来源：[AchievementPaper.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementPaper.java#L12-L54)、[AchievementCertificate.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementCertificate.java#L12-L48)

### 九种成就类型一览

| 实体 | 表名 | 主要领域字段 | 特色字段 |
|------|------|------------|---------|
| `AchievementPaper` | `achievement_papers` | `paper_title` | `journal_name`、`publish_date`、`author_order`、`indexed` |
| `AchievementCertificate` | `achievement_certificates` | `certificate_name` | `certificate_type`、`obtain_date` |
| `AchievementContest` | `achievement_contests` | `contest_name` | `organizer`、`contest_category`、`award_category`、`award_level`、`contest_type`、`award_date`、`award_count`、`team_members`、`instructors` |
| `AchievementPatent` | `achievement_patents` | `patent_name` | `patent_type`、`grant_no`、`grant_date`、`first_inventor` |
| `AchievementResearch` | `achievement_researches` | `project_name` | `teacher_no`、`project_leader` |
| `AchievementJournal` | `achievement_journals` | `work_title` | `publication_name`、`publish_date` |
| `AchievementDoubleHundred` | `achievement_double_hundreds` | `project_name` | `project_category`、`project_domain`、`project_leader`、`leader_student_no`、`education_level`、`team_members`、`instructors`、`team_size`、`planned_level`、`college`、`final_level` |
| `AchievementIeerTraining` | `achievement_ieer_trainings` | `project_name` | `college_name`、`project_type`、`project_leader`、`instructor_name`、`recommended_level`、`is_key_area`、`final_status` |
| `AchievementWorks` | `achievement_works` | `work_name` | `work_category`、`work_type`、`publish_date`、`publish_occasion`、`organizer`、`impact_scope` |

`AchievementContest` 和 `AchievementDoubleHundred` 是字段最多的实体，各有 18+ 个字段。`AchievementResearch` 和 `AchievementCertificate` 最简洁。

> 来源：[AchievementContest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java#L1-L219)、[AchievementDoubleHundred.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementDoubleHundred.java#L1-L240)、[AchievementIeerTraining.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementIeerTraining.java#L1-L196)、[AchievementWorks.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementWorks.java#L1-L186)、[AchievementPatent.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementPatent.java#L1-L164)、[AchievementJournal.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementJournal.java#L1-L142)、[AchievementResearch.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementResearch.java#L1-L141)、[AchievementCertificate.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementCertificate.java#L1-L142)

## 审核工作流实体

### `AchievementReviewRequest` — `achievement_review_requests` 表

成就审批工作流的中心实体。它将**待审变更捕获为序列化 JSON**，而非直接修改目标记录。

| 字段 | Java 类型 | 描述 |
|------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `requester_id` | FK → `AppUser` | 谁提交了请求（贪婪） |
| `reviewer_id` | FK → `AppUser` | 分配的审核人（贪婪，可空直到分配） |
| `category` | `String` | 成就类型区分符（如 "paper"、"contest"） |
| `action` | `String` | "CREATE" 或 "UPDATE" |
| `status` | `String` | "PENDING"、"APPROVED" 或 "REJECTED" |
| `record_id` | `Long` | 到目标成就记录的 FK（CREATE 时为空） |
| `title` | `String` | 人类可读的请求摘要 |
| `summary` | `String` | 可选详细描述 |
| `payload_json` | `String` (LONGTEXT) | 完整提议实体数据为 JSON |
| `payload_snapshot_json` | `String` (LONGTEXT) | 变更前快照为 JSON |
| `changes_json` | `String` (LONGTEXT) | 结构化字段级差异 |
| `rejection_reason` | `String` (TEXT) | 驳回原因 |
| `supporting_documents_json` | `String` (TEXT) | 上传证明文档引用的 JSON 数组 |
| `created_at` | `LocalDateTime` | 请求提交时间 |
| `updated_at` | `LocalDateTime` | 最后状态变更时间 |

`category` + `action` + `status` 三元组形成**状态机**。`record_id` 列可为空，专门用于支持 CREATE 请求（此时目标记录尚不存在）。

> 来源：[AchievementReviewRequest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementReviewRequest.java#L7-L186)

### `ProfileReviewRequest` — `profile_review_requests` 表

学生档案修改的结构类似但**更简单**的对应实体。它省略了 `category` 和 `action` 字段，因为档案修改始终是对单一已知实体的更新，且仅使用 `payload_snapshot_json` + `changes_json`（无 `payload_json`）。

| 字段 | Java 类型 | 描述 |
|------|----------|------|
| `id` | `Long` | PK, AUTO_INCREMENT |
| `requester_id` | FK → `AppUser` | 提交修改的学生（贪婪） |
| `reviewer_id` | FK → `AppUser` | 分配的审核人（贪婪，可空） |
| `status` | `String` | "PENDING"、"APPROVED" 或 "REJECTED" |
| `title` | `String` | 请求描述 |
| `summary` | `String` | 可选详情 |
| `payload_snapshot_json` | `String` (LONGTEXT) | 请求时档案的完整快照 |
| `changes_json` | `String` (LONGTEXT) | 变更字段的结构化差异 |
| `rejection_reason` | `String` (TEXT) | 驳回说明 |
| `supporting_documents_json` | `String` (TEXT) | 证明文件引用 |
| `created_at` | `LocalDateTime` | 提交时间戳 |
| `updated_at` | `LocalDateTime` | 最后更新时间戳 |

> 来源：[ProfileReviewRequest.java](backend/src/main/java/com/gcsc/studentcenter/entity/ProfileReviewRequest.java#L7-L141)

## 系统配置

### `SystemSetting` — `system_settings` 表

主键是设置键本身（`String`），而非自增整数的**键值存储**实体。

| 字段 | Java 类型 | 描述 |
|------|----------|------|
| `setting_key` | `String` | PK, 最大 64, 唯一配置标识符 |
| `setting_value` | `String` (TEXT) | NOT NULL, 任意配置值（常为 JSON） |
| `updated_at` | `LocalDateTime` | NOT NULL, 最后修改时间戳 |

> 来源：[SystemSetting.java](backend/src/main/java/com/gcsc/studentcenter/entity/SystemSetting.java#L10-L47)、[SystemSettingRepository.java](backend/src/main/java/com/gcsc/studentcenter/repository/SystemSettingRepository.java#L6-L8)

## Repository 层模式

### 查询派生约定

Spring Data JPA 的查询派生被广泛使用。成就 Repository 遵循 `findAllByAuthor_UsernameOrderByCreatedAtDesc` 模式用于个人记录列表。审核请求 Repository 使用：`findAllByRequester_UsernameOrderByUpdatedAtDesc`。

### 获取策略汇总

| 关系 | 获取类型 | 理由 |
|------|---------|------|
| Achievement → `AppUser`（作者） | **EAGER** | 渲染成就记录时几乎总是需要作者信息 |
| Review → `AppUser`（请求者/审核人） | **EAGER** | 审核列表必须显示谁提交了请求、谁在审核 |
| `StudentProfile` → 子经历 | **LAZY** | 经历仅在查看特定档案详情页时加载 |
| `LoginHistory` → `AppUser` | **无**（普通 `Long` FK） | 登录日志独立查询 |

### StudentProfileRepository 中的高级 JPQL

最复杂的查询是 `searchProfiles`，一个多参数动态搜索，连接 `StudentProfile` 与 `AppUser`，应用 13 个可选过滤参数，使用 `COALESCE` 在 8 个不同字段中进行关键词搜索。此查询还展示了 `allowedClassNames` 模式，其中教师级访问被限定在一组特定班级内。

> 来源：[AchievementPaperRepository.java](backend/src/main/java/com/gcsc/studentcenter/repository/AchievementPaperRepository.java#L8-L19)、[AchievementReviewRequestRepository.java](backend/src/main/java/com/gcsc/studentcenter/repository/AchievementReviewRequestRepository.java#L8-L11)、[StudentProfileRepository.java](backend/src/main/java/com/gcsc/studentcenter/repository/StudentProfileRepository.java#L15-L76)

## 数据序列化策略

一个值得注意的架构决策是对复杂多值数据使用**原始 JSON-in-TEXT 列**而非规范化连接表。

| 实体 | 字段 | 内容格式 |
|------|------|---------|
| 全部 9 种成就类型 | `_image_urls`、`_attachments` | URL 字符串 / 文件元数据对象的 JSON 数组 |
| `AchievementReviewRequest` | `payload_json`、`payload_snapshot_json`、`changes_json`、`supporting_documents_json` | JSON 对象和数组 |
| `ProfileReviewRequest` | `payload_snapshot_json`、`changes_json`、`supporting_documents_json` | JSON 对象和数组 |
| `AppUser` | `assigned_classes` | 逗号分隔字符串（非 JSON） |

这种方法以可查询性换取简化的 CRUD 操作和模式灵活性。Service 层通过 Jackson 的 `ObjectMapper` 透明处理序列化/反序列化。

---

**下一步**：数据模型基础建立后，通过 [REST API 设计](./rest-api) 了解这些实体如何通过 API 暴露，或通过 [九种成就类型](./achievement-system) 了解成就实体如何驱动多态 Service。