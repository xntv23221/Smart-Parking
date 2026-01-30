<template>
  <div class="login-container">
    <div class="login-box">
      <div class="header">
        <div class="logo-area">
          <van-icon name="guide-o" size="48" color="#1989fa" />
        </div>
        <h2 class="title">欢迎回来</h2>
        <p class="subtitle">登录以继续使用智慧停车服务</p>
      </div>

      <div class="form-area">
        <!-- 登录表单 -->
        <div class="input-group">
          <van-field
            v-model.trim="loginForm.username"
            left-icon="user-o"
            placeholder="请输入用户名"
            class="custom-field"
            :border="false"
          />
          <van-field
            v-model="loginForm.password"
            left-icon="lock"
            type="password"
            placeholder="请输入密码"
            class="custom-field"
            :border="false"
          />
        </div>
        
        <div class="forgot-pwd">
          <span @click="onForgotPassword">忘记密码?</span>
        </div>

        <div class="action-btn">
          <van-button 
            type="primary" 
            block 
            round 
            color="linear-gradient(to right, #4bb0ff, #6149f6)"
            :loading="loading" 
            size="large"
            @click="onLogin"
          >
            登 录
          </van-button>
        </div>
      </div>

      <div class="footer-link">
        <span>
          还没有账号? <span class="link" @click="router.push('/register')">立即注册</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { useAuthStore } from "../stores/auth";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const auth = useAuthStore();

const loading = ref(false);

const loginForm = reactive({
  username: "",
  password: ""
});

function onForgotPassword() {
  showToast("请联系管理员重置密码");
}

function validateUsername(username: string) {
  return username.length >= 3 && username.length <= 64;
}

function validatePassword(password: string) {
  return password.length >= 6 && password.length <= 128;
}

async function onLogin() {
  if (!validateUsername(loginForm.username) || !validatePassword(loginForm.password)) {
    showToast("请输入正确的用户名和密码");
    return;
  }
  loading.value = true;
  try {
    await auth.login(loginForm.username, loginForm.password);
    await router.replace("/home");
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "登录失败"));
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-area {
  width: 80px;
  height: 80px;
  background: rgba(25, 137, 250, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.title {
  font-size: 24px;
  color: #323233;
  margin: 0 0 8px;
  font-weight: 600;
}

.subtitle {
  font-size: 14px;
  color: #969799;
  margin: 0;
}

.input-group {
  margin-bottom: 20px;
}

.custom-field {
  background: #f7f8fa;
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
}

.forgot-pwd {
  text-align: right;
  margin-bottom: 24px;
}

.forgot-pwd span {
  font-size: 13px;
  color: #969799;
  cursor: pointer;
}

.action-btn {
  margin-bottom: 24px;
}

.footer-link {
  text-align: center;
  font-size: 14px;
  color: #646566;
}

.link {
  color: #1989fa;
  cursor: pointer;
  font-weight: 500;
  margin-left: 4px;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
