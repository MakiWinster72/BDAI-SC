# TODO: opti 分支改进计划

本计划基于 `opti` 分支重新审查生成，不沿用已放弃的 `css` 分支判断。

当前基线：
- 前端 `npm --prefix frontend run build` 已通过。
- 后端 `mvn test` 未完成验证：沙箱无法解析 `repo.maven.apache.org`，不是代码编译结论。
- 项目实际前端栈是 Vue 3 + Vite + JavaScript + 自研 CSS，没有 TypeScript、Tailwind、Element Plus、Pinia。

核心原则：
- 先修真实风险，不做“看起来很现代”的大迁移。
- 后端先堵安全洞，前端先收口重复实现。
- 每次改动都必须能回答“为什么现在做它”。

## 1. 收紧上传附件访问权限

状态：已完成基础修复。`/uploads/**` 静态公开入口已移除，上传返回值已改为 `/api/media/uploads/...`，头像、成果与审核材料均走登录鉴权和权限校验。

### 任务
- 移除或限制 `SecurityConfig.java` 中 `/uploads/**` 的匿名访问。
- 梳理 `UploadService.java` 返回的 `/uploads/{userId}/...` URL。
- 成就材料、审核材料统一走 `MediaController.java` 这类受控接口。
- 头像也不匿名公开，统一使用受控媒体接口读取。
- 前端同步调整 `utils/media.js`、成果预览、审核材料预览的 URL 解析逻辑。

### 原因
现在后端一边有 `MediaController` 做权限校验，一边又把 `/uploads/**` 静态公开，这等于绕过鉴权。审核材料和成果附件可能包含隐私或证明文件，这是优先级最高的安全问题。

## 2. 修复学生档案对象级权限

状态：已完成基础修复。`GET /api/student-profiles/{id}` 已按本人、管理员、负责班级教师、本班班干部校验；`/api/student-profiles/search` 已限制普通学生只能搜索到本人，教师和班干部仍按班级范围过滤。

### 任务
- 为 `GET /api/student-profiles/{id}` 增加操作者参数和权限判断。
- 明确访问规则：学生只能看自己，班干部只能看本班，教师只能看负责班级，管理员可看全部。
- 收紧 `/api/student-profiles/search`：普通学生不能分页枚举全校学生。
- 将班级范围判断抽成统一服务，供档案、审核、通知等模块复用。
- 越权访问返回 403，不要继续用普通业务异常混淆。

### 原因
学生档案包含证件号、电话、家长信息等敏感数据。只要登录就能按 ID 读取他人档案，这是典型 IDOR，风险高于任何前端重构。

## 3. 让审核开关真正约束写接口

状态：已完成基础修复。档案审核开启时，学生和班干部不能通过 `PUT /api/student-profiles/me` 直接落库；成果审核开启时，学生和班干部不能通过 `POST/PUT/DELETE /api/achievements/**` 直写。审核通过后的落库改走专用内部方法，避免被直写拦截误伤。

### 任务
- 检查 `PUT /api/student-profiles/me` 是否在档案审核开启时允许学生直写。
- 检查 `POST/PUT/DELETE /api/achievements/**` 是否在成果审核开启时允许学生直写。
- 审核开启时，普通学生和班干部的写入应统一进入审核请求；管理员保留必要的直接修改能力。
- 前端仍可根据审核配置展示不同交互，但后端必须是最终约束。

### 原因
审核不能只靠前端自觉。只要直写 API 仍然可用，用户就可以绕过 UI 直接提交请求，审核流程就只是装饰。

## 4. 统一后端授权模型

状态：已完成基础修复。`AchievementController`、`SystemSettingsController`、`AdminController` 已改为使用 Spring Security `Authentication`，不再在 Controller 中手动解析 JWT；成就对象权限改为读取数据库中的当前用户角色，避免旧 token 的 role claim 决定敏感授权；已启用 `@EnableMethodSecurity` 作为后续方法级授权入口。

### 任务
- 优先使用 `SecurityContext` / `Authentication` 获取当前用户，不再在 Controller 中重复解析 JWT。
- 敏感授权以数据库中的当前角色为准，不把 JWT claim 里的角色当最终权限来源。
- 引入 `@EnableMethodSecurity` 或集中式 `AuthorizationService`，逐步替代分散的 `if (role == ...)`。
- 扩展 `GlobalExceptionHandler.java`，统一处理 401、403、404、409 和校验错误。

### 原因
当前权限判断散在 Controller 和 Service 中，有的信数据库，有的信 JWT claim。角色变更、旧 token、重复逻辑都会让权限行为难以审计。先统一授权来源，再谈细节优化。

## 5. 补后端核心安全测试

状态：已完成基础覆盖。已新增服务层安全单元测试，覆盖学生档案 IDOR、普通学生搜索范围、档案审核开启时禁止直写、成就审核开启时禁止直写、成就对象权限以数据库角色为准。

