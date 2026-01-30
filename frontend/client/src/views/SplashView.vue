<template>
  <div class="splash-container">
    <div class="splash-content">
      <div class="icon-wrapper">
        <img src="/logo.png" alt="Logo" class="logo-img">
      </div>
      <p class="subtitle">智慧停车 <b>·</b> 畅行无忧</p>
      <div class="loading-dots">
        <span />
        <span />
        <span />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();

onMounted(() => {
  // 模拟启动加载过程
  setTimeout(() => {
    if (auth.isAuthed) {
      router.replace("/home");
    } else {
      router.replace("/login");
    }
  }, 2500);
});
</script>

<style scoped>
.splash-container {
  height: 100vh;
  width: 100vw;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
}

.splash-content {
  text-align: center;
  color: #333;
  animation: fadeInUp 0.8s ease-out;
}

.icon-wrapper {
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.logo-img {
  width: 320px;
  height: auto;
  margin-left: 10px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.8;
  margin: 0 0 40px 0;
  letter-spacing: 4px;
  color: #666;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: rgba(25, 137, 250, 0.8);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
  100% { transform: translateY(0px); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
