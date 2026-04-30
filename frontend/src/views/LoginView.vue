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
            <div class="pie-legend-item"><span class="pie-dot" style="background:#FF85BB"></span>竞赛</div>
            <div class="pie-legend-item"><span class="pie-dot" style="background:#FFCEE3"></span>科研</div>
            <div class="pie-legend-item"><span class="pie-dot" style="background:var(--primary)"></span>论文</div>
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

          <div v-if="allowRegistration" class="switch-line">
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
import { getSystemSettings } from "../api/admin";
import { useToast } from "../composables/useToast";

const route = useRoute();
const router = useRouter();
const toast = useToast();

const form = reactive({
  username: route.query.username ? String(route.query.username) : "",
  password: "",
});

const isSubmitting = ref(false);
const feedback = reactive({ text: "", type: "" });
const showPassword = ref(false);
const usernameError = ref("");
const allowRegistration = ref(false);

async function fetchSystemSettings() {
  try {
    const res = await getSystemSettings();
    allowRegistration.value = res.data.allowRegistration === true;
    localStorage.setItem('gcsc_allowRegistration', allowRegistration.value ? '1' : '0');
  } catch (e) {
    allowRegistration.value = false;
    localStorage.setItem('gcsc_allowRegistration', '0');
  }
}
fetchSystemSettings();

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

    if (data.lastLoginInfo) {
      const { ipAddress, deviceName, loginTime } = data.lastLoginInfo;
      const time = loginTime ? new Date(loginTime).toLocaleString("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" }) : "未知";
      toast.info(`上次登录：${deviceName || "未知设备"} · ${ipAddress || "未知IP"} · ${time}`);
    } else {
      toast.info("欢迎使用！请先去「个人信息」填写你的个人资料～");
    }

    localStorage.setItem(
      "bdai_sc_user",
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
    localStorage.setItem("bdai_sc_token", data.token || "");
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
@import '../assets/styles/login-view.css';
</style>
