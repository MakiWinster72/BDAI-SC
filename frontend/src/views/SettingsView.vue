<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">设置</h1>
    </header>

    <section class="info-shell settings-shell">
      <div class="settings-card">
        <div class="settings-row clickable" @click="togglePasswordChange">
          <div class="settings-text">
            <div class="settings-title">修改密码</div>
          </div>
          <svg
            class="chevron"
            :class="{ open: showPasswordForm }"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path d="M6 9l6 6 6-6" />
          </svg>
        </div>
        <transition name="expand">
          <div v-if="showPasswordForm" class="settings-expand pw-fields">
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              class="info-input pw-input"
              placeholder="旧密码"
            />
            <input
              v-model="passwordForm.newPassword"
              type="password"
              class="info-input pw-input"
              placeholder="新密码（6-32位）"
            />
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              class="info-input pw-input"
              placeholder="确认新密码"
            />
            <p v-if="passwordError" class="form-tip">{{ passwordError }}</p>
            <p v-if="passwordSuccess" class="feedback success">
              {{ passwordSuccess }}
            </p>
            <div class="pw-actions">
              <button
                class="ghost-button pw-btn"
                type="button"
                @click="cancelPasswordChange"
              >
                取消
              </button>
              <button
                class="action-button pw-btn"
                type="button"
                :disabled="passwordLoading"
                @click="handleChangePassword"
              >
                {{ passwordLoading ? "保存中..." : "保存" }}
              </button>
            </div>
          </div>
        </transition>
      </div>
      <div class="settings-card">
        <div class="settings-row clickable" @click="toggleExportOptions">
          <div class="settings-text">
            <div class="settings-title">导出信息</div>
            <div class="settings-subtitle">展开后选择个人信息导出格式</div>
          </div>
          <svg
            class="chevron"
            :class="{ open: showExportOptions }"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path d="M6 9l6 6 6-6" />
          </svg>
        </div>
        <transition name="expand">
          <div v-if="showExportOptions" class="settings-expand export-options">
            <button
              class="export-option-button"
              type="button"
              @click="openExcelExportDialog"
            >
              个人信息(Excel)
            </button>
            <button
              class="export-option-button"
              type="button"
              @click="handleExportPdfFromSettings"
            >
              个人信息(PDF)
            </button>
          </div>
        </transition>
      </div>
      <div class="settings-card">
        <div class="settings-row clickable" @click="toggleLoginHistory">
          <div class="settings-text">
            <div class="settings-title">登录历史</div>
            <div class="settings-subtitle">查看账号的登录记录</div>
          </div>
          <svg
            class="chevron"
            :class="{ open: showLoginHistory }"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path d="M6 9l6 6 6-6" />
          </svg>
        </div>
        <transition name="expand">
          <div v-if="showLoginHistory" class="settings-expand login-history-list">
            <div v-if="loginHistoryLoading" class="login-history-loading">加载中...</div>
            <template v-else-if="loginHistory.length > 0">
              <div
                v-for="item in loginHistory"
                :key="item.loginTime"
                class="login-history-item"
              >
                <div class="login-history-device">
                  <img
                    class="device-icon"
                    :src="browserIconSrc(item.browser)"
                    :alt="item.browser || '未知浏览器'"
                    :title="item.browser || '未知浏览器'"
                  />
                  <img
                    class="device-icon"
                    :src="osIconSrc(item.os)"
                    :alt="item.os || '未知系统'"
                    :title="item.os || '未知系统'"
                  />
                  {{ item.deviceName }}
                </div>
                <div class="login-history-meta">
                  <span class="login-history-ip">{{ item.ipAddress }}</span>
                  <span class="login-history-time">{{ formatTime(item.loginTime) }}</span>
                </div>
              </div>
              <PaginationBar
                v-model:current-page="loginHistoryPage"
                v-model:page-size="loginHistorySize"
                :total-pages="loginHistoryTotalPages"
                :page-size-options="[10, 20, 50]"
                mode="simple"
                @update:current-page="loadLoginHistory"
                @update:page-size="onLoginHistorySizeChange"
              />
            </template>
            <div v-else class="login-history-empty">
              暂无登录记录
            </div>
          </div>
        </transition>
      </div>
      <div class="settings-actions">
        <button class="settings-action" type="button" @click="handleLogout">
          退出登录
        </button>
      </div>
    </section>

    <StudentExportDialog
      :open="exportDialogOpen"
      filename-prefix="student_profile_export"
      preview-title="导出预览"
      empty-message="没有获取到个人信息，请稍后再试。"
      :preview-limit="1"
      :enable-pdf="true"
      :export-pdf="handleExportPdf"
      :load-rows="loadExportRows"
      @close="closeExportDialog"
    />

    <div class="mobile-capsule">
      <div class="capsule-left">
        <div
          class="capsule-action"
          role="button"
          tabindex="0"
          @click="openDashboardSidebar"
        >
          <span class="capsule-icon" aria-hidden="true">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </span>
        </div>
      </div>
      <div class="capsule-right"></div>
    </div>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import PaginationBar from "../components/PaginationBar.vue";
