---
layout: home

hero:
  name: 学生信息管理中心
  text: 大数据与人工智能学院
  tagline: BDAI-SC, 基于 Spring Boot + Vue3
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 使用手册
      link: /manual/

features:
  - icon:
      src: /icons/student.svg
    title: 学生档案管理
    details: 完整的个人信息、教育经历、干部经历、党团关系的记录与审核流程。
    link: /design/student-profile-model
    linkText: 查看档案模型
  - icon:
      src: /icons/achievement.svg
    title: 九类成果记录
    details: 竞赛、论文、专利、证书、科研、作品、双百、大创等学生荣誉的全生命周期管理。
    link: /design/achievement-system
    linkText: 了解成就系统
  - icon:
      src: /icons/auth.svg
    title: JWT 认证体系
    details: 基于 Spring Security + jjwt 的无状态认证，四级角色。
    link: /design/jwt-auth
    linkText: 认证流程详解
  - icon:
      src: /icons/review.svg
    title: 审核工作流
    details: 成就提交与档案修改均通过教师/管理员审核。
    link: /design/review-workflow
    linkText: 审核工作流
  - icon:
      src: /icons/fe.svg
    title: Vue 3 前端架构
    details: Composition API + Composable 模式，Dashboard Shell + 响应式设计。
    link: /design/dashboard-layout
    linkText: 前端架构
  - icon:
      src: /icons/be.svg
    title: Spring Boot 后端
    details: 分层架构（Controller / Service / Repository / Entity），JPA + MySQL。
    link: /design/architecture
    linkText: 后端架构
---

## 更新日志

### 2026-03-06
- docs: 补充提交信息中文描述规范
- docs: 补充项目启动与提交规范文档
- feat: 合并JWT注册登录完善功能
- feat: 合并前端认证模块
- feat: 合并登录态首页与路由守卫功能
- feat: 增加登录后首页与路由登录守卫
- feat: 完善注册登录鉴权并接入JWT会话
- feat: 完成前端注册登录页面与接口对接
- feat: 完成后端注册登录接口与MySQL集成
- fix: 修复JPA方言初始化并补充数据库排查说明
- style: 优化认证页面视觉与动画效果

### 2026-03-07
- docs: 补充前后端连接说明并忽略IDE文件
- style: 登录按钮使用纯色主色调
- style: 调整全局配色方案

### 2026-03-09
- chore: 切换JDK到21
- docs: 完善介绍，添加TODO以及页面设计预览

### 2026-03-10
- feat: 上传支持附件类型
- feat: 动态发布与媒体上传后端
- feat: 发布编辑面板与Markdown预览
- feat: 最大媒体/文件限制为200MB
- feat: 注册登录补充角色与档案字段
- feat: 添加video功能
- feat: 首页动态发布器与媒体展示
- fix: 修复图片无法显示，以及结尾重复添加到问题
- fix: 修复换行出现\<br\>的问题
- fix: 取消"发布动态"按钮的外框
- ignore: AGENTS and uploads folder
- style: publisher-footer不参与滚动
- style: 优化发布动态时的文本编辑框以及预览排版
- style: 细化发布动态页的出入动画
- style: 让卡片全宽

### 2026-03-11
- docs: 数据库说明文档
- feat: menu-card和profile-card改为sticky
- feat: 为个人成就添加编辑删除功能
- feat: 删除功能
- feat: 当用户聚焦输入时监控键盘快捷键方便加粗、斜体、缩进
- feat: 把个人成就连接到数据库
- feat: 新建student_profiles数据表
- feat: 添加myinfos的可填写项
- feat: 绑定myinfos连接到数据库
- fix: 为每个标签页分配相应的routes,home、congra、memory、achievements
- fix: 修复列表的使用逻辑，自动匹配序号，续行
- fix: 修复缩进无反应的问题
- fix: 修正头像问题，匹配所有routes下的头像到用户信息
- fix: 把文本工具栏移动到文本框上面
- fix: 统一路由设计，开放myinfos
- fix: 缩进与取消缩进不再自动选择文本
- pages: open 个人成就
- pages: open 个人记录
- style: 优化个人成就卡片的展示效果
- style: 优化成就添加页的排版

