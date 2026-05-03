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
