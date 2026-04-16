import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'GCSC 文档中心',
  description: 'GCSC Student Center 文档',
  lang: 'zh-CN',

  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/' },
      { text: 'API', link: '/api/' }
    ],
    sidebar: [
      {
        text: '指南',
        items: [
          { text: '介绍', link: '/guide/' },
          { text: '快速开始', link: '/guide/getting-started' }
        ]
      }
    ]
  }
})
