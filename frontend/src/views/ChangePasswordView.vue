<template>
  <div class="auth-layout change-password-layout">
    <div class="auth-card change-password-card">
      <div class="auth-brand">
        <h1 class="auth-title">修改初始密码</h1>
        <p class="auth-subtitle">
          为保障账号安全，首次使用管理员下发的密码登录后，请先设置个人新密码。
        </p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-row">
          <label class="form-label" for="oldPassword">当前密码</label>
          <input
            id="oldPassword"
            v-model="form.oldPassword"
            class="form-input"
            type="password"
            autocomplete="current-password"
            placeholder="请输入当前密码"
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
          />
        </div>

        <p v-if="errorMessage" class="feedback error">{{ errorMessage }}</p>

        <button class="action-button auth-submit" type="submit" :disabled="loading">
          {{ loading ? "保存中..." : "确认修改" }}
        </button>
      </form>

      <button class="change-password-logout" type="button" @click="handleLogout">
        退出登录
      </button>
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
.change-password-layout {
  padding: 24px 16px;
}

.change-password-card {
  width: min(440px, calc(100vw - 32px));
}

.auth-brand {
  margin-bottom: 24px;
  text-align: center;
}

.auth-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--primary-dark);
}

.auth-subtitle {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-sub);
}

.auth-form {
  display: grid;
  gap: 14px;
}

.auth-submit {
  width: 100%;
  margin-top: 4px;
}

.change-password-logout {
  display: block;
  width: 100%;
  margin-top: 16px;
  padding: 10px;
  border: none;
  background: transparent;
  color: var(--text-sub);
  font-size: 13px;
  cursor: pointer;
}

.change-password-logout:hover {
  color: var(--primary);
}
</style>
