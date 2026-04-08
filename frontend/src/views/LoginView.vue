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

        <!-- Data grid pattern -->
        <div class="data-grid" aria-hidden="true">
          <svg class="data-grid-svg" viewBox="0 0 280 320" xmlns="http://www.w3.org/2000/svg">
            <!-- Horizontal lines -->
            <line x1="0" y1="40" x2="280" y2="40" class="grid-line"/>
            <line x1="0" y1="80" x2="280" y2="80" class="grid-line"/>
            <line x1="0" y1="120" x2="280" y2="120" class="grid-line"/>
            <line x1="0" y1="160" x2="280" y2="160" class="grid-line"/>
            <line x1="0" y1="200" x2="280" y2="200" class="grid-line"/>
            <line x1="0" y1="240" x2="280" y2="240" class="grid-line"/>
            <line x1="0" y1="280" x2="280" y2="280" class="grid-line"/>
            <!-- Vertical lines -->
            <line x1="40" y1="0" x2="40" y2="320" class="grid-line"/>
            <line x1="80" y1="0" x2="80" y2="320" class="grid-line"/>
            <line x1="120" y1="0" x2="120" y2="320" class="grid-line"/>
            <line x1="160" y1="0" x2="160" y2="320" class="grid-line"/>
            <line x1="200" y1="0" x2="200" y2="320" class="grid-line"/>
            <line x1="240" y1="0" x2="240" y2="320" class="grid-line"/>
            <!-- Data points at intersections -->
            <circle cx="80" cy="80" r="3" class="data-node"/>
            <circle cx="160" cy="120" r="3" class="data-node"/>
            <circle cx="200" cy="80" r="3" class="data-node"/>
            <circle cx="120" cy="200" r="3" class="data-node"/>
            <circle cx="40" cy="160" r="3" class="data-node"/>
            <circle cx="240" cy="200" r="3" class="data-node"/>
            <!-- Data flow lines connecting nodes -->
            <polyline points="80,80 160,120 200,80" class="data-flow-line"/>
            <polyline points="40,160 120,200 240,200" class="data-flow-line"/>
          </svg>
        </div>

        <!-- Floating data cards (top-right) -->
        <div class="data-card data-card--1" aria-hidden="true">
          <div class="data-card-bar" style="width:60%"></div>
          <div class="data-card-bar" style="width:80%"></div>
          <div class="data-card-bar" style="width:45%"></div>
          <div class="data-card-label">Student #1</div>
        </div>

        <!-- Floating data card (bottom-left) -->
        <div class="data-card data-card--2" aria-hidden="true">
          <div class="data-card-row">
            <div class="data-dot data-dot--active"></div>
            <div class="data-card-text">已录入</div>
          </div>
          <div class="data-card-row">
            <div class="data-dot"></div>
            <div class="data-card-text">审核中</div>
          </div>
          <div class="data-card-row">
            <div class="data-dot"></div>
            <div class="data-card-text">已完成</div>
          </div>
        </div>

        <!-- Binary code decoration (top-left) -->
        <div class="data-binary" aria-hidden="true">
          <span>01100011</span>
          <span>01110101</span>
          <span>00100000</span>
          <span>01110011</span>
        </div>

        <!-- Bar chart (bottom area) -->
        <div class="data-bars" aria-hidden="true">
          <div class="bar-chart-label">Achievement Distribution</div>
          <div class="bar-row">
            <span class="bar-name">竞赛</span>
            <div class="bar-track"><div class="bar-fill" style="width:78%"></div></div>
            <span class="bar-val">78%</span>
          </div>
          <div class="bar-row">
            <span class="bar-name">科研</span>
            <div class="bar-track"><div class="bar-fill bar-fill--purple" style="width:55%"></div></div>
            <span class="bar-val">55%</span>
          </div>
          <div class="bar-row">
            <span class="bar-name">论文</span>
            <div class="bar-track"><div class="bar-fill bar-fill--dark" style="width:42%"></div></div>
            <span class="bar-val">42%</span>
          </div>
          <div class="bar-row">
            <span class="bar-name">专利</span>
            <div class="bar-track"><div class="bar-fill" style="width:28%"></div></div>
            <span class="bar-val">28%</span>
          </div>
        </div>

        <!-- Pie chart decoration (mid-right) -->
        <div class="data-pie" aria-hidden="true">
          <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
            <circle cx="40" cy="40" r="30" class="pie-base"/>
            <path d="M40,40 L40,10 A30,30 0 0,1 68.6,55 Z" class="pie-slice-1"/>
            <path d="M40,40 L68.6,55 A30,30 0 0,1 25,66 Z" class="pie-slice-2"/>
            <path d="M40,40 L25,66 A30,30 0 0,1 10,30 Z" class="pie-slice-3"/>
            <path d="M40,40 L10,30 A30,30 0 0,1 40,10 Z" class="pie-slice-4"/>
          </svg>
          <div class="pie-legend">
            <div class="pie-legend-item"><span class="pie-dot" style="background:#d49c3b"></span>竞赛</div>
            <div class="pie-legend-item"><span class="pie-dot" style="background:#9b59b6"></span>科研</div>
            <div class="pie-legend-item"><span class="pie-dot" style="background:#640c72"></span>论文</div>
            <div class="pie-legend-item"><span class="pie-dot" style="background:rgba(255,255,255,0.25)"></span>其他</div>
          </div>
        </div>

        <!-- Connection lines (network nodes) -->
        <div class="data-connection" aria-hidden="true">
          <svg class="data-conn-svg" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
            <circle cx="100" cy="100" r="6" class="conn-node conn-node--center"/>
            <circle cx="30" cy="40" r="4" class="conn-node"/>
            <circle cx="170" cy="30" r="4" class="conn-node"/>
            <circle cx="160" cy="170" r="4" class="conn-node"/>
            <circle cx="40" cy="165" r="4" class="conn-node"/>
            <line x1="100" y1="100" x2="30" y2="40" class="conn-line"/>
            <line x1="100" y1="100" x2="170" y2="30" class="conn-line"/>
            <line x1="100" y1="100" x2="160" y2="170" class="conn-line"/>
            <line x1="100" y1="100" x2="40" y2="165" class="conn-line"/>
            <line x1="30" y1="40" x2="170" y2="30" class="conn-line conn-line--dashed"/>
            <line x1="170" y1="30" x2="160" y2="170" class="conn-line conn-line--dashed"/>
            <line x1="160" y1="170" x2="40" y2="165" class="conn-line conn-line--dashed"/>
            <line x1="40" y1="165" x2="30" y2="40" class="conn-line conn-line--dashed"/>
          </svg>
        </div>

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
  background: rgba(212, 156, 59, 0.2);
}
.page-orb--2 {
  width: 30rem; height: 30rem;
  bottom: -14rem; left: -8rem;
  background: rgba(100, 12, 114, 0.25);
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
  background: linear-gradient(160deg, #1a0a2e 0%, #2d1050 40%, #640c72 100%);
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
  width: 22rem; height: 22rem;
  top: -7rem; left: -5rem;
  background: rgba(100, 12, 114, 0.7);
  animation: floatOrb 14s ease-in-out infinite;
}
.brand-bg-orb--2 {
  width: 16rem; height: 16rem;
  bottom: -4rem; right: -4rem;
  background: rgba(212, 156, 59, 0.45);
  animation: floatOrb 18s ease-in-out infinite reverse;
  animation-delay: -6s;
}
.brand-bg-orb--3 {
  width: 9rem; height: 9rem;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(212, 156, 59, 0.12);
  animation: pulseOrb 6s ease-in-out infinite;
}

/* ── Data Grid Pattern ── */
.data-grid {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.07;
  pointer-events: none;
}
.data-grid-svg {
  width: 280px;
  height: 320px;
}
.grid-line {
  stroke: #d49c3b;
  stroke-width: 0.8;
  stroke-dasharray: 3 4;
}
.data-node {
  fill: #d49c3b;
  animation: nodePulse 3s ease-in-out infinite;
}
.data-node:nth-child(4) { animation-delay: 0.5s; }
.data-node:nth-child(5) { animation-delay: 1s; }
.data-node:nth-child(6) { animation-delay: 1.5s; }
.data-flow-line {
  fill: none;
  stroke: #d49c3b;
  stroke-width: 1;
  stroke-dasharray: 4 3;
  animation: flowDash 2s linear infinite;
}

/* ── Floating Data Cards ── */
.data-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(212, 156, 59, 0.12);
  border-radius: 12px;
  padding: 12px 14px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.data-card--1 {
  top: 48px;
  right: 32px;
  width: 120px;
}
.data-card--2 {
  bottom: 60px;
  left: 28px;
  width: 110px;
}
.data-card-bar {
  height: 5px;
  border-radius: 3px;
  background: linear-gradient(90deg, rgba(212,156,59,0.7), rgba(212,156,59,0.3));
  margin-bottom: 8px;
  animation: barGrow 4s ease-in-out infinite;
}
.data-card-bar:nth-child(2) { animation-delay: 0.3s; }
.data-card-bar:nth-child(3) { animation-delay: 0.6s; }
.data-card-label {
  font-size: 9px;
  color: rgba(212, 156, 59, 0.35);
  letter-spacing: 0.5px;
  font-family: monospace;
  margin-top: 4px;
}
.data-card-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 7px;
}
.data-card-row:last-child { margin-bottom: 0; }
.data-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  border: 1.5px solid rgba(212, 156, 59, 0.4);
  flex-shrink: 0;
}
.data-dot--active {
  background: #d49c3b;
  border-color: #d49c3b;
  box-shadow: 0 0 8px rgba(212, 156, 59, 0.6);
}
.data-card-text {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 0.3px;
}

