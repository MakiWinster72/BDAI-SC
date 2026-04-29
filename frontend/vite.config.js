import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const envDir = fileURLToPath(new URL('..', import.meta.url))
  const env = loadEnv(mode, envDir, '')
  const frontendHost = env.BDAI_SC_FRONTEND_HOST || '0.0.0.0'
  const frontendPort = Number.parseInt(env.BDAI_SC_FRONTEND_PORT || '5173', 10)
  const backendPort = env.BDAI_SC_BACKEND_PORT || env.VITE_BACKEND_PORT || '8080'

  return {
    envDir,
    plugins: [vue()],
    server: {
      host: frontendHost,
      port: Number.isNaN(frontendPort) ? 5173 : frontendPort,
      proxy: {
        '/api': {
          target: `http://127.0.0.1:${backendPort}`,
          changeOrigin: true,
        },
        '/uploads': {
          target: `http://127.0.0.1:${backendPort}`,
          changeOrigin: true,
        },
      },
    },
  }
})
