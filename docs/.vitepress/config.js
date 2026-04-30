import { defineConfig } from "vitepress";

export default defineConfig({
  title: "BSAI-SC wiki",
  description: "大数据与人工智能学院学生信息管理中心 · 技术文档",
  lang: "zh-CN",
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,

  // Code syntax highlighting with Shiki
  markdown: {
    theme: {
      light: "github-light",
      dark: "tokyo-night",
    },
    lineNumbers: false,
  },

  // Head tags — fonts + favicon
  head: [
    ["link", { rel: "icon", type: "image/svg+xml", href: "/favicon.svg" }],
    // Preconnect: skip DNS lookup + TCP handshake for Google Fonts
    ["link", { rel: "preconnect", href: "https://fonts.googleapis.com" }],
    [
      "link",
      {
        rel: "preconnect",
        href: "https://fonts.gstatic.com",
        crossorigin: "",
      },
    ],
    [
      "link",
      {
        rel: "preconnect",
        href: "https://fonts.gstatic.com",
      },
    ],
  ],

  themeConfig: {
    // Logo
    logo: "/logo.svg",
    siteTitle: "数智学院学生信息管理中心",

    // Nav
    nav: [
      { text: "首页", link: "/" },
      {
        text: "指南",
        items: [
          { text: "快速开始", link: "/guide/getting-started" },
        ],
      },
      {
        text: "详细设计",
        items: [
          {
            text: "概述",
            link: "/design/",
          },
          {
            text: "后端核心",
            items: [
              { text: "架构概述", link: "/design/architecture" },
              { text: "JWT 认证", link: "/design/jwt-auth" },
              { text: "RBAC", link: "/design/rbac" },
              { text: "REST API", link: "/design/rest-api" },
              { text: "学生档案", link: "/design/student-profile-model" },
              { text: "成就系统", link: "/design/achievement-system" },
              { text: "审核工作流", link: "/design/review-workflow" },
              { text: "数据库 Schema", link: "/design/database-schema" },
              { text: "管理面板", link: "/design/admin-backup" },
            ],
          },
          {
            text: "前端核心",
            items: [
              { text: "路由守卫", link: "/design/router" },
              { text: "Axios 层", link: "/design/axios-layer" },
              { text: "仪表盘布局", link: "/design/dashboard-layout" },
              { text: "成就编辑器", link: "/design/achievement-editor" },
              { text: "导出工具", link: "/design/export-tools" },
            ],
          },
          {
            text: "设计与体验",
            items: [
              { text: "CSS 架构", link: "/design/css-architecture" },
              { text: "动画模式", link: "/design/animation" },
              { text: "Iframe 嵌入", link: "/design/iframe-embed" },
            ],
          },
          {
            text: "参考",
            items: [
              { text: "配置参考", link: "/design/config-reference" },
              { text: "项目结构", link: "/design/project-structure" },
              { text: "快速开始", link: "/design/quick-start" },
            ],
          },
        ],
      },
      { text: "API", link: "/api/" },
    ],

    // Sidebar — grouped with section titles
    sidebar: {
      "/guide/": [
        {
          text: "指南",
          items: [
            { text: "快速开始", link: "/guide/getting-started" },
            { text: "详细设计概述", link: "/design/" },
          ],
        },
      ],

      "/design/": [
        {
          text: "概述",
          items: [{ text: "设计总览", link: "/design/" }],
        },
        {
          text: "后端核心",
          items: [
            { text: "架构概述", link: "/design/architecture" },
            { text: "JWT 认证流程", link: "/design/jwt-auth" },
            { text: "基于角色的访问控制", link: "/design/rbac" },
            { text: "REST API 设计", link: "/design/rest-api" },
            { text: "学生档案数据模型", link: "/design/student-profile-model" },
            { text: "多实体成就系统", link: "/design/achievement-system" },
            { text: "审核与审批工作流", link: "/design/review-workflow" },
            { text: "数据库 Schema 约定", link: "/design/database-schema" },
            { text: "管理面板与备份", link: "/design/admin-backup" },
          ],
        },
        {
          text: "前端核心",
          items: [
            { text: "Vue Router 与路由守卫", link: "/design/router" },
            { text: "Axios 请求层", link: "/design/axios-layer" },
            { text: "仪表盘布局与导航", link: "/design/dashboard-layout" },
            {
              text: "成就编辑器组合式函数",
              link: "/design/achievement-editor",
            },
            { text: "学生档案导出工具", link: "/design/export-tools" },
          ],
        },
        {
          text: "设计与体验",
          items: [
            { text: "CSS 架构与约定", link: "/design/css-architecture" },
            { text: "动画与过渡模式", link: "/design/animation" },
            { text: "内嵌 Iframe 模式", link: "/design/iframe-embed" },
          ],
        },
        {
          text: "参考",
          items: [
            { text: "配置参考", link: "/design/config-reference" },
            { text: "项目结构", link: "/design/project-structure" },
            { text: "快速开始", link: "/design/quick-start" },
          ],
        },
      ],

      "/api/": [
        {
          text: "API 参考",
          items: [{ text: "接口总览", link: "/api/" }],
        },
      ],
    },

    // Search
    search: {
      provider: "local",
      options: {
        detailedView: true,
      },
    },

    // Footer
    footer: {
      message: "基于 Spring Boot + Vue 3 构建",
      copyright: "版权所有 © 广东财经大学大数据与人工智能学院",
    },

    // Social links
    socialLinks: [
      { icon: "github", link: "https://github.com/MakiWinster/gcsc" },
    ],

    // Edit link
    editLink: {
      pattern: "https://github.com/MakiWinster/gcsc/edit/main/docs/:path",
      text: "在 GitHub 上编辑此页",
    },

    // Doc footer
    docFooter: {
      prev: "上一页",
      next: "下一页",
    },

    // Outline
    outline: {
      level: [2, 3],
      label: "目录",
    },

    // Last updated
    lastUpdated: {
      text: "最后更新",
      formatOptions: {
        dateStyle: "short",
        timeStyle: "short",
      },
    },
  },
});