/* ── Binary Code Decoration ── */
.data-binary {
  position: absolute;
  bottom: 140px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  opacity: 0.10;
  animation: binaryBlink 8s ease-in-out infinite;
}
.data-binary span {
  font-size: 9px;
  color: #d49c3b;
  font-family: monospace;
  letter-spacing: 1px;
}

/* ── Bar Chart ── */
.data-bars {
  position: absolute;
  bottom: 44px;
  right: 16px;
  width: 160px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(212, 156, 59, 0.08);
  border-radius: 10px;
  padding: 10px 12px 8px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.bar-chart-label {
  font-size: 8px;
  color: rgba(212, 156, 59, 0.35);
  letter-spacing: 0.5px;
  font-family: monospace;
  margin-bottom: 8px;
  text-transform: uppercase;
}
.bar-row {
  display: grid;
  grid-template-columns: 28px 1fr 30px;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}
.bar-row:last-child { margin-bottom: 0; }
.bar-name {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.35);
  font-family: monospace;
  text-align: right;
}
.bar-track {
  height: 5px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 3px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, rgba(212,156,59,0.65), rgba(212,156,59,0.35));
  animation: barGrow 3s ease-in-out infinite;
}
.bar-fill--purple {
  background: linear-gradient(90deg, rgba(155,89,182,0.65), rgba(155,89,182,0.35));
}
.bar-fill--dark {
  background: linear-gradient(90deg, rgba(100,12,114,0.65), rgba(100,12,114,0.35));
}
.bar-val {
  font-size: 8px;
  color: rgba(212, 156, 59, 0.4);
  font-family: monospace;
}

