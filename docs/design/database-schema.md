# 数据库 Schema 约定

## 概述


本页面记录了 BDAI-SC 数据库中每张表的结构模式、命名规则以及 JPA 映射约定。

## 模式概述

数据库包含 22 张表，划分为四个逻辑域：核心、成果、工作流、社交/配置。

## 命名约定

### 表名

所有表名均使用 snake_case 复数形式，通过 `@Table(name = "...")` 显式映射。

| Java 实体 | 数据库表 | 领域 |
|-----------|----------|------|
| AppUser | users | 核心 |
| StudentProfile | student_profiles | 核心 |
| AchievementContest | achievement_contests | 成果 |
| AchievementPaper | achievement_papers | 成果 |
| AchievementReviewRequest | achievement_review_requests | 工作流 |
| ProfileReviewRequest | profile_review_requests | 工作流 |
| EducationExperience | education_experiences | 个人档案子实体 |
| CadreExperience | cadre_experiences | 个人档案子实体 |
| LoginHistory | login_histories | 审计 |
| SystemSetting | system_settings | 配置 |

### 列名

所有列均使用 snake_case，通过 `@Column(name = "...")` 显式声明。

| 约定 | Java 字段 | 数据库列 |
|------|-----------|----------|
| 复合词 | displayName | display_name |
| 缩写词小写 | imageUrl | image_url |
| 下划线前缀（内部） | imageUrls | _image_urls |
| 布尔值前缀 | offCampusLiving | off_campus_living |
| 外键引用 | author | author_id |

## 主键与标识策略

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

唯一例外是 `SystemSetting`，使用自然字符串键 `setting_key` 作为主键。

## 列类型映射

| Java 类型 | MySQL 类型 | 备注 |
|-----------|------------|------|
| Long | bigint(20) | 主键和外键 |
| String（名称） | varchar(64) | display_name, student_name |
| String（标题） | varchar(255) | contest_name, certificate_name |
| String（编码） | varchar(32) | student_no, phone |
| String（长文本） | TEXT / LONGTEXT | JSON 载荷、team_members |
| LocalDateTime | datetime(6) | 时间戳 |
| LocalDate | date | 日期字段 |
| Boolean | bit(1) | is_current, off_campus_living |
| Enum | varchar(16) | @Enumerated(EnumType.STRING) |

## 成果实体骨架

所有九个成果实体共享通用结构：

| 字段 | 数据库列 | 类型 | 用途 |
|------|----------|------|------|
| id | id | bigint(20) | 主键 |
| author | author_id | bigint(20) | 外键 → users |
| studentNo | student_no | varchar(32) | 反规范化学号 |
| studentName | student_name | varchar(64) | 反规范化姓名 |
| imageUrl | image_url | varchar(255) | 主图片 URL |
| _imageUrls | _image_urls | TEXT | JSON 数组 |
| _attachments | _attachments | TEXT | JSON 数组 |
| createdAt | created_at | datetime(6) | 创建时间戳 |

## 关系映射模式

### 一对一：User ↔ StudentProfile

```java
@OneToOne
@JoinColumn(name = "user_id", nullable = false, unique = true)
private StudentProfile profile;
```

### 带级联的一对多：Profile → Experiences

```java
@OneToMany(mappedBy = "profile", orphanRemoval = true, cascade = CascadeType.ALL)
private List<EducationExperience> educationExperiences;
```

### 多对一：Achievements → User（立即获取）

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "author_id", nullable = false)
private AppUser author;
```

## 审核工作流表

| 列 | 类型 | 用途 |
|----|------|------|
| requester_id | bigint(20) FK | 提交用户 |
| reviewer_id | bigint(20) FK | 审核用户（可为空） |
| status | varchar(16) | PENDING / APPROVED / REJECTED |
| payload_snapshot_json | LONGTEXT | 完整 JSON 快照 |
| changes_json | LONGTEXT | 差异描述 |
| rejection_reason | TEXT | 拒绝原因 |

## 模式迁移与 DDL 策略

`ddl-auto: update` 的作用：
- ✅ 创建新表
- ✅ 添加新列
- ✅ 加宽现有列
- ❌ 不删除列或表
- ❌ 不重命名列
- ❌ 不更改列类型

::: warning 生产环境
生产环境应使用 Flyway 或 Liquibase 进行模式迁移。
:::

## 约定检查清单

| 规则 | 验证方式 |
|------|----------|
| 表名 snake_case 复数 | @Table(name = "...") |
| 主键 Long + IDENTITY | @Id @GeneratedValue |
| 列名显式声明 | @Column(name = "snake_case") |
| 外键命名 {role}_id | author_id, user_id, profile_id |
| 枚举 @Enumerated(EnumType.STRING) | 不使用序数 |
| 媒体字段 _前缀 | _image_urls, _attachments |
| 子实体级联 | CascadeType.ALL + orphanRemoval |

## 后续步骤

- [学生档案数据模型](./student-profile-model) — 档案实体及其子集合
- [多实体成就系统](./achievement-system) — 完整的成果架构
- [快速开始](./getting-started) — 环境设置
