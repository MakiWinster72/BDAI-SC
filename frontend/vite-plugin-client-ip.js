import { resolveClientIpFromReq } from './vite-proxy-client-ip.js'

/**
 * 在 Vite 收到请求时尽早写入 X-Real-IP / X-Forwarded-For，
 * 供后续 /api 反代带给后端（避免只看到 127.0.0.1）。
 */
export function clientIpDevPlugin() {
  return {
    name: 'client-ip-dev-middleware',
    configureServer(server) {
      server.middlewares.use((req, _res, next) => {
        const ip = resolveClientIpFromReq(req)
        if (ip) {
          req.headers['x-forwarded-for'] = ip
          req.headers['x-real-ip'] = ip
        }
        next()
      })
    },
  }
}