import StudentExportDialog from "../components/StudentExportDialog.vue";
import { useStudentPdfExport } from "../composables/useStudentPdfExport";
import { getMenuLocation, isMenuEnabled } from "../constants/menu";
import { getStudentProfile } from "../api/profile";
import { API_BASE } from "../api/request";
import { changePassword, getLoginHistory } from "../api/auth";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";
import { useToast } from "../composables/useToast";
import { resolveMediaUrl } from "../utils/media";
import { loadUser } from "../utils/userStorage";

const router = useRouter();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const { exportResumePdf } = useStudentPdfExport();
const { success: toastSuccess } = useToast();
const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const sidebarOpen = ref(false);
const showPasswordForm = ref(false);
const showExportOptions = ref(false);
const showLoginHistory = ref(false);
const loginHistory = ref([]);
const loginHistoryLoading = ref(false);
const loginHistoryPage = ref(1);
const loginHistorySize = ref(20);
const loginHistoryTotalPages = ref(1);
const exportDialogOpen = ref(false);
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordError = ref("");
const passwordSuccess = ref("");
const passwordLoading = ref(false);

const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  navigateWithViewTransition(router, getMenuLocation(key));
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}

function openExportDialog() {
  exportDialogOpen.value = true;
}

function closeExportDialog() {
  exportDialogOpen.value = false;
}

function openExcelExportDialog() {
  exportDialogOpen.value = true;
  toastSuccess("个人信息已准备就绪，请确认导出");
}

async function loadExportRows() {
  const { data } = await getStudentProfile();
  return data ? [data] : [];
}

async function handleExportPdf(rows) {
  const student = Array.isArray(rows) ? rows[0] : null;
  if (!student) {
    throw new Error("没有获取到个人信息，请稍后再试。");
  }
  await exportResumePdf({
    student,
    resolveMediaUrl,
  });
  toastSuccess("PDF 导出成功！");
}

async function handleExportPdfFromSettings() {
  const rows = await loadExportRows();
  await handleExportPdf(rows);
}

function handleLogout() {
  localStorage.removeItem("bdai_sc_token");
  localStorage.removeItem("bdai_sc_user");
  router.push("/login");
}

function togglePasswordChange() {
  if (!showPasswordForm.value) {
    showExportOptions.value = false;
  }
  showPasswordForm.value = !showPasswordForm.value;
  if (!showPasswordForm.value) {
    cancelPasswordChange();
  }
}

function toggleExportOptions() {
  if (!showExportOptions.value) {
    showPasswordForm.value = false;
    cancelPasswordChange();
  }
  showExportOptions.value = !showExportOptions.value;
}

function toggleLoginHistory() {
  if (!showLoginHistory.value) {
    showPasswordForm.value = false;
    showExportOptions.value = false;
    cancelPasswordChange();
    loadLoginHistory();
  }
  showLoginHistory.value = !showLoginHistory.value;
}

async function loadLoginHistory() {
  loginHistoryLoading.value = true;
  try {
    const page = loginHistoryPage.value - 1;
    const { data } = await getLoginHistory(page, loginHistorySize.value);
    loginHistory.value = data.content || [];
    loginHistoryTotalPages.value = Math.max(1, data.totalPages || 1);
  } catch {
    loginHistory.value = [];
  } finally {
    loginHistoryLoading.value = false;
  }
}

function onLoginHistorySizeChange(newSize) {
  loginHistorySize.value = newSize;
  loginHistoryPage.value = 1;
  loadLoginHistory();
}

function formatTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const pad = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

const ICON_BASE = "/assets/icons";

const BROWSER_ICON_MAP = {
  Chrome: "Chrome",
  Safari: "Safari",
  Firefox: "Firefox",
  Edge: "Edge",
  Opera: "opera",
  Brave: "brave",
};

const OS_ICON_MAP = {
  Windows: "Windows",
  macOS: "macos",
  Linux: "linux",
  Android: "android",
  iOS: "macos",
};

function browserIconSrc(browser) {
  const file = BROWSER_ICON_MAP[browser];
  return file ? `${ICON_BASE}/${file}.svg` : "";
}

function osIconSrc(os) {
  const file = OS_ICON_MAP[os];
  return file ? `${ICON_BASE}/${file}.svg` : "";
}

function cancelPasswordChange() {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordError.value = "";
  passwordSuccess.value = "";
}

async function handleChangePassword() {
  passwordError.value = "";
  passwordSuccess.value = "";

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = "两次输入的新密码不一致";
    return;
  }

  if (passwordForm.newPassword.length < 6) {
    passwordError.value = "新密码长度不能少于6位";
    return;
  }

  passwordLoading.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    passwordSuccess.value = "密码修改成功";
    toastSuccess("密码修改成功");
    setTimeout(() => {
      cancelPasswordChange();
      showPasswordForm.value = false;
    }, 1500);
  } catch (err) {
    passwordError.value =
      err.response?.data?.message || "修改失败，请检查旧密码是否正确";
  } finally {
    passwordLoading.value = false;
  }
}
</script>

<style scoped>
@import '../assets/styles/settings-view.css';
</style>
