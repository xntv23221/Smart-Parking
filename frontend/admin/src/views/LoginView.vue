<template>
  <div class="page">
    <div class="header">
      <div class="header-content">
        <img src="/logo.png" alt="Logo" class="logo" />
        <span class="title-separator">|</span>
        <span class="sub-title">智慧停车</span>
      </div>
    </div>
    
    <div class="main">
      <div class="illustration">
        <img src="/star.png" alt="Parking Illustration" />
      </div>
      
      <div class="login-box" :class="{ 'register-mode': tab === 'register' }">
        <el-tabs v-model="tab" class="tabs" :stretch="true">
          <el-tab-pane label="账号密码登录" name="login">
            <div class="login-form-container">
              <el-form :model="loginForm" label-position="top" @submit.prevent>
                <el-form-item>
                  <el-input 
                    v-model.trim="loginForm.username" 
                    placeholder="请输入用户名" 
                    autocomplete="username" 
                    size="large"
                    class="custom-input"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-input 
                    v-model="loginForm.password" 
                    type="password" 
                    placeholder="请输入密码" 
                    autocomplete="current-password" 
                    show-password 
                    size="large"
                    class="custom-input"
                  />
                </el-form-item>
                
                <el-form-item>
                  <div class="form-footer">
                    <el-checkbox v-model="loginForm.remember">是否自动登录</el-checkbox>
                    <el-button type="text" class="btn-register-link" @click="switchToRegister">没有账号？点我注册</el-button>
                  </div>
                </el-form-item>
  
                <el-button type="primary" :loading="loading" class="btn-submit" @click="onLogin">登录</el-button>
              </el-form>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="企业微信扫码" name="qrcode">
            <div class="qrcode-container">
              <div class="qrcode-header">
                <el-icon class="wechat-icon"><ChatDotRound /></el-icon>
                <span>企业微信登录</span>
              </div>
              <div class="qrcode-box">
                <img src="/qrcode.png" alt="Scan QRCode" class="qrcode-img" />
              </div>
              <div class="qrcode-footer">
                 <p>请使用企业微信扫描二维码登录</p>
                 <p>“OA”</p>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="用户注册" name="register" v-if="tab === 'register'">
            <div class="register-container">
              <div class="standalone-header">
                <div class="standalone-title-wrap">
                  <span class="standalone-title">用户注册</span>
                  <div class="standalone-bar"></div>
                </div>
              </div>
              <el-form :model="registerForm" label-position="top" @submit.prevent>
                <el-form-item>
                  <el-input 
                    v-model.trim="registerForm.username" 
                    placeholder="请输入用户名" 
                    autocomplete="username" 
                    size="large"
                    class="custom-input"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-input 
                    v-model="registerForm.password" 
                    type="password" 
                    placeholder="请输入密码" 
                    autocomplete="new-password" 
                    show-password 
                    size="large"
                    class="custom-input"
                  />
                </el-form-item>
                
                <el-form-item>
                <el-input 
                  v-model.trim="registerForm.invitationCode" 
                  placeholder="请输入安全密钥" 
                  autocomplete="off" 
                  size="large"
                  class="custom-input"
                />
              </el-form-item>
  
                <el-form-item>
                  <div class="captcha-container">
                    <el-input 
                      v-model="registerForm.captcha" 
                      placeholder="请输入验证码" 
                      size="large"
                      class="custom-input"
                    />
                    <canvas ref="captchaCanvas" width="100" height="40" @click="refreshCaptcha" class="captcha-img"></canvas>
                  </div>
                </el-form-item>
  
                <div class="form-footer" style="justify-content: flex-end;">
                  <el-button type="text" @click="tab = 'login'">返回登录</el-button>
                </div>
                
                <el-button type="primary" :loading="loading" class="btn-submit" @click="onRegister">注册并登录</el-button>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="slogan-area">
          <h3 class="slogan">PARK SMART NOW</h3>
        </div>
      </div>
    </div>

    <div class="loginDown">
      <div class="footer-content">
        <div class="footer-links">
          <span>帮助 | 隐私 | 条款</span>
        </div>
        <div class="footer-info">
          <span>Copyright © 2025 - 至今 Smart Parking 版权所有</span>
          <span class="divider"></span>
          <a target="_blank" href="https://beian.miit.gov.cn" style="color:#848585">ICP备案 黑ICP备XXXXXXXX号</a>
          <span class="divider"></span>
          <a target="_blank" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=XXXXXXXXXXXXXX号">
            <img src="../assets/login/gonganlogo.png" style="margin-right:4px" />
            <p style="display:inline-block;color:#848585;margin:0">黑公网安备 XXXXXXXXXXXXXX号</p>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ChatDotRound } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const auth = useAuthStore();

