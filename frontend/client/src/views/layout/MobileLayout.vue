<template>
  <div class="layout">
    <div class="content">
      <router-view />
    </div>
    <van-tabbar route>
      <van-tabbar-item to="/home" icon="location-o">停车</van-tabbar-item>
      <van-tabbar-item to="/assistant" icon="service-o">智停管家</van-tabbar-item>
      <van-tabbar-item to="/message" icon="chat-o">消息</van-tabbar-item>
      <van-tabbar-item to="/wallet" icon="gold-coin-o">钱包</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">个人中心</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {
  padding-bottom: 56px;
}
</style>
