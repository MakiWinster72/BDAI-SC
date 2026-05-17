# 成就编辑器组合式函数

## 概述


`useAchievementEditor` 是驱动 BDAI-SC 中成就创建与编辑体验的核心状态机。

## 架构概览

三个关注点的交汇处：
- Schema 驱动的表单渲染
- 媒体上传编排
- 带条件审核路由的持久化

## 模块级单例状态

| 符号 | 类型 | 用途 |
|------|------|------|
| editorOpen | ref(false) | 控制编辑器面板的可见性 |
| editId | ref(null) | 编辑模式标识 |
| hintCollapsed | ref(false) | 切换分类提示面板 |
| form | reactive({...}) | 持有 imageUrl、imageUrls[]、attachments[]、category、fields{} |

## 编辑器生命周期方法

| 方法 | 用途 |
|------|------|
| openEditorForCategory(activeCategory) | 初始化新成就创建 |
| openEditorForEdit(item) | 初始化现有成就编辑 |
| applyFieldDefaults() | 确保必填字段存在 |

## 媒体上传管道

### 图片上传流程

1. 容量检查
2. 类型验证
3. 大小验证
4. 上传与追加

### 附件上传流程

附件对象包含 `url`、`name`、`mediaType` 字段，用于下游渲染决定器。

## 保存与审核提交

`saveAchievement()` 结合了：
- 标题验证
- 媒体序列化
- 条件审核路由
- 乐观列表更新

## 与配套组合式函数的关系

| 组合式函数 | 职责 | 共享状态 |
|------------|------|----------|
| useAchievementEditor | 表单状态、验证、保存逻辑 | form、editorOpen、editId |
| useAchievementUpload | 上传限制配置、文件验证 | imageInput、attachmentInput |
| useAchievementList | 成就列表获取、过滤 | achievements |

## 后续步骤

- [多实体成就系统](./achievement-system) — 类别 Schema
- [审核与批准工作流](./review-workflow) — 审核路由