### 2026-03-12
- docs: 更新数据库描述，添加了足够的备注
- feat: 创建教师/部门表
- feat: 教师/部门联系方式添加搜索功能
- feat: 重新调整数据库结构，把avatar更换到users中，而不是student_profiles
- fix: 不单独区别教师/部门
- pages: open contacts 教师/部门联系方式

### 2026-03-13
- docs: 添加港澳台字段以及特殊学生字段的描述
- feat: 为移动端添加工具栏
- feat: 修改学生数据库表，添加hk、mo、tw
- feat: 删除社交部分，删除联系方式
- feat: 将student-info连接上数据库
- feat: 教师联系方式链接上数据库
- feat: 添加student-info下的详情页面
- feat: 添加教师卡片的查看卡片
- feat: 添加编辑/删除教师功能
- feat: 联系方式增加"添加"按钮
- feat: 限制喜报只允许老师/管理员发布
- fix: 修复publisher-sheet顶部margin错误
- fix: 修复下拉菜单无法关闭的bug
- fix: 修复下拉菜单过长问题
- fix: 修复合并后语法错误
- fix: 修复无法正常路由到contacts
- fix: 取消限制localhost访问后端
- fix: 提高下拉菜单的z轴
- fix: 默认展示结果卡片
- Merge branch 'feat/mobile'
- Merge branch 'page/contacts'
- Merge branch 'page/contacts' into page/student-infos
- Merge branch 'page/student-infos'
- pages: open student-info 学生信息
- style: 优化下拉菜单展示效果，提高不透明度，添加出现消失动画

### 2026-03-14
- feat: 根据正式资料增加更多信息字段
- fix: 修复下拉菜单为原生模式
- fix: 修复日期选择器在手机端无法显示的问题
- style: 优化我的信息页面排版
- style: 统一下拉菜单样式

### 2026-03-16
- docs: 添加注册登录的说明
- docs: 补充新的数据库字段说明
- feat: 在profile-card添加设置图标入口
- feat: 完善退出登录功能
- feat: 将学院和专业改为下拉列表
- feat: 将设置路由到settings
- fix: 修复阴影不再溢出界面
- fix: 修正信息填写顺序，年级学院班级
- fix: 注册时不再允许填写学号班级等信息
- fix: 班别数字改为步进器，防止填写错误
- Merge branch 'dev'
- Merge branch 'main' into feat/mobile
- Merge: dev 基本完成学生信息填写页面
- pages: open 后台管理 admin
- style: 取消菜单栏的阴影，取消描边
- style: 在menus-card中使用列表展示学术成就分类
- style: 统一后台管理的样式
- style: 设计设置页基本页面
- TODO: 明暗模式
- TODO: 注销账号

### 2026-03-18
- docs: 更新数据库描述
- feat: 为每一项成就添加专有的描述，分开每个成就添加的项
- feat: 为表格增加添加减少行功能和
- feat: 取消学院的选择，仅大数据与人工智能学院
- feat: 在info界面添加查询个人成就功能
- feat: 把各类成就连接到数据库 imp
- feat: 把教育经历接入数据库
- feat: 教育经历添加至今选项
- feat: 添加教育经历的前端设计
- feat: 设计好各分类点击筛选
- feat: 详情页对应myinfos
- fix: 不再固定每个筛选添加成就时的选项
- fix: 专业添加大数据管理与应用（佛山校区全学段）、大数据管理与应用（数字治理）
- fix: 修复政治面貌的选项：群众、共青团员、中共预备党员、中共党员
- fix: 修复筛选器
- fix: 修正错别字，"接受为预备党员" => "接收为预备党员"
- fix: 删除education-controls-wrap的背景
- fix: 删除右上角的分类小标签
- fix: 提示使用真实照片
- fix: 民族填写中固定族字，以防不规范填写
- fix: 添加前端去重保护，修复有时候会重复出现成就的bug
- fix: 演绎作品展示的achievement-view open不再显示tag
- fix: 筛选器删除学院
- Merge branch 'achievement-list' into page/student-infos
- Merge branch 'achievement-list' into page/student-infos
- Merge branch 'page/student-infos' into dev 合并学生信息以及个人成就
- Merge dev, 完成我的信息
- style: student-info添加背景
- style: 不再使用自定义下拉菜单样式
- style: 为menu-drawer open添加动画
- style: 为表格增加行减少行添加动画
- style: 优化创作、表演这部分成就的展示效果
- style: 优化学术论文的卡片展示
- style: 优化学科竞赛、文体艺术的展示效果
- style: 优化期刊作品、专利的展示效果
- style: 优化科研项目的展示效果
- style: 改善education-controls-wrap在宽度不足时的表现，加减行改在表格左下
- style: 改善科研项目卡片的展示效果，添加注释
- style: 统一"提示"的样式为斜体淡灰色
- style: 让achievement-list里面的图片限制最大宽度
- TODO: 修改密码
- TODO: 数据库备份与恢复

