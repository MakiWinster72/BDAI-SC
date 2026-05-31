<template>
  <div class="auth-layout change-password-page">
    <div class="page-orb page-orb--1" aria-hidden="true"></div>
    <div class="page-orb page-orb--2" aria-hidden="true"></div>

    <div class="split-panel">
      <div class="brand-panel" aria-hidden="true">
        <div class="brand-bg-orb brand-bg-orb--1"></div>
        <div class="brand-bg-orb brand-bg-orb--2"></div>
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

      <div class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <h1 class="form-title">修改初始密码</h1>
            <p class="form-subtitle">
              使用管理员下发的密码登录后，请先设置个人新密码再进入系统
            </p>
          </div>

          <form @submit.prevent="handleSubmit" novalidate>
            <div class="form-row">
              <label class="form-label" for="oldPassword">当前密码</label>
              <input
                id="oldPassword"
                v-model="form.oldPassword"
                class="form-input"
                type="password"
                autocomplete="current-password"
                placeholder="请输入当前密码"
                required
              />
            </div>

            <div class="form-row">
              <label class="form-label" for="newPassword">新密码</label>
              <input
                id="newPassword"
                v-model="form.newPassword"
                class="form-input"
                type="password"
                autocomplete="new-password"
                placeholder="6-32 位"
                required
              />
            </div>

            <div class="form-row">
              <label class="form-label" for="confirmPassword">确认新密码</label>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                class="form-input"
                type="password"
                autocomplete="new-password"
                placeholder="再次输入新密码"
                required
              />
            </div>

            <p v-if="errorMessage" class="feedback error" role="alert">
              {{ errorMessage }}
            </p>

            <button
              class="action-button"
              type="submit"
              :disabled="loading"
            >
              {{ loading ? "保存中..." : "确认修改" }}
            </button>
          </form>

          <p class="switch-line">
            <button
              class="switch-link change-password-logout-btn"
              type="button"
              @click="handleLogout"
            >
              退出登录
            </button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { changePassword } from "@/api/auth";
import { useToast } from "@/composables/useToast";
import { loadUser, saveUser } from "@/utils/userStorage";

const router = useRouter();
const { success: toastSuccess } = useToast();

const form = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const loading = ref(false);
const errorMessage = ref("");

function handleLogout() {
  localStorage.removeItem("bdai_sc_token");
  localStorage.removeItem("bdai_sc_user");
  router.replace("/login");
}

async function handleSubmit() {
  errorMessage.value = "";

  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
    errorMessage.value = "请填写完整";
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = "两次输入的新密码不一致";
    return;
  }
  if (form.newPassword.length < 6 || form.newPassword.length > 32) {
    errorMessage.value = "新密码长度须为 6-32 位";
    return;
  }
  if (form.newPassword === form.oldPassword) {
    errorMessage.value = "新密码不能与当前密码相同";
    return;
  }

  loading.value = true;
  await changePassword({
    oldPassword: form.oldPassword,
    newPassword: form.newPassword,
  })
    .then(() => {
      const user = loadUser();
      saveUser({ ...user, mustChangePassword: false });
      toastSuccess("密码修改成功");
      router.replace("/myinfos");
    })
    .catch((err) => {
      errorMessage.value =
        err?.response?.data?.message || "修改失败，请检查当前密码是否正确";
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<style scoped>
@import "@/assets/styles/login-view.css";

.change-password-logout-btn {
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  cursor: pointer;
}

.feedback.error {
  margin: 0 0 12px;
  font-size: 13px;
}
</style>
