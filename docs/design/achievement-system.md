# 九种成就类型

GCSC 成就系统建模了**九种不同类别**的学生成就，每种由专属的 JPA 实体、专属的数据库表以及完全独立的一组表单字段、校验规则和展示逻辑支撑。系统未采用带类型区分符的单表多态设计，而是采用**每类型一张具体表**策略：每种类别拥有自己的实体类、Repository 和序列化逻辑，由单一的 `AchievementService` 通过类型字符串分发来协调。本文记录每种类型及其特定 Schema，以及将其呈现到 UI 的渲染管道。

> 来源：[AchievementService.java](backend/src/main/java/com/gcsc/studentcenter/service/AchievementService.java#L17-L30)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L19-L32)

## 类型分类一览

九种成就类别自然聚为三组。**学术发表**类型捕获正式研究成果。**竞赛与资质**类型记录奖项类和资质类成就。**项目型**类型追踪结构化 institutional 项目的多字段元数据。

以下表格汇总每种类型的后端实体、数据库表、领域字段数量和用于卡片标题及列表头的首要标识字段。

| 类型 Key | 中文名称 | 实体类 | 数据库表 | 领域字段数 | Title 字段 |
|---------|---------|--------|---------|-----------|-----------|
| `contest` | 学科竞赛、文体艺术 | `AchievementContest` | `achievement_contests` | 13 | `contestName` |
| `paper` | 发表学术论文 | `AchievementPaper` | `achievement_papers` | 6 | `paperTitle` |
| `journal` | 发表期刊作品 | `AchievementJournal` | `achievement_journals` | 4 | `workTitle` |
| `patent` | 专利授权 | `AchievementPatent` | `achievement_patents` | 6 | `patentName` |
| `certificate` | 职业资格证书 | `AchievementCertificate` | `achievement_certificates` | 5 | `certificateName` |
| `research` | 参与教师科研项目 | `AchievementResearch` | `achievement_researches` | 5 | `projectName` |
| `works` | 代表性作品 | `AchievementWorks` | `achievement_works` | 7 | `workName` |
| `doubleHundred` | 双百工程 | `AchievementDoubleHundred` | `achievement_double_hundreds` | 14 | `projectName` |
| `ieerTraining` | 大创训练计划 | `AchievementIeerTraining` | `achievement_ieer_trainings` | 10 | `projectName` |

> 来源：[achievementConstants.js](frontend/src/constants/achievementConstants.js#L19-L32)、[AchievementContest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java#L8-L10)、[AchievementDoubleHundred.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementDoubleHundred.java#L8-L9)

## 共享实体骨架

尽管领域各异，所有九种实体类共享一组完全相同的**结构化跨领域字段**。这种一致性使 `AchievementService` 能通过一组重载的 `toResponse()` 方法应用统一的逻辑——所有权检查、媒体处理和响应序列化。

每个实体公开以下通用列：

| 字段 | 类型 | 用途 |
|------|------|------|
| `id` | `Long`（自增） | 主键 |
| `author` | `ManyToOne → AppUser` | 记录所有者，强制非空 |
| `student_no` | `String(32)` | 录入时的学生证号 |
| `student_name` | `String(64)` | 学生显示名 |
| `image_url` | `String(255)` | 旧版单图字段 |
| `_image_urls` | `TEXT`（JSON 数组） | 多图图库存储 |
| `_attachments` | `TEXT`（JSON 数组） | 文件附件元数据 |
| `created_at` | `LocalDateTime` | 强制非空的记录创建时间戳 |

双图字段（`image_url` 和 `_image_urls`）表明了从单图到多图支持的迁移路径，其中旧字段为向后兼容而保留。附件序列化为包含 `{name, url}` 对象的 JSON 数组，存储在 TEXT 列中而非连接表——这是有意为之的简单性权衡。

> 来源：[AchievementContest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java#L12-L69)、[AchievementJournal.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementJournal.java#L12-L48)

## 类型深度解析：领域字段

### Contest（学科竞赛、文体艺术）——字段最丰富的 Schema

竞赛类型是最字段密集的类别，反映了学术竞赛记录的复杂性。它捕获竞赛的完整生命周期：事件身份、主办方来源、奖项层级和团队构成。后端实体在共享骨架之外映射了 13 个领域特定列，使 `AchievementContest` 成为系统内最大的实体。

关键字段包括 `contest_name`（赛事官方全称）、`organizer`（第一主办单位）、两级奖项分类（`contest_category` 用于范围如国家/省/校级，`award_category` 用于具体奖项等级）、`award_level` 获奖等次、`contest_type` 竞赛平台分类（互联网+/挑战杯/创青春/其他）、作为自由文本存储的 `team_members`（含排序说明）以及指导教师的 `instructors`。`remark` 字段用作团队/个人区分符。

> 来源：[AchievementContest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java#L26-L57)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L96-L199)

### Paper（发表学术论文）和 Journal（发表期刊作品）

两种类型捕获出版产出，但粒度不同。论文实体追踪 `paper_title`、`journal_name`、`author_order`（作者列表中的排名）和 `indexed`（SCI/EI/核心期刊收录状态）。期刊实体刻意更简单：记录 `work_title`、`publication_name` 和 `publish_date`——专为正式出版物上的文学或艺术作品设计，而非同行评审研究。两者共享相同的时态字段（`publishDate`）和 `studentName`/`studentNo` 身份配对。

> 来源：[AchievementPaper.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementPaper.java#L26-L42)、[AchievementJournal.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementJournal.java#L26-L36)

### Patent（专利授权）

专利实体用 `patent_name`、`patent_type`（发明/实用新型/外观设计/软件著作权）、`grant_no`（官方授权号）、`grant_date` 和 `first_inventor` 建模知识产权。UI 中显示的标签动态派生自 `patentType`——当类型值存在时显示类型标签，否则回退到通用"专利"文字。

> 来源：[AchievementPatent.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementPatent.java#L26-L42)、[achievementDetailSchemas.js](frontend/src/constants/achievementDetailSchemas.js#L45-L50)

### Certificate（职业资格证书）

这是最简单的资质类型，仅捕获 `certificate_name`、`certificate_type` 和 `obtain_date`。它面向中国国家职业资格目录中列出的专业资质。最简 Schema 反映了记录的本身特性：资质独立于团队或组织上下文而存在。

> 来源：[AchievementCertificate.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementCertificate.java#L26-L36)

### Research（参与教师科研项目）

科研实体将学生成就与教师工作连接起来。它记录 `project_name`、`teacher_no`（指导教师的员工号）和 `project_leader`（教师姓名）。值得注意的是，后端实体缺少前端表单定义中存在的 `projectLevel` 和 `projectRole` 字段——这表明存在 Schema 漂移：前端表单收集的数据多于后端持久化的数据，这种模式可能代表计划中的未来扩展。

> 来源：[AchievementResearch.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementResearch.java#L25-L34)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L318-L378)

### Works（代表性作品）

作品实体用丰富的元数据集捕获创意产出：`work_name`、`work_category`（理论/创作/表演）、`work_type`（根据时长和技术复杂度分大/中/小规模）、`publish_date`、`publish_occasion`（平台或场所）、`organizer` 和 `impact_scope`（国家/地区/省级）。这是唯一使用 `note` 而非 `remark` 作为自由文本字段的类型——从后端列名到前端表单键存在命名不一致。

> 来源：[AchievementWorks.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementWorks.java#L26-L48)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L379-L427)

### Double Hundred（双百工程）——团队密集型项目类型

`AchievementDoubleHundred` 是仅次于竞赛的第二复杂实体，有 14 个领域字段。它建模一个结构化大学项目，捕获大量团队元数据：`project_category`、`project_domain`、`project_name`、`project_leader`、`leader_student_no`、`education_level`（本科/研究生）、`team_members`、`instructors`、`team_size`、`planned_level`（目标资助等级）、`college`、`final_level`（项目结题评级）和 `remark`。前端表单使用系统唯一的 `radio` 输入类型来处理 `educationLevel`（本科/研究生），使其成为唯一具有有界选择字段而非自由文本的类型。

> 来源：[AchievementDoubleHundred.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementDoubleHundred.java#L25-L62)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L428-L526)

### IEER Training（大创训练计划）——制度工作流类型

`AchievementIeerTraining` 实体追踪创新创业教育训练计划参与情况。其字段编码了正式的制度工作流：`college_name`、`project_name`、`project_type`（创新训练/创业训练/创业实践）、`project_leader`、`instructor_name`、`recommended_level`、`is_key_area`（项目是否被推荐为重点领域支持）以及 `final_status`（优秀/通过/延期/终止）。与双百工程一样，它使用 `radio` 输入处理 `isKeyArea`（是/否）。

> 来源：[AchievementIeerTraining.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementIeerTraining.java#L25-L50)、[achievementConstants.js](frontend/src/constants/achievementConstants.js#L527-L589)

## 前端表单生成：类别字段映射

前端在 [achievementConstants.js](frontend/src/constants/achievementConstants.js) 的 `categoryFieldMap` 对象中集中了所有类型特定表单定义。每个类别条目定义：

- **`fields`**：有序字段描述符数组，驱动动态表单生成。每个描述符有 `key`、`label`、`kind`（当前始终为 `"input"` 或 `"radio"`）、`placeholder`，以及可选的 `type`（`"date"`）和 `options`（用于 radio 类型）。
- **`titleKey`**：其值成为卡片和列表中成就显示标题的字段键。
- **`dateKey`**：用于按时间排序和日期显示的字段键。

每个类型的字段数组**前置** `studentNo` 和 `studentName`（`ieerTraining` 除外，其用 `collegeName` 替代），在所有表单布局中建立一致的标识头部。配套的 `categoryHints` 对象提供类型特定的指导性文本，在用户输入表单时显示，内容来自制度性成就报告指南。

> 来源：[achievementConstants.js](frontend/src/constants/achievementConstants.js#L63-L590)

## 渲染管道：从实体到可视化卡片

渲染管道通过两个阶段将扁平字段映射转换为类型特定的视觉表示：**卡片主体渲染**（列表视图）和**明细渲染**（展开视图）。

`AchievementCardBody.vue` 组件使用以 `item.category` 为键的 `v-if` / `v-else-if` 链来渲染类型特定的卡片布局。每个分支调用 [achievementFormatters.js](frontend/src/utils/achievementFormatters.js) 中的专用格式化函数——`formatContestAwardPill` 用于奖项徽章、`formatContestMembers` 用于团队构成、`formatPaperAuthors` 用于作者排名、`formatWorksCategory` 用于创意元数据。没有专用分支的类型（`doubleHundred`、`ieerTraining`）回退到通用渲染器，显示 `item.previewFields` 和 `item.dateLabel`/`item.dateValue`。

> 来源：[AchievementCardBody.vue](frontend/src/components/AchievementCardBody.vue#L1-L170)、[achievementFormatters.js](frontend/src/utils/achievementFormatters.js#L1-L68)

## 明细视图：自定义组件 vs Schema 驱动渲染

`AchievementDetailRenderer.vue` 中的展开明细视图采用**混合渲染策略**。四种类型获得带专用布局的自定义 Vue 组件，其余五种类型通过声明式 Schema 驱动的共享 `AchievementDetailSchemaContent` 组件渲染。

| 渲染策略 | 涵盖类型 | 组件 |
|---------|---------|------|
| 自定义组件 | `contest`、`works`、`doubleHundred`、`ieerTraining` | `details/` 下的独立 `.vue` 文件 |
| Schema 驱动 | `paper`、`journal`、`patent`、`certificate`、`research` | `AchievementDetailSchemaContent.vue` |

Schema 驱动方法，定义在 [achievementDetailSchemas.js](frontend/src/constants/achievementDetailSchemas.js)，使用声明式结构——`groups` 包含 `rows`，每行有 `label` 和 `value` 函数。每个 `value` 是一个闭包，接收 item 的 `fields` 对象并使用 `text()` 和专用格式化函数返回格式化字符串。`hidden` 选项允许条件可见性——例如，论文 Schema 中的 `indexed` 行在没有索引信息时隐藏。这种双策略平衡了复杂类型（竞赛的多段奖项展示、作品的类别/规模元数据）对丰富自定义布局的需求与较简单、较统一类型的 Schema DSL 可维护性。

> 来源：[AchievementDetailRenderer.vue](frontend/src/components/achievement/AchievementDetailRenderer.vue#L30-L54)、[achievementDetailSchemas.js](frontend/src/constants/achievementDetailSchemas.js#L1-L98)

## `research` 类型中的 Schema 漂移

添加新成就类型时，完整变更面跨越**7 个接触点**：创建新 JPA 实体 + Repository、在 `AchievementService` 中添加 `save*`/`update*`/`toResponse*`/`load*`/`fetch*ByStudent` 方法、在 `achievementConstants.js` 中添加 `categoryFieldMap` 条目和 `categoryHints`、在 `AchievementCardBody.vue` 中添加展示分支，以及在 `achievementDetailSchemas.js` 中添加自定义组件或 Schema 条目。Schema 驱动路径对于具有统一键值展示需求的类型来说代价显著更低。

`research` 类型表现出**前端-后端 Schema 漂移**：前端 `categoryFieldMap.research` 定义了 `projectLevel` 和 `projectRole` 字段，但 `AchievementResearch` 实体没有对应列。进入这些字段的数据在持久化时被静默丢弃。这不是 bug，而是功能实现不完整的指示——可能是计划中的科研实体扩展，先在前端开始但尚未贯彻到数据库层。

---

**下一步**：要了解这九种类型如何通过 API 层提供服务以及 CRUD 操作如何分发，继续阅读 [成就服务多态](/9-achievement-service-polymorphism)。要了解上传媒体和附件如何与每种类型集成，参见 [文件上传与媒体管理](/10-file-upload-and-media-management)。要了解这些实体属于的更广泛数据模型，查阅 [数据模型与 JPA 实体](./student-profile-model)。