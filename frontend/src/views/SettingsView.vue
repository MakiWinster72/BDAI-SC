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
import StudentExportDialog from "../components/StudentExportDialog.vue";
import { useStudentPdfExport } from "../composables/useStudentPdfExport";
import { getMenuLocation, isMenuEnabled } from "../constants/menu";
import { getStudentProfile } from "../api/profile";
import { API_BASE } from "../api/request";
import { changePassword } from "../api/auth";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";

const router = useRouter();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const { exportResumePdf } = useStudentPdfExport();
const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const sidebarOpen = ref(false);
const showPasswordForm = ref(false);
const showExportOptions = ref(false);
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

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

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
}

async function handleExportPdfFromSettings() {
  const rows = await loadExportRows();
  await handleExportPdf(rows);
}

function handleLogout() {
  localStorage.removeItem("gcsc_token");
  localStorage.removeItem("gcsc_user");
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
.settings-shell {
  min-height: unset;
}

.settings-card:hover {
  border-color: rgba(3, 107, 114, 0.28);
}

.settings-row.clickable {
  cursor: pointer;
  border-radius: 12px;
  transition: background 160ms ease;
}

.settings-row.clickable:hover {
  background: rgba(3, 107, 114, 0.06);
}

.chevron {
  width: 18px;
  height: 18px;
  color: #5b7680;
  transition: transform 280ms ease;
  flex-shrink: 0;
}

.chevron.open {
  transform: rotate(180deg);
}

.settings-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #6a8087;
}

.settings-expand {
  display: grid;
  gap: 10px;
  padding-top: 14px;
  margin-top: 4px;
  border-top: 1px solid rgba(3, 107, 114, 0.1);
}

.settings-expand:hover {
  border-color: rgba(3, 107, 114, 0.28);
}

.pw-input {
  width: 100%;
  box-sizing: border-box;
  box-shadow: none !important;
}

.pw-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
  align-items: center;
}

.pw-btn {
  height: 36px !important;
  padding: 0 18px !important;
  border-radius: 10px !important;
  font-size: 14px !important;
  margin-top: 0 !important;
  width: auto !important;
}

.pw-btn.ghost-button {
  background: rgba(255, 255, 255, 0.5) !important;
}

.pw-btn.action-button {
  background: #036b72 !important;
  color: #fff !important;
  border: none !important;
}

.export-options {
  gap: 12px;
}

.export-option-button {
  width: 100%;
  min-height: 42px;
  border: 1px solid rgba(3, 107, 114, 0.16);
  background: rgba(255, 255, 255, 0.72);
  color: #0f4d55;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 160ms ease,
    border-color 160ms ease,
    transform 160ms ease;
}

.export-option-button:hover {
  background: rgba(205, 255, 249, 0.72);
  border-color: rgba(3, 107, 114, 0.3);
}

.export-option-button:active {
  transform: scale(0.99);
}

.expand-enter-active,
.expand-leave-active {
  transition:
    opacity 240ms ease,
    max-height 280ms ease;
  max-height: 320px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 320px;
}

</style>