### 2026-03-19
- feat: 团组织与入党信息单独一个工作表
- feat: 在填写教育经历时如果勾选了"至今"，那么自动删除后续空行
- feat: 在筛选结果中添加两个按钮，选择本页和选择全部
- feat: 导出教育经历
- feat: 新增导出xlsx功能
- fix: 在点击确认后，"教育经历"不会回归默认5行
- fix: 教育经历自动过滤空行
- fix: 日历禁止选择未来时间
- fix: 注册和登录的"用户名"提示语改为"学号"
- fix: 筛选列表不再显示学院
- Merge branch 'page/myinfo' into feat/export
- Merge branch 'page/student-infos' into feat/export

### 2026-03-20
- feat: 为导出提供预览功能
- feat: 单独导出教育经历/团组织信息不分表
- feat: 在myinfo也添加导出按钮
- feat: 地址选择器
- feat: 学生筛选器添加每页条数调整
- feat: 导出学生信息pdf
- feat: 导出的表中可以点击表头跳转，成就
- feat: 校外居住增加地址选择器
- feat: 添加导出配置页面，可选择导出的字段
- feat: 添加表格试图
- feat: 现在可以导出成就了
- feat: 表格视图可选择字段功能
- fix: 修复合并错误，无法筛选出学生数据的bug
- fix: 修复导出按钮无反应的问题
- fix: 修复无法筛选班级的bug
- fix: 修复班级不更新旧值的bug
- fix: 团组织前面添加班级
- fix: 团组织空置为""而不是"-"
- fix: 导出的表格自动调整列宽
- fix: 导出预览显示仅前三个人
- fix: 总表中去掉年级/学院/专业/班级
- fix: 把港澳台和特殊学生的字段放在最后
- fix: 补充两校区的住宿信息选项
- fix: 表格无法展示数据的bug
- fix: 调整导出字段顺序，独立教育经历和团组织信息
- Merge branch 'achievement-list' into feat/mobile
- Merge branch 'dev' week 2
- Merge branch 'feat/export' into dev
- Merge branch 'feat/export' into dev
- Merge branch 'feat/export' into dev
- Merge branch 'feat/export' into feat/mobile
- Merge branch 'page/excelView' into dev 添加了表格试图
- Merge branch 'page/myinfo' into feat/mobile
- Merge branch 'page/myinfo' into feat/pdf
- Merge branch 'page/student-infos' into dev
- Merge branch 'page/student-infos' into dev 合并学生信息页面，完成了预览和导出
- Merge branch 'page/student-infos' into feat/mobile
- Merge myinfo 我的信息
- refactor: 将导出设置为组件，这样各个页面都能方便访问
- style: 为预览页面添加出入动画
- style: 优化pdf中的团组织信息展示
- style: 优化pdf住宿信息的展示
- style: 优化pdf标题与数据间距
- style: 优化pdf输出样式
- style: 优化了myinfos中三个按钮的样式
- style: 优化表格视图
- style: 修复pdf标题，个人成就单独一页开始
- style: 确保标题与内容同页面
- style: 翻译表格组件，删除表格注释
- TODO: 教育经历的高度设置为两行

### 2026-03-23
- docs: 更新备用联系方式文档
- docs: 更新学生干部经历的docs
- feat: 个人信息页添加出生日期
- feat: 删除学院，先填写学生类型才能选择专业，添加研究生专业
- feat: 学生干部经历接入数据库
- feat: 添加备用联系方式
- feat: 添加学生干部经历
- style: 详细地址提示到门牌号