### 任务
- 用 Spring Boot + MockMvc 补最小安全回归测试。
- 首批覆盖上传私有文件访问、档案 IDOR、学生搜索越权、审核绕过、管理员权限边界。
- 为权限服务抽单元测试，避免每次靠手动点页面验证。
- 修复 Maven 本地仓库和 CI 环境，让 `mvn test` 成为可重复命令。

### 原因
现在 `backend/src/test` 为空。权限和审核一旦改错，不一定会在页面上立刻暴露，但会直接影响数据安全。P0 安全修复没有测试保护，就是换一种方式埋雷。

## 6. 收口学生档案前端重复实现

状态：已完成第二阶段基础收口。已把学生档案表单共用选项抽到 `frontend/src/constants/profileOptions.js`，头像上传入口抽到 `frontend/src/composables/useProfileAvatarUpload.js`，并新增 `frontend/src/composables/useProfileFormModel.js` 收口表单初始状态、经历行创建、空行判断、经历 payload 映射和后端经历数据规范化。`MyInfosView.vue` 与 `StudentProfileEditor.vue` 已共享这些基础规则。剩余大块表单区块拆分应继续小步推进，不能一次性重写。

### 任务
- 先确认 `MyInfosView.vue` 和 `StudentProfileEditor.vue` 的职责边界。
- 建议组件边界：
  - `MyInfosView.vue`：只负责页面装配、加载当前用户档案、提交审核或保存。
  - `StudentProfileEditor.vue`：负责档案表单 UI，不直接决定页面级路由和菜单。
  - `useProfileFormModel.js`：负责表单状态、地址级联、宿舍拼接、证件规则、经历行处理和保存 payload。
  - `ProfileBasicSection.vue`、`ProfileDormSection.vue`、`ProfilePartySection.vue`、`ProfileFamilySection.vue`、`EducationExperienceTable.vue`、`CadreExperienceTable.vue`：负责独立表单区块。
- 数据流采用 props down / events up：父级传入初始档案和编辑状态，子组件通过事件提交变更。
- watch 只保留副作用逻辑，字段派生优先使用 computed。

### 原因
`MyInfosView.vue` 和 `StudentProfileEditor.vue` 都接近 3000/2400 行，且大量字段、watch、保存逻辑重复。继续双轨维护会导致学生端、教师端、审核端表单规则逐渐不一致。

## 7. 决定成果 composable 的去留

状态：已完成。已确认 `useAchievementList.js`、`useAchievementEditor.js`、`useAchievementPreview.js` 没有被 `AchievementsView.vue` 或其他模块引用，且内部存在模块级单例状态，不适合直接接入多实例页面。当前选择删除这三份未使用 composable，保留已被页面真实使用的 `useAchievementUpload.js` 和 `useAchievementUploadSettings.js`。

### 任务
- 审查 `useAchievementList.js`、`useAchievementEditor.js`、`useAchievementPreview.js` 是否应该接入 `AchievementsView.vue`。
- 如果接入，先把模块级单例状态改为每次调用独立状态，避免多个页面实例互相污染。
- 如果短期不接入，删除这些未引用 composable，避免误导维护者。
- 推荐组件边界：
  - `AchievementsView.vue`：页面装配和路由 query 同步。
  - `AchievementListPanel.vue`：分类、列表、分页和空状态。
  - `AchievementEditorPanel.vue`：新增/编辑表单。
  - `AchievementDetailPanel.vue`：详情展示。
  - `AchievementMediaPreview.vue`：附件预览。

### 原因
当前最糟糕的状态不是“没抽象”，而是“抽了一半但没用”。未接入的 composable 会让后续开发者误判真实数据流，增加维护成本。

## 8. 统一班级审核入口

状态：已完成。已保留 `NotificationsView.vue` 的班级审核面板作为唯一真实入口，审核列表与处理操作继续统一走 `useNotifications.js`。已删除独立的 `ClassReviewsView.vue` 和旧的 `useClassReviews.js`，并把 `/class-reviews` 改为重定向到 `/notifications?panel=class-reviews&category=pending`，避免旧链接进入第二套状态。

### 任务
- 在 `NotificationsView.vue` 的班级审核面板和 `/class-reviews` 独立路由之间二选一。
- 如果保留通知面板模式，删除或合并 `ClassReviewsView.vue`、`useClassReviews.js`。
- 如果保留独立路由，菜单、路由和通知入口都指向同一套状态来源。
- 审核操作统一走 `useNotifications.js` 或拆出的审核状态模块。

### 原因
同一业务有两套入口和两套状态管理，最容易产生“一个页面审批了，另一个页面还显示待处理”的错觉。审核类功能必须保证状态来源唯一。

## 9. 拆分学生列表和后台管理巨型视图

