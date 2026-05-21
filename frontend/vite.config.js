import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const envDir = fileURLToPath(new URL('../', import.meta.url))
  const env = loadEnv(mode, envDir, '')

  return {
    envDir,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: env.BDAI_SC_FRONTEND_HOST || '0.0.0.0',
      port: Number.parseInt(env.BDAI_SC_FRONTEND_PORT, 10) || 5173,
      proxy: {
        '/api': {
          target: `http://127.0.0.1:${env.BDAI_SC_BACKEND_PORT || env.VITE_BACKEND_PORT || '8080'}`,
          changeOrigin: true,
          xfwd: true,
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq, req) => {
              const raw =
                req.headers['x-forwarded-for'] ||
                req.socket?.remoteAddress ||
                req.connection?.remoteAddress
              if (!raw) return
              const ip = String(raw).split(',')[0].trim().replace(/^::ffff:/, '')
              if (!ip) return
              proxyReq.setHeader('X-Forwarded-For', ip)
              proxyReq.setHeader('X-Real-IP', ip)
            })
          },
        },
        '/uploads': {
          target: `http://127.0.0.1:${env.BDAI_SC_BACKEND_PORT || env.VITE_BACKEND_PORT || '8080'}`,
          changeOrigin: true,
          xfwd: true,
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq, req) => {
              const raw =
                req.headers['x-forwarded-for'] ||
                req.socket?.remoteAddress ||
                req.connection?.remoteAddress
              if (!raw) return
              const ip = String(raw).split(',')[0].trim().replace(/^::ffff:/, '')
              if (!ip) return
              proxyReq.setHeader('X-Forwarded-For', ip)
              proxyReq.setHeader('X-Real-IP', ip)
            })
          },
        },
      },
    },
  }
})