### 2026-03-24
- assets: 添加了一些图标
- feat: 个人信息添加证件类型
- feat: 个人成就支持附件
- feat: 图片/视频预览功能
- feat: 成就支持添加三张图片以及附件
- feat: 文档预览功能：docx/doc/xlsx/xls/pdf
- feat: 新增双百工程/创新创业项目
- fix: 修复删除窗口无法正常关闭的问题
- Merge branch 'achievement-list' into page/myinfo
- style: achievement-media-panel改为sticky
- style: 优化docx的预览
- style: 优化了成就展示页面
- style: 优化图片展示
- style: 修正detail-badges的对齐

### 2026-03-25
- feat: 为所有页面添加胶囊
- feat: 美化移动端胶囊，添加动画效果
- fix: 修复全部选项消失的bug
- fix: 胶囊只在移动端出现
- refactor: 统一card-menu和profile-card
- style: 优化student-info在移动端的样式
- style: 优化student-info的胶囊，把列表/表格和全屏按钮统一样式
- style: 优化移动端的info-hero
- style: 优化网格在手机端的展示
- style: 隐藏步进器内部的上下按钮

### 2026-03-26
- fix: 修复桌面段achievement-fields的宽度显示
- pages: open admin 开放后台管理页面
- style: 优化教育/职业/联系人的外观
- style: 优化桌面段教育经历和干部经历的展示
- style: 优化选择成就类型时的动画
- style: 在未选择成就分类时不展示图片/附件添加入口

### 2026-03-27
- feat: 现在导出会统计成就数量
- fix: 修复username 的 placeholder 偏移问题
- fix: 修正一些导出选项，未填写显示无
- Merge branch 'feat/export' into dev 修复导出，未填写显示无
- Merge branch 'feat/export' into dev 合并新的统计：空显示无，统计成就数量
- style: 修复桌面段时间段的展示myinfos

### 2026-03-28
- feat: admin添加编辑信息按钮
- feat: 为admin添加组件
- feat: 将menu-card和profile-card做成组件
- feat: 当学生类别为研究生时，班级为1班
- fix: 修复一些简易的描述
- fix: 补充本科生专业
- Merge branch 'dev' into page/student-infos
- Merge branch 'page/myinfo' into dev

### 2026-04-01
- feat: 管理员现在可编辑个人信息

### 2026-04-02
- feat: 点击编辑时屏幕下方出现保存按钮
- fix: card-menu 动画
- fix: 修复card-menu的上间距
- Merge branch 'admin' into dev
- style: 优化card-menu的切换逻辑和排版
- style: 优化教育经历/干部经历的排版
- style: 将profile-card设计成nav

### 2026-04-03
- feat: 允许跨域访问资源
- feat: 入党流程不必经过入团
- feat: 后台管理可调节上传图片的数量以及附件大小
- feat: 后台管理可限制附件类型
- feat: 审核init
- feat: 导出的pdf分开入团和入党流程
- feat: 提交审核会记录更改的部分，方便审查
- feat: 消息分类，待处理、滞后和已处理
- Merge branch 'admin' into dev 新增了管理员页面
- style: 优化card-menu标题
- style: 优化card-menu滚动效果
- style: 优化教育经历/干部经历的审查排版
- style: 修复ghost按钮无法对齐action按钮的问题
- style: 删除分类组的admin-stat-card卡片
- style: 导出pdf时保持头像的宽高比
- style: 当宽度较小时，预览转移到屏幕下方

### 2026-04-06
- feat: admin搜索/筛选用户
- feat: admin用户管理
- feat: settings修改密码
- feat: 把动画设置为通用类型
- feat: 滞后审核显示滞后天数
- feat: 设置页会记录登录历史
- feat: 设置页导出个人信息
- feat: 设置页面记录登录记录
- fix: 修复设置路由
- fix: 无学号/班级时显示"/"
- fix: 类型不兼容问题
- Merge branch 'dev' into admin
- Merge branch 'feat/export' into page/settings
- Merge branch 'feat/notification' into admin 添加通知功能
- Merge branch 'feat/notification' into page/admin
- Merge branch 'page/settings' into dev 修复设置页面路由
- Merge branch 'page/settings' into dev 合并设置
- style: 为用户管理弹窗应用动画
- style: 优化通知列表展示
- style: 删除多余的描述,admin
- style: 删除鼠标悬浮效果settings
- style: 去除menu-notification-tabs的阴影
- style: 添加适当边距

