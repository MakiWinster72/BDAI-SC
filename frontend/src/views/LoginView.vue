<template>
  <div class="auth-layout">
    <div class="auth-card">
      <h1 class="auth-title">欢迎回来</h1>
      <p class="auth-subtitle">登录 GCSC 学生中心</p>

      <form @submit.prevent="handleLogin">
        <div class="form-row">
          <label class="form-label" for="username">用户名</label>
          <input
            id="username"
            v-model.trim="form.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            required
          />
        </div>

        <div class="form-row">
          <label class="form-label" for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            class="form-input"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
          />
        </div>

        <button class="action-button" :disabled="isSubmitting" type="submit">
          {{ isSubmitting ? '登录中...' : '登录' }}
        </button>
      </form>

      <div :class="['feedback', feedback.type]">{{ feedback.text }}</div>

      <div class="switch-line">
        还没有账号？
        <RouterLink class="switch-link" to="/register">去注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { login } from '../api/auth'

const route = useRoute()

const form = reactive({
  username: route.query.username ? String(route.query.username) : '',
  password: ''
})

const isSubmitting = ref(false)
const feedback = reactive({ text: '', type: '' })

function parseError(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message
  }
  return '请求失败，请检查后端服务是否启动'
}

async function handleLogin() {
  feedback.text = ''
  feedback.type = ''

  isSubmitting.value = true
  try {
    const { data } = await login(form)
    feedback.text = data.message || '登录成功'
    feedback.type = 'success'

    localStorage.setItem(
      'gcsc_user',
      JSON.stringify({ username: data.username, displayName: data.displayName })
    )
  } catch (error) {
    feedback.text = parseError(error)
    feedback.type = 'error'
  } finally {
    isSubmitting.value = false
  }
}
</script>