const tab = ref<"login" | "qrcode" | "register">("login");
const loading = ref(false);
const captchaCanvas = ref<HTMLCanvasElement | null>(null);

const loginForm = reactive({
  username: "",
  password: "",
  remember: false
});

const registerForm = reactive({
  username: "",
  password: "",
  invitationCode: "",
  captcha: "",
  generatedCaptcha: ""
});

/**
 * 生成随机验证码字符串
 * @returns {string} 4位随机验证码
 */
function generateCaptcha() {
  const chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  let captcha = "";
  for (let i = 0; i < 4; i++) {
    captcha += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return captcha;
}

/**
 * 绘制验证码图片到Canvas
 */
function drawCaptcha() {
  const canvas = captchaCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  const width = canvas.width;
  const height = canvas.height;

  ctx.clearRect(0, 0, width, height);
  ctx.fillStyle = "#f0f2f5";
  ctx.fillRect(0, 0, width, height);

  const captcha = generateCaptcha();
  registerForm.generatedCaptcha = captcha;

  ctx.font = "bold 24px Arial";
  ctx.fillStyle = "#333";
  ctx.textBaseline = "middle";
  ctx.textAlign = "center";
  
  // 添加干扰线
  for(let i=0; i<5; i++) {
      ctx.strokeStyle = `rgba(${Math.random()*255},${Math.random()*255},${Math.random()*255},0.5)`;
      ctx.beginPath();
      ctx.moveTo(Math.random()*width, Math.random()*height);
      ctx.lineTo(Math.random()*width, Math.random()*height);
      ctx.stroke();
  }

  ctx.fillText(captcha, width / 2, height / 2);
}

/**
 * 刷新验证码
 */
function refreshCaptcha() {
  drawCaptcha();
  registerForm.captcha = "";
}

/**
 * 切换到注册标签页
 */
function switchToRegister() {
  tab.value = "register";
}

watch(tab, (newTab) => {
  if (newTab === 'register') {
    nextTick(() => {
      drawCaptcha();
    });
  }
});

/**
 * 校验用户名
 * @param username 用户名
 */
function validateUsername(username: string) {
  return username.length >= 3 && username.length <= 64;
}

/**
 * 校验密码
 * @param password 密码
 */
function validatePassword(password: string) {
  return password.length >= 6 && password.length <= 128;
}

/**
 * 处理登录逻辑
 */
async function onLogin() {
  if (!validateUsername(loginForm.username) || !validatePassword(loginForm.password)) {
    ElMessage.error("请输入正确的用户名和密码");
    return;
  }
  
  loading.value = true;
  try {
    await auth.login(loginForm.username, loginForm.password);
    await router.replace("/messages");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "登录失败"));
  } finally {
    loading.value = false;
  }
}

/**
 * 处理注册逻辑
 */
async function onRegister() {
  if (!validateUsername(registerForm.username) || !validatePassword(registerForm.password)) {
    ElMessage.error("请输入正确的用户名和密码");
    return;
  }
  
  if (registerForm.invitationCode !== 'admin') {
    ElMessage.error("安全密钥错误");
    return;
  }

  if (registerForm.captcha.toLowerCase() !== registerForm.generatedCaptcha.toLowerCase()) {
      ElMessage.error("验证码错误");
      refreshCaptcha();
      return;
  }

  loading.value = true;
  try {
    await auth.register(registerForm.username, registerForm.password, "ADMIN", null);
    await router.replace("/messages");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "注册失败"));
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: white;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.header {
  padding: 20px 200px; /* 增加左侧内边距，使Logo区域右移 */
  background: white;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 8px; /* 缩小间距 */
}

.logo {
  height: 40px; 
  width: auto;
}

.title-separator {
  font-size: 20px;
  color: #999;
  margin: 0 2px; /* 缩小竖线间距 */
}

.sub-title {
  font-size: 18px;
  color: #999;
  font-weight: normal;
}

.main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 200px; /* 减小间距使登录框左移 */
  background-color: white;
}

.illustration img {
  max-width: 650px; /* 放大图片 */
  height: auto;
  margin-left: 150px; /* 图片向右移动 */
  transform: translateY(-50px); /* 向上移动 */
}