### 2026-04-07
- feat: 取消审核功能
- feat: 审核开关
- feat: 审核界面可查看当前学生信息
- feat: 将成就审核功能接入后端
- feat: 成就审核
- fix: 修复个人信息未审核就发生更改
- fix: 修复类型错误
- Merge branch 'page/admin' into dev
- style: 简化审核管理界面

### 2026-04-09
- fix: 修复高亮导致光标无法看清
- fix: 设置图标
- refactor: 分开style.css
- refactor: 增加logo
- style: add 一些小装饰
- style: del card-menu shadow
- style: header四周圆角
- style: immerse 背景
- style: opti card-menu 动画
- style: 优化card-menu的样式
- style: 修复背景沉浸式设计
- style: 删除按钮闪光效果
- style: 去除card-menu的阴影
- style: 将profile-card设计到head上
- style: 放大logo
- style: 登录与注册页面

### 2026-04-10
- feat: footer
- feat: studentinfo添加学生类型筛选
- feat: 测试数据功能
- fix: dashboard-layout修复高度弹跳问题
- fix: 个人信息修复审核功能
- fix: 修复学生列表
- ignore jmeter
- ignore pycache
- style: achievement-dynamic-fields全宽
- style: favicon
- style: opti myinfos 样式
- style: 优化achievements页面的样式
- style: 优化admin的样式
- style: 优化edit-docker-inner按钮
- style: 优化student-info页面设计
- style: 优化通知页面样式结构
- style: 修改studentinfo结构,解决了页面闪动问题
- style: 修正页码切换器
- style: 取消渐变
- style: 调整card-menu的顺序

### 2026-04-11
- feat: 取消用户管理中的班级筛选
- feat: 备份附件
- feat: 实现数据库备份和恢复功能
- feat: 当筛选结果大于100, 查看表格预览时给出预警
- feat: 数据库导出时保存到项目目录
- feat: 新增备份和恢复入口
- feat: 新增通知功能
- feat: 离开通知页面前本地缓存输入框
- feat: 页脚跳转到https://beian.miit.gov.cn/
- fix: 修复编译错误
- fix: 修复预览成就页面依然出现页脚
- fix: 允许 * 访问
- fix: 用户管理不再显示所有用户
- fix: 解决恢复功能的跨域问题
- merge: resolve conflict in AdminView.vue - combine user management with tab nav
- style: notif-reject-box浮窗
- style: opti admin页面样式
- style: 优化info-card student-filter-card的样式
- style: 优化用户列表的展示效果
- style: 修复成就预览不全宽的问题
- style: 修复页脚描述
- style: 修改附件恢复功能的描述
- style: 删除admin页面不必要的信息
- style: 缩小悬浮分割弹窗的间距
- style: 美化拒绝窗口
- style: 调整学生信息预览按钮的排版

### 2026-04-12
- feat: 为导出/修改密码/修改配置等添加toast提示
- fix: 修复附件媒体预览
- Merge branch 'admin' into style/google 合并备份与恢复
- style: info-shell 删除背景
- style: 优化card student-results-card的间距
- style: 优化个人成就banner的边距
- style: 优化媒体查看器关闭逻辑
- style: 优化成就增加和编辑页面
- style: 整理表格样式
- style: 统一tabs样式
- style: 统一加载态
- style: 统一动画样式
- style: 统一卡片样式
- style: 统一按钮样式

### 2026-04-13
- feat: 登录提示
- feat: 首次登录提示填写资料
- fix: 修复成就描述
- fix: 注册成功跳转至登录页,而非直接登录
- ignore vitepress
- refactor: 分离可复用组件
- refactor: 删除无用样式
- refactor: 将重复的工具函数提取到共享模块和 composables 中
- refactor: 提取成就页查看内容页
- refactor: 整理样式
- style: notif-empty样式修正
- style: studentinfo悬浮按钮
- style: 优化studentinfo的按钮
- 提取工具函数到 utils/constants，消除跨视图重复代码

### 2026-04-14
- feat: 修复role参数缺失
- feat: 提交成就时toast提示审核
- feat: 正在审核时用户无法编辑个人信息
- feat: 计数通知功能
- fix: 为新注册用户新增profile
- fix: 修复步进器切换问题
- fix: 修复管理员编辑成就需要审核的问题
- fix: 修复通知分类问题
- fix: 修正发布期刊和双百工程的作品名称字段
- fix: 提交审核时使用了错误的作品名
- fix: 注册绑定学号到个人信息
- Merge branch 'achievements' into dev
- Merge branch 'dev' into achievements
- refactor: 优化成就设计模板
- style: del 谷歌字体
- style: 不显示review-achievement-date
- style: 优化myinfos界面
- style: 优化步进器样式
- style: 修复成就编辑页在窄宽度的效果
- style: 将驳回理由放在前面
- style: 通知显示avatar媒体

