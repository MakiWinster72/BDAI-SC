# Axios 请求层

## 概述


Axios 请求层是 BDAI-SC 中所有前端到后端 HTTP 交互流经的唯一网关。

## 核心实例架构

`request.js` 创建了一个自定义 Axios 实例，配备两个拦截器，将身份验证令牌注入、Base URL 解析、超时策略集中在一个仅 35 行的模块中。

## 实例配置

| 设置 | 值 | 用途 |
|------|------|------|
| baseURL | VITE_API_BASE 或 `http://{hostname}:8080` | 路由到 Spring Boot 后端 |
| timeout | 12000 毫秒 | 防止无限期挂起 |

## 拦截器流水线

### 请求拦截器 - 令牌注入

```javascript
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gcsc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 响应拦截器 - 未授权处理

当检测到 401 时：
1. 清除 `gcsc_token` 和 `gcsc_user`
2. 硬导航到 `/login`

## 领域 API 模块映射

| 模块 | 文件 | 方法 |
|------|------|------|
| Auth | auth.js | POST, GET |
| Achievements | achievements.js | GET, POST, PUT, DELETE |
| Profile | profile.js | GET, PUT |
| Upload | upload.js | POST (multipart) |
| Admin | admin.js | GET, PUT, DELETE, fetch* |

## 二进制响应与 Fetch 逃生舱

`admin.js` 中的四个备份函数使用原生 `fetch()` 而非 Axios，因为 Axios 的 JSON 转换器会破坏二进制文件下载。

```javascript
export function downloadBackupDb() {
  const token = localStorage.getItem('gcsc_token')
  return fetch(`${API_BASE}/api/admin/backup/db`, {
    headers: { Authorization: `Bearer ${token}` }
  })
}
```

## 错误处理理念

**两层错误处理策略：**

1. **全局层（拦截器）**：只有 401 触发自动会话清理
2. **局部层（composable）**：每个 composable 管理自己的错误信息

## 后续步骤

- [Vue Router 与路由守卫](./router) — 路由守卫如何与令牌生命周期交互
- [JWT 身份验证流程](./jwt-auth) — 令牌建立
