<template>
  <div class="page">
    <div class="user-card">
      <div class="avatar">
        <van-image
          round
          width="64px"
          height="64px"
          :src="userInfo?.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
        />
      </div>
      <div class="info">
        <div class="name">{{ userInfo?.nickname || userInfo?.username || '用户' }}</div>
        <div class="phone">{{ maskPhone(userInfo?.phone) }}</div>
      </div>
      <van-icon name="arrow" class="arrow" @click="router.push('/profile/edit')" />
    </div>

    <van-cell-group inset class="mt-3">
      <van-cell title="我的订单" is-link icon="orders-o" to="/orders" />
      <van-cell title="我的爱车" is-link icon="logistics" to="/vehicles" />
      <van-cell title="我的发票" is-link icon="bill-o" to="/invoices" />
      <van-cell title="我的月卡" is-link icon="vip-card-o" to="/monthly-cards" />
    </van-cell-group>

    <van-cell-group inset class="mt-3">
      <van-cell title="账户设置" is-link icon="setting-o" @click="showToast('功能开发中')" />
      <van-cell title="关于我们" is-link icon="info-o" @click="showToast('智停管家 v1.0')" />
    </van-cell-group>
    
    <div class="logout-btn">
      <van-button block type="default" @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { useAuthStore } from "../stores/auth";
import { getProfile } from "../api/client";

const router = useRouter();
const auth = useAuthStore();
const userInfo = ref<any>(null);

const maskPhone = (phone?: string) => {
  if (!phone) return '-';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

onMounted(async () => {
  try {
    const res = await getProfile();
    userInfo.value = res;
  } catch {
    // ignore
  }
});

async function onLogout() {
  auth.logout();
  await router.replace("/login");
}
</script>

<style scoped>
.page {
  padding-bottom: 20px;
}

.user-card {
  background: #fff;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  margin-right: 16px;
}

.info {
  flex: 1;
}

.name {
  font-size: 20px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.phone {
  font-size: 14px;
  color: #969799;
}

.arrow {
  color: #969799;
  font-size: 20px;
}

.mt-3 {
  margin-top: 12px;
}

.logout-btn {
  margin: 24px 16px;
}
</style>