### 2026-04-15
- feat: 把通知tab栏移动到cardmenu
- feat: 文件导入用户
- feat: 未读小红点
- fix: 修复本地与服务器数据冲突
- Merge branch 'dev'
- style: 优化通知列表的样式
- style: 修复cardmenu的动画
- style: 修正notification tabs样式
- style: 通知计数

### 2026-04-16
- feat: admin页面其他页
- feat: 为双百/大创添加achievementVIEW
- feat: 关闭注册功能
- feat: 用户管理选择功能
- feat: 管理用户列表不再显示自己
- fix: 修复9个成就对应前端后端数据库字段
- fix: 成就修改审核锁,正在审核的成就无法编辑/删除
- fix: 管理员无法删除自己
- import: zread
- init vitepress
- Merge branch 'achievements' into dev
- Merge branch 'admin' into teachSep

### 2026-04-17
- docs: API

### 2026-04-20
- docs: from gcsc to BDAI-SC
- docs: 调整quickstart顺序
- fix: VPNav
- style: style and 侧边栏 and index

### 2026-04-25
- ignore cache dist

### 2026-04-26
- chore: opti README
- chore: opti README
- chore: opti README
- chore: README
- chore: 项目重命名 gcsc → bdai_sc（文档/变量名/storage key）
- feat: admin用户表单可以选择角色筛选
- feat: admin页面可以调整滞后时间
- feat: 为老师添加班级分类
- feat: 引入 .env 统一配置机制
- feat: 新增班干部角色
- feat: 添加用户弹窗右侧新增 xlsx/csv 格式示例图片
- feat: 班干部审核
- feat: 老师只能看到自己班级
- feat: 通知检测,如果当前通知已经被处理,通过toast通知用户
- fix: 信息修改使用了成就的api
- fix: 修复studentinfo查不到班干部
- fix: 修复通知可见性
- fix: 修复通知的时间获取,添加已滞后展示
- fix: 班级审核的路由不再使用notification
- Merge branch 'dev'
- Merge branch 'dev' into admin
- style: 统一班级审核页面到通知页面

### 2026-04-27
- chore: opti README
- chore: opt README
- chore: rm todo
- chore: update CLAUDE AND AGENTS, unignore agents
- chore: 删除docs
- chore: 成就 -> 成果
- feat: add skill
- feat: opti notification
- feat: 修复教师的班级责任范围
- feat: 增加特殊学生选项
- feat: 导入重复检测
- feat: 根据身份id识别身份是hk/mo/tw
- feat: 用户备注
- feat: 登录历史
- fix: 修复步进器从2000开始的问题
- fix: 修复滚动的位置
- fix: 完善证件号码输入校验逻辑和提示
- Merge branch 'admin' into dev
- style: AWESOME banner!!!! 太好看了 deepseek牛逼
- style: opti admin
- style: opti card-menu
- style: opti myinfos
- style: opti myinfos
- style: opti 个人成果页面
- style: opti 学生信息
- style: opti 成就banner
- style: 优化edit-dock-inner的样式
- style: 优化users-content的样式

### 2026-04-28
- feat: chip跟随滚动
- feat: vite proxy, ready to opti 移动端
- feat: 三秒不动chip自动收起
- feat: 上传进度条
- feat: 为教育经历和干部经历添加清空按钮
- feat: 为日历选择器增加日历图标
- feat: 导出增加干部经历
- feat: 移动端不显示xylogo
- feat: 证明资料
- fix: achievement-grid在achievement-fields全宽
- fix: added bottom margin
- fix: brand-profile-chip的出现位置
- fix: header (10) < chip (35) < backdrop (40) < sheet/view (50) < capsule(55)
- fix: rm dock in 移动端
- fix: 修复achievement-sheet open的底部边距
- fix: 修复vite proxy
- fix: 修复住址选择器
- fix: 修复其他个人信息审核导致本账号无法编辑的bug
- fix: 修复原本无条件清空证件号码的bug
- fix: 修复屏幕宽度不够的时候班级序号溢出问题
- fix: 修复已读未读共享状态
- fix: 修复教育经历和干部经历判空有差的bug
- fix: 修复步进器错误
- fix: 把achievement-sheet open的通知改为toast
- fix: 查看成就时capsule自动隐藏
- fix: 桌面段chip最高高度距离顶部75px
- fix: 移动端侧边栏
- style: chip click
- style: mobile login page
- style: opti myinfos mobile
- style: 教育经历+干部经历重构
- style: 胶囊样式