/* ── Pie Chart ── */
.data-pie {
  position: absolute;
  top: 50%;
  right: 16px;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.data-pie svg {
  width: 72px;
  height: 72px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.2));
}
.pie-base { fill: rgba(255,255,255,0.04); }
.pie-slice-1 { fill: #d49c3b; opacity: 0.5; }
.pie-slice-2 { fill: #9b59b6; opacity: 0.5; }
.pie-slice-3 { fill: #640c72; opacity: 0.5; }
.pie-slice-4 { fill: rgba(255,255,255,0.12); }
.pie-legend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 12px;
}
.pie-legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 8px;
  color: rgba(255, 255, 255, 0.35);
  font-family: monospace;
}
.pie-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ── Network Connection Diagram ── */
.data-connection {
  position: absolute;
  top: 80px;
  left: 20px;
  opacity: 0.12;
}
.data-conn-svg {
  width: 140px;
  height: 140px;
}
.conn-node {
  fill: rgba(212, 156, 59, 0.5);
}
.conn-node--center {
  fill: #d49c3b;
  filter: drop-shadow(0 0 6px rgba(212,156,59,0.8));
}
.conn-line {
  stroke: rgba(212, 156, 59, 0.3);
  stroke-width: 1;
}
.conn-line--dashed {
  stroke-dasharray: 3 3;
}

/* ── Data Animations ── */
@keyframes nodePulse {
  0%, 100% { opacity: 1; r: 3; }
  50% { opacity: 0.5; r: 4; }
}
@keyframes flowDash {
  to { stroke-dashoffset: -21; }
}
@keyframes barGrow {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}
@keyframes binaryBlink {
  0%, 90%, 100% { opacity: 0.10; }
  95% { opacity: 0.04; }
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
}

.brand-emblem-ring {
  position: absolute;
  inset: 20px;
  border-radius: 50%;
  border: 2px solid rgba(212, 156, 59, 0.55);
  animation: spinRing 20s linear infinite;
}
.brand-emblem-ring::before {
  content: "";
  position: absolute;
  top: -5px; left: 50%;
  transform: translateX(-50%);
  width: 10px; height: 10px;
  border-radius: 50%;
  background: #d49c3b;
  box-shadow: 0 0 16px rgba(212, 156, 59, 0.9), 0 0 32px rgba(212,156,59,0.4);
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
  text-shadow: 0 2px 20px rgba(0,0,0,0.3);
  line-height: 1.3;
}

.brand-tagline {
  font-size: 17px;
  color: rgba(212, 156, 59, 0.95);
  letter-spacing: 4px;
  font-weight: 600;
  text-shadow: 0 0 20px rgba(212, 156, 59, 0.4);
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
  background: linear-gradient(135deg, #640c72, #3d0770);
  box-shadow: 0 4px 16px rgba(100, 12, 114, 0.38);
  transition: transform 220ms ease, box-shadow 220ms ease, filter 220ms ease;
}
.action-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(100, 12, 114, 0.48);
  filter: brightness(1.05);
}
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
