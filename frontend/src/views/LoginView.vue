<template>
  <div class="auth-layout">
    <!-- Ambient orbs -->
    <div class="page-orb page-orb--1" aria-hidden="true"></div>
    <div class="page-orb page-orb--2" aria-hidden="true"></div>

    <!-- Split panel: logo left, form right -->
    <div class="split-panel">

      <!-- ── LEFT: Brand Panel ── -->
      <div class="brand-panel" aria-hidden="true">
        <div class="brand-bg-orb brand-bg-orb--1"></div>
        <div class="brand-bg-orb brand-bg-orb--2"></div>
        <div class="brand-bg-orb brand-bg-orb--3"></div>
        <div class="brand-content">
          <div class="brand-emblem">
            <img
              src="/assets/icons/xylogo.png"
              alt="XY"
              class="brand-emblem-img"
            />
            <div class="brand-emblem-ring"></div>
          </div>
          <div class="brand-text">
            <div class="brand-name">大数据与人工智能学院</div>
            <div class="brand-tagline">学生信息管理中心</div>
          </div>
        </div>
      </div>

      <!-- ── RIGHT: Form Panel ── -->
      <div class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <h1 class="form-title">欢迎回来</h1>
            <p class="form-subtitle">登录到您的账号</p>
          </div>

          <form @submit.prevent="handleLogin" novalidate>
            <div class="form-row">
              <label class="form-label" for="username">学号</label>
              <input
                id="username"
                v-model.trim="form.username"
                class="form-input"
                type="text"
                placeholder="请输入学号"
                autocomplete="username"
                :class="{ 'input--error': usernameError }"
                required
              />
              <span v-if="usernameError" class="field-error" role="alert">{{ usernameError }}</span>
            </div>

            <div class="form-row">
              <label class="form-label" for="password">密码</label>
              <div class="input-wrapper">
                <input
                  id="password"
                  v-model="form.password"
                  class="form-input"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                >
                  <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
            </div>

            <button class="action-button" :disabled="isSubmitting" type="submit">
              <span class="btn-content">
                <span v-if="isSubmitting" class="btn-spinner" aria-hidden="true"></span>
                {{ isSubmitting ? "登录中..." : "登录" }}
              </span>
            </button>
          </form>

          <Transition name="feedback-fade">
            <div v-if="feedback.text" :class="['feedback', feedback.type]" role="status" aria-live="polite">
              <svg v-if="feedback.type === 'error'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              {{ feedback.text }}
            </div>
          </Transition>

          <div class="switch-line">
            还没有账号？
            <RouterLink class="switch-link" to="/register">去注册</RouterLink>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login } from "../api/auth";

const route = useRoute();
const router = useRouter();

const form = reactive({
  username: route.query.username ? String(route.query.username) : "",
  password: "",
});

const isSubmitting = ref(false);
const feedback = reactive({ text: "", type: "" });
const showPassword = ref(false);
const usernameError = ref("");

function parseError(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  return "请求失败，请检查后端服务是否启动";
}

async function handleLogin() {
  feedback.text = "";
  feedback.type = "";
  usernameError.value = "";

  if (!form.username) {
    usernameError.value = "请输入学号";
    return;
  }

  isSubmitting.value = true;
  try {
    const { data } = await login(form);
    feedback.text = data.message || "登录成功";
    feedback.type = "success";

    localStorage.setItem(
      "gcsc_user",
      JSON.stringify({
        username: data.username,
        displayName: data.displayName,
        avatarUrl: data.avatarUrl || "",
        role: data.role,
        studentNo: data.studentNo,
        className: data.className,
        college: data.college,
      }),
    );
    localStorage.setItem("gcsc_token", data.token || "");
    router.push("/myinfos");
  } catch (error) {
    feedback.text = parseError(error);
    feedback.type = "error";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
/* ── Layout ── */
.auth-layout {
  position: relative;
  min-height: 100vh;
  padding: 28px;
  display: grid;
  place-items: center;
  overflow: hidden;
}

/* ── Ambient Orbs ── */
.page-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  pointer-events: none;
  animation: floatOrb 20s ease-in-out infinite;
}
.page-orb--1 {
  width: 40rem; height: 40rem;
  top: -16rem; right: -10rem;
  background: rgba(100, 12, 114, 0.32);
}
.page-orb--2 {
  width: 30rem; height: 30rem;
  bottom: -14rem; left: -8rem;
  background: rgba(155, 89, 182, 0.28);
  animation-delay: -8s;
}

/* ── Split Panel ── */
.split-panel {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  width: min(980px, calc(100vw - 40px));
  min-height: 640px;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: var(--shadow), 0 0 0 1px rgba(255,255,255,0.5);
  animation: cardEnter 720ms var(--ease-out) forwards;
}

/* ── Brand Panel (Left) ── */
.brand-panel {
  position: relative;
  background: linear-gradient(145deg, #3d0770 0%, #640c72 45%, #7b2d8e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 56px 40px;
  min-height: 580px;
}

/* Decorative brand bg orbs */
.brand-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  pointer-events: none;
}
.brand-bg-orb--1 {
  width: 24rem; height: 24rem;
  top: -8rem; left: -6rem;
  background: rgba(155, 89, 182, 0.5);
  animation: floatOrb 14s ease-in-out infinite;
}
.brand-bg-orb--2 {
  width: 18rem; height: 18rem;
  bottom: -5rem; right: -5rem;
  background: rgba(199, 125, 255, 0.45);
  animation: floatOrb 18s ease-in-out infinite reverse;
  animation-delay: -6s;
}
.brand-bg-orb--3 {
  width: 10rem; height: 10rem;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.06);
  animation: pulseOrb 6s ease-in-out infinite;
}

