<template>
  <div class="auth-layout">
    <div class="auth-card">
      <h1 class="auth-title">创建账号</h1>
      <p class="auth-subtitle">注册后即可登录 GCSC 学生中心</p>

      <form @submit.prevent="handleRegister">
        <div class="form-row">
          <label class="form-label" for="displayName">名字</label>
          <input
            id="displayName"
            v-model.trim="form.displayName"
            class="form-input"
            type="text"
            placeholder="请输入真实名字"
            autocomplete="nickname"
            required
          />
        </div>

        <div class="form-row">
          <label class="form-label" for="username"> 学号 </label>
          <input
            id="username"
            v-model.trim="form.username"
            class="form-input"
            type="text"
            placeholder="请输入学号"
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
            placeholder="至少 6 位"
            autocomplete="new-password"
            required
          />
        </div>

        <button class="action-button" :disabled="isSubmitting" type="submit">
          {{ isSubmitting ? "注册中..." : "注册" }}
        </button>
      </form>

      <div :class="['feedback', feedback.type]">{{ feedback.text }}</div>

      <div class="switch-line">
        已有账号？
        <RouterLink class="switch-link" to="/login">去登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { register } from "../api/auth";

const router = useRouter();

const form = reactive({
  displayName: "",
  username: "",
  password: "",
});

const isSubmitting = ref(false);
const feedback = reactive({ text: "", type: "" });

function parseError(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  return "请求失败，请检查后端服务是否启动";
}

async function handleRegister() {
  feedback.text = "";
  feedback.type = "";
  isSubmitting.value = true;

  try {
    const { data } = await register(form);
    feedback.text = data.message || "注册成功";
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

    setTimeout(() => {
      router.push("/myinfos");
    }, 600);
  } catch (error) {
    feedback.text = parseError(error);
    feedback.type = "error";
  } finally {
    isSubmitting.value = false;
  }
}
</script>
