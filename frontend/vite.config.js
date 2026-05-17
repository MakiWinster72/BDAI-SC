import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const envDir = fileURLToPath(new URL('../', import.meta.url))
  const env = loadEnv(mode, envDir, '')

  return {
    envDir,
    plugins: [vue()],
    server: {
      host: env.BDAI_SC_FRONTEND_HOST || '0.0.0.0',
      port: Number.parseInt(env.BDAI_SC_FRONTEND_PORT, 10) || 5173,
      proxy: {
        '/api': {
          target: `http://127.0.0.1:${env.BDAI_SC_BACKEND_PORT || env.VITE_BACKEND_PORT || '8080'}`,
          changeOrigin: true,
          xfwd: true,
        },
        '/uploads': {
          target: `http://127.0.0.1:${env.BDAI_SC_BACKEND_PORT || env.VITE_BACKEND_PORT || '8080'}`,
          changeOrigin: true,
          xfwd: true,
        },
      },
    },
  }
})