状态：已完成。已新增 `frontend/src/composables/useStudentSearch.js`，把 `StudentInfoView.vue` 中的筛选状态、年级/学生类型/专业/特殊学生选项、港澳台互斥、重置逻辑、搜索参数构造和特殊学生标签解析收口到独立 composable。已拆出 `StudentFilterBar.vue`、`StudentListPanel.vue`、`StudentGridPanel.vue`，学生信息页现在主要保留数据加载、分页状态、表格数据组装、详情与导出。后台管理已拆出 `AdminUploadSettingsTab.vue`、`AdminReviewSettingsTab.vue`、`AdminSystemSettingsTab.vue`、`AdminUsersTab.vue`、`AdminBackupTab.vue`，`AdminView.vue` 现在主要保留状态、API 调用、弹窗和危险操作函数。

### 任务
- 拆 `StudentInfoView.vue`：
  - `StudentFilterBar.vue`
  - `StudentListPanel.vue`
  - `StudentGridPanel.vue`
  - `StudentProfileDrawer.vue`
  - `useStudentSearch.js`
- 拆 `AdminView.vue`：
  - `AdminUploadSettingsTab.vue`
  - `AdminReviewSettingsTab.vue`
  - `AdminUsersTab.vue`
  - `AdminBackupTab.vue`
  - `AdminSystemSettingsTab.vue`
- 每个子组件只暴露明确 props/emits，不通过父组件 ref 乱调用内部方法。

### 原因
这两个 View 都已经超过 2000 行。它们不是“页面”，而是多个小应用堆在一个文件里。拆分能降低认知成本，也能让后续权限、导出、筛选等逻辑更容易测试。

## 10. 清理前端重复 API、配置和状态源

状态：已完成。已删除重复的 `frontend/src/api/achievement.js`，统一从 `frontend/src/api/achievements.js` 导出成就 API；`loadUser()` 已统一走 `utils/userStorage.js`。`StudentInfoView.vue` 已全面复用 `utils/studentProfileExport.js`（`exportGroups`、`buildPreviewSheets`、`fetchAchievementsForStudents`、`checkStudentProfileNeedsDetail`、`ACHIEVEMENT_EXPORT_CATEGORIES` 等），表格视图与导出共用同一套字段与 sheet 构建逻辑。`useReviewSettings` 与 `useAchievementUploadSettings` 已改为模块级单例。

### 任务
- 合并 `api/achievement.js` 和 `api/achievements.js`。
- 统一使用 `utils/userStorage.js`，删除页面内重复的 `loadUser()`。
- 复用 `utils/studentProfileExport.js` 中的导出配置，不在 `StudentInfoView.vue` 再复制一份。
- 将高频业务判断逐步迁移到配置函数，例如 `checkXxxSupport()`、`getXxxConfig()`。
- 对 settings 类 composable 使用单一状态源，避免多个组件各自缓存配置。

### 原因
重复 API 和重复配置会让改动变成猜谜。一个字段名、一个权限规则、一个导出项，如果散落在多处，就一定会出现漏改。

## 11. 只做渐进式前端规范化

状态：路径别名已完成。`vite.config.js` 已配置 `@` → `src`，并新增 `frontend/jsconfig.json` 供 IDE 跳转。`src` 下 41 个文件的 `from "../..."` / `@import "../..."` 已统一为 `@/...`；同目录 `./` 引用保持不变。TypeScript / Pinia / Tailwind / Element Plus 整体迁移仍不做。

### 任务
- 先加 `@/` 路径别名，减少深层相对路径。
- 新增文件优先用更小的组件和 composable，不继续扩大巨型 View。
- 暂不整体迁移 TypeScript、Pinia、Tailwind、Element Plus。
- 如果后续迁移 TypeScript，从 `api/`、`constants/`、新 composable 开始，不从巨型 View 直接硬改。

### 原因
项目实际栈和目标规范差距很大。现在一口气迁移技术栈，会制造大量无关 diff，还会掩盖真正的业务风险。先把边界拆清楚，再迁移才有意义。

## 12. 建立最低验证标准

状态：已完成。`README.md` 已补充「开发与验证」：必跑命令表、环境变量与上传目录策略、公开接口与审核直写规则、9 类成果说明。`AGENTS.md` / `CLAUDE.md` 已同步修正（媒体鉴权、成就 API 非公开、后端安全测试、审核直写）。新增 `scripts/verify.sh` 一键执行 `npm --prefix frontend run build` 与 `mvn test`。

### 任务
- 每次前端改动后至少运行 `npm --prefix frontend run build`。
- 每次后端安全或权限改动后运行 `mvn test`。
- 在 README 或开发文档中写明必跑命令、环境变量、数据库依赖和上传目录策略。
- 修正文档里与代码不一致的内容，例如成果类型数量、接口认证状态、审核直写行为。

### 原因
现在前端能构建，但 bundle 已有体积警告；后端测试无法在当前环境完成。没有稳定验证标准，后续任何重构都会靠感觉合并，这就是项目再次被写坏的根源。
