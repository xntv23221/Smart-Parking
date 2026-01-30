<template>
  <el-container class="layout">
    <el-aside class="aside" width="220px">
      <div class="brand">{{ brandName }}</div>
      <el-menu :default-active="active" router>
        <!-- <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item> -->
        <el-menu-item index="/messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>消息中心</span>
        </el-menu-item>
        
        <template v-if="auth.isManager">
          <el-menu-item index="/parking-lots">
            <el-icon><Location /></el-icon>
            <span>停车场管理</span>
          </el-menu-item>
          <el-menu-item index="/spaces">
            <el-icon><Grid /></el-icon>
            <span>车位管理</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>订单查询</span>
          </el-menu-item>
        </template>

        <template v-if="auth.isAdmin">
          <el-menu-item index="/parking-lots">
            <el-icon><Location /></el-icon>
            <span>停车场管理</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/settings" @click="showToast">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">{{ brandName }}</div>
        <div class="header-right">
          <el-button size="small" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { Odometer, Location, List, User, Setting, Grid, ChatDotRound } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const active = computed(() => route.path);
const brandName = computed(() => {
  if (auth.isAdmin) return "系统管理端";
  if (auth.isManager) return "停车场管理端";
  return "Smart Parking";
});

function showToast() {
  ElMessage.info("功能开发中");
}

function onLogout() {
  auth.logout();
  router.replace("/login");
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  border-right: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 600;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
}

.header-left {
  font-weight: 600;
}

.main {
  padding: 16px;
}

@media (max-width: 860px) {
  .aside {
    width: 0 !important;
    overflow: hidden;
  }
}
</style>