.login-box {
  width: 350px; /* 减小登录框宽度 */
  background: #ecf5ff;
  padding: 40px 40px 40px; /* 恢复顶部内边距 */
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 确保底部圆角被内部元素遵循 */
}

:deep(.el-input__inner) {
  font-size: 16px !important;
  height: 45px !important; /* 放大输入框高度 */
  color: #333 !important; /* 加深输入字体颜色 */
}

.custom-input :deep(.el-input__wrapper) {
  padding: 5px 15px; /* 增加内边距 */
  border-radius: 12px !important; /* 使边角更加圆润 */
}

/* 注册模式下隐藏 Tabs 头部 - 已启用 */
.register-mode :deep(.el-tabs__header) {
  display: none;
}

.btn-register-link {
  padding-right: 0;
  transform: translateX(-5px); /* 使用 transform 强制向右移动 */
}

.tabs {
  margin-top: -25px; /* 标识保持靠上 */
  margin-bottom: 20px;
}

/* 精准控制标签页头部（标识）与下方内容（输入框）之间的间距 */
:deep(.el-tabs__header) {
  margin-bottom: 20px !important; /* 恢复默认间距 */
}

/* 仅在登录表单容器中增加顶部间距，实现“账号密码登录”页面的间距增大 */
.login-form-container {
  margin-top: 10px; /* 从 40px 缩小到 30px */
}

.register-container {
  /* 注册容器不需要额外 margin-top，因为有 standalone-header */
}

/* 独立的注册标题样式，完全复刻 Tab 风格 */
.standalone-header {
  margin-bottom: 20px; /* 匹配 .tabs 的 margin-bottom */
  text-align: center;
  position: relative;
  margin-top: 0; /* 移除负 margin，让标识在边框内下降显示 */
  height: 40px; /* 匹配 el-tabs 默认高度 */
  display: flex;
  justify-content: center;
}

.standalone-title-wrap {
  position: relative;
  display: inline-block;
  padding: 0 20px;
}

.standalone-title {
  font-size: 18px; /* 匹配 .el-tabs__item */
  font-weight: bold;
  color: #333; /* 匹配 .el-tabs__item.is-active */
  line-height: 40px;
}

.standalone-bar {
  background-color: #409eff; /* 匹配 .el-tabs__active-bar */
  height: 3px;
  width: 100%;
  position: absolute;
  bottom: 0; /* 贴在底部 */
  left: 0;
  border-radius: 2px;
}

/* 注册表单容器间距调整 */
.register-container .el-form {
  margin-top: 30px; /* 匹配登录页的 login-form-container margin-top */
}

:deep(.el-tabs__item) {
  font-size: 18px;
  font-weight: bold;
  color: #666;
}

:deep(.el-tabs__item.is-active) {
  color: #333;
}

:deep(.el-tabs__active-bar) {
  background-color: #409eff;
  height: 3px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.captcha-container .el-input {
  flex: 1;
}

.captcha-img {
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 12px; /* 匹配输入框的圆角 */
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 15px;
  width: 100%; /* 确保占满宽度 */
}

:deep(.el-checkbox__label) {
  font-size: 15px;
}

.btn-submit {
  width: 100%;
  height: 45px;
  font-size: 18px;
  background-color: #409eff;
  border-color: #409eff;
}

.btn-submit:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin-top: 5px; /* 您可以在这里单独调整“企业微信扫码”标识与下方“企业微信登录”标题之间的距离 */
}

.qrcode-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 20px;
  margin-bottom: 20px;
}

.wechat-icon {
  font-size: 24px;
}

.qrcode-box {
  background: white;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.qrcode-img {
  width: 240px;
  height: 240px;
  display: block;
}

.qrcode-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.qrcode-footer p {
  margin: 5px 0;
}

.slogan-area {
  margin: auto -40px -40px -40px;
  padding: 15px 0;
  text-align: center;
}

.slogan {
  color: #003366;
  font-weight: bold;
  font-size: 16px;
  letter-spacing: 1px;
  margin: 0;
}

.loginDown {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: #848585;
  border-top: 1px solid #eee;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.footer-links {
  margin-bottom: 5px;
  font-size: 12px;
}

.footer-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.footer-info a {
  text-decoration: none;
  display: flex;
  align-items: center;
}

.divider {
  display: inline-block;
  width: 1px;
  height: 12px;
  background-color: #ccc;
  margin: 0 5px;
}

.loginDown img {
  width: 20px;
  height: 20px;
}
</style>