### 2026-04-29
- feat: 侧边栏退出动画
- feat: 年选择器
- feat: 移动端单独筛选
- feat: 选择年级后必须选择专业
- feat: 通知页面胶囊
- fix: achievement-backdrop 在移动端的效果
- fix: avatar border
- fix: brand-profile-chip统一边距
- fix: chip index
- fix: edit-dock
- fix: footer zindex
- fix: 个人成果等待用户选择再跳转
- fix: 其他设置的选择框改为步进器
- fix: 取消详情按钮
- fix: 备份按钮
- fix: 学生列表显示学生头像
- fix: 审核状态不保留问题
- fix: 点击tab栏的时候不会自动退出侧边栏
- fix: 班级选择改为选择器
- style: admin capsule
- style: admin mobile
- style: capsoule图标边框
- style: capsule
- style: click icon渐变too
- style: del student-filter-caption
- style: finally 整好了 zindex
- style: logo background
- style: opti admin view
- style: opti chip
- style: opti mobile users-card-header
- style: opti student-detail-view open
- style: opti 移动端footer
- style: studentinfo icon
- style: 移动端不显示admin-tabs

### 2026-04-30
- docs: manual
- docs: 使用手册
- docs: 删除时长和等级
- docs: 重写gettingstarted
- feat: use 统一的blur 背景颜色
- feat: 导出按钮移动到capsule上
- feat: 检查用户占用
- feat: 港澳台移动端可单点取消
- feat: 用户分开各自文件夹存储附件
- feat: 移动端编辑与导出PDF添加到CAPSULE
- feat: 证件类型/证件号码 单独一行
- fix: col-checkbox操作逻辑
- fix: true ip
- fix: 代码块fallback中文字体
- fix: 侧边栏高于capsule
- fix: 前端启动env文件
- fix: 港澳台互斥
- fix: 移动端备用联系方式单独一行
- fix: 移动端显示筛选结果
- fix: 究极bug
- Merge branch 'dev'
- Merge branch 'dev' into docs
- Merge branch 'main' into docs
- Merge branch 'main' into docs
- Merge branch 'main' into docs
- style: 优化侧边栏交互
- style: 删除成就页面的全部提示
- style: 表格取消描边
- style: 返璞归真
- style: 重新设计配色
- style: 重新配色

### 2026-05-01
- docs: login1
- feat: student-detail capsule
- fix: 修复通知已读逻辑
- fix: 隐藏student-detail-capsule桌面段

### 2026-05-02
- docs: manual
- docs: 删除在GitHub上编辑此页
- feat: 为图片添加圆角和阴影
- feat: 单独通知admin媒体限制
- feat: 图片查看控件
- feat: 学历改为选择器
- fix: syncStoredIds
- fix: 修复myinfos中层号错误问题
- fix: 修复未读随机变成已读的问题
- fix: 入党无需rutuan
- fix: 居中studentinfos中的floating-btn
- fix: 证明资料卡片始终位于成果页面限制下面
- fix: 身份证最后一位无法填写数字
- style: pagination-bar pagination-bar--simple 增加上下边距
- style: vitepress theme
- style: 标题颜色
- style: 桌面端改成悬浮按钮

### 2026-05-03
- chore: format
- style: logo

### 2026-05-04
- docs: getting-started
- feat: image viewer

### 2026-05-05
- docs: backup and restore
- docs: init
- docs: zread
- docs: 在getting started中更新manual目录
- docs: 测试流程
- feat: env
- feat: init
- feat: test users
- feat: vite的checkbox style
- fix: 注册只允许 STUDENT role
- style: center feed-title, 好看多了

### 2026-05-06
- docs: update myinfos
- docs: 设计规范