/* Brand content */
.brand-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22px;
  text-align: center;
}

/* Emblem with animated ring */
.brand-emblem {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 296px;
  height: 296px;
  animation: floatEmblem 8s ease-in-out infinite;
}

.brand-emblem-ring {
  position: absolute;
  inset: 20px;
  border-radius: 50%;
  border: 2px solid rgba(199, 125, 255, 0.5);
  animation: spinRing 20s linear infinite;
}
.brand-emblem-ring::before {
  content: "";
  position: absolute;
  top: -5px; left: 50%;
  transform: translateX(-50%);
  width: 10px; height: 10px;
  border-radius: 50%;
  background: #c77dff;
  box-shadow: 0 0 16px rgba(199, 125, 255, 0.85), 0 0 32px rgba(199,125,255,0.35);
}

.brand-emblem-img {
  width: 240px;
  height: 240px;
  border-radius: 64px;
  object-fit: contain;
  filter: drop-shadow(0 12px 40px rgba(0,0,0,0.4));
}

/* Brand text */
.brand-name {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
  text-shadow: 0 2px 20px rgba(0,0,0,0.25);
  line-height: 1.3;
}

.brand-tagline {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 4px;
  font-weight: 600;
}

/* ── Form Panel (Right) ── */
.form-panel {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 44px;
}

.form-card {
  width: 100%;
  max-width: 340px;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: var(--primary-dark);
  letter-spacing: -0.3px;
}

.form-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--text-sub);
}

/* ── Form Elements ── */
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-dark);
  letter-spacing: 0.2px;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  border: 1.5px solid rgba(100, 12, 114, 0.18);
  background: rgba(255, 255, 255, 0.95);
  color: var(--text-main);
  border-radius: 12px;
  height: 46px;
  padding: 0 14px;
  outline: none;
  font-size: 15px;
  font-family: inherit;
  transition: border-color 240ms ease, box-shadow 240ms ease;
}
.form-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(100, 12, 114, 0.1);
}
.form-input::placeholder {
  color: rgba(100, 12, 114, 0.36);
}
.form-input.input--error {
  border-color: var(--danger);
}

.form-row {
  margin-bottom: 16px;
}

.field-error {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: var(--danger);
}

.input-wrapper {
  position: relative;
}
.input-wrapper .form-input {
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  right: 12px; top: 50%;
  transform: translateY(-50%);
  background: none; border: none;
  cursor: pointer;
  color: var(--text-sub);
  padding: 4px;
  display: inline-flex;
  align-items: center;
  border-radius: 6px;
  transition: color 200ms ease;
}
.password-toggle:hover { color: var(--primary); }

/* ── Button ── */
.action-button {
  margin-top: 8px;
  border: 0;
  width: 100%;
  height: 48px;
  border-radius: 12px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.3px;
  cursor: pointer;
  background: linear-gradient(135deg, #9b59b6, #640c72);
  box-shadow: 0 4px 16px rgba(100, 12, 114, 0.38);
  transition: transform 220ms ease, box-shadow 220ms ease, filter 220ms ease;
  overflow: hidden;
  position: relative;
}
.action-button::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent);
  transform: translateX(-100%);
  transition: transform 500ms ease;
}
.action-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(100, 12, 114, 0.48);
  filter: brightness(1.05);
}
.action-button:hover:not(:disabled)::before { transform: translateX(100%); }
.action-button:active:not(:disabled) { transform: translateY(0); }
.action-button:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-spinner {
  display: inline-block;
  width: 15px; height: 15px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

/* ── Feedback ── */
.feedback {
  margin-top: 12px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.feedback.error { color: var(--danger); }
.feedback.success { color: var(--success); }

.feedback-fade-enter-active,
.feedback-fade-leave-active { transition: opacity 280ms ease, transform 280ms ease; }
.feedback-fade-enter-from,
.feedback-fade-leave-to { opacity: 0; transform: translateY(-3px); }

/* ── Switch ── */
.switch-line {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--text-sub);
}
.switch-link {
  color: var(--primary);
  font-weight: 600;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 200ms ease, color 200ms ease;
}
.switch-link:hover { color: var(--primary-dark); border-color: var(--primary); }

/* ── Animations ── */
@keyframes cardEnter {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes floatOrb {
  0%, 100% { transform: translate3d(0,0,0) scale(1); }
  50% { transform: translate3d(14px,-18px,0) scale(1.04); }
}
@keyframes floatEmblem {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}
@keyframes spinRing {
  to { transform: rotate(360deg); }
}
@keyframes pulseOrb {
  0%, 100% { opacity: 0.5; transform: translate(-50%,-50%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%,-50%) scale(1.15); }
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .split-panel {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    min-height: auto;
    width: min(480px, calc(100vw - 32px));
    border-radius: 24px;
  }
  .brand-panel {
    padding: 40px 28px 32px;
    gap: 14px;
  }
  .brand-emblem { width: 200px; height: 200px; }
  .brand-emblem-img { width: 164px; height: 164px; border-radius: 46px; }
  .brand-name { font-size: 20px; letter-spacing: 1px; }
  .brand-tagline { font-size: 13px; letter-spacing: 1px; }
  .form-panel { padding: 32px 28px; }
  .auth-layout { padding: 16px; }
}

@media (max-width: 480px) {
  .brand-panel { padding: 32px 20px 24px; }
  .form-panel { padding: 28px 20px; }
  .form-card { max-width: 100%; }
  .brand-emblem { width: 140px; height: 140px; margin-bottom: 4px; }
  .brand-emblem-img { width: 116px; height: 116px; border-radius: 32px; }
  .brand-name { font-size: 18px; letter-spacing: 1px; }
  .brand-tagline { font-size: 12px; }
}
</style>
