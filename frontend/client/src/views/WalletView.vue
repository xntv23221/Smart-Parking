<template>
  <div class="page">
    <div class="balance-card">
      <div class="label">当前余额 (元)</div>
      <div class="amount">{{ walletText }}</div>
      <div class="version">版本号: {{ wallet?.version ?? '-' }}</div>
    </div>

    <van-cell-group inset title="财务管理">
      <van-grid :column-num="3" clickable :border="false">
        <van-grid-item icon="orders-o" text="我的订单" to="/orders" />
        <van-grid-item icon="bill-o" text="电子发票" to="/invoices" />
        <van-grid-item icon="warning-o" text="欠费补缴" @click="showToast('暂无欠费')" />
        <van-grid-item icon="vip-card-o" text="月卡管理" to="/monthly-cards" />
        <van-grid-item icon="location-o" text="我的车位" @click="showToast('暂无固定车位')" />
        <van-grid-item icon="gold-coin-o" text="账户储值" @click="showRecharge = true" />
      </van-grid>
    </van-cell-group>

    <van-cell-group inset title="交易记录" style="margin-top: 12px">
      <div v-if="history.length === 0" class="empty-history">暂无记录</div>
      <van-cell 
        v-for="log in history" 
        :key="log.logId" 
        :title="log.remark" 
        :value="formatAmount(log.amount, log.type)" 
        :label="formatTime(log.createdAt)"
        :value-class="getValueClass(log.type)"
      />
    </van-cell-group>

    <!-- Recharge Dialog -->
    <van-dialog v-model:show="showRecharge" title="账户充值" show-cancel-button @confirm="onRecharge">
      <div class="recharge-content">
        <van-field v-model="amount" type="number" label="金额" placeholder="请输入充值金额" />
      </div>
    </van-dialog>

    <!-- Withdraw Dialog -->
    <van-dialog v-model:show="showWithdraw" title="余额提现" show-cancel-button @confirm="onWithdraw">
      <div class="recharge-content">
        <van-field v-model="withdrawAmount" type="number" label="金额" placeholder="请输入提现金额" />
        <van-field v-model="withdrawAccount" label="账户" placeholder="支付宝/微信/银行卡号" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { showToast } from "vant";
import { getMyWallet, recharge, withdraw, getWalletHistory, type UserWallet, type WalletLog } from "../api/client";
import { getErrorMessage } from "../utils/error";

const loading = ref(false);
const showRecharge = ref(false);
const showWithdraw = ref(false);
const wallet = ref<UserWallet | null>(null);
const history = ref<WalletLog[]>([]);
const amount = ref("100.00");
const withdrawAmount = ref("");
const withdrawAccount = ref("");

const walletText = computed(() => {
  if (loading.value) return "...";
  return wallet.value?.balance ?? "0.00";
});

async function load() {
  loading.value = true;
  try {
    wallet.value = await getMyWallet();
    history.value = await getWalletHistory();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "加载失败"));
  } finally {
    loading.value = false;
  }
}

function parseAmount(v: string) {
  const n = Number(v);
  if (!Number.isFinite(n) || n <= 0) {
    throw new Error("请输入正确的金额");
  }
  return n;
}

async function onRecharge() {
  let v = 0;
  try {
    v = parseAmount(amount.value);
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "参数错误"));
    return false; // Prevent closing
  }
  
  try {
    wallet.value = await recharge(v);
    // Refresh history
    history.value = await getWalletHistory();
    showToast("充值成功");
    return true;
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "充值失败"));
    return false;
  }
}

const formatAmount = (amount: number, type: number) => {
  const prefix = type === 1 ? '+' : '';
  return `${prefix}${amount.toFixed(2)}`;
};

const formatTime = (iso: string | any) => {
  if (!iso) return '-';
  if (typeof iso === 'string') {
    return iso.replace('T', ' ').slice(0, 16);
  }
  return new Date(iso).toLocaleString();
};

const getValueClass = (type: number) => {
  return type === 1 ? 'text-green' : 'text-red';
};

onMounted(load);
</script>

<style scoped>
.page {
  padding-bottom: 20px;
}

.balance-card {
  background: linear-gradient(135deg, #1989fa, #0570db);
  color: white;
  padding: 30px 20px;
  margin-bottom: 16px;
}

.label {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.amount {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.version {
  font-size: 12px;
  opacity: 0.6;
}

.recharge-content {
  padding: 20px 10px;
}

.empty-history {
  padding: 20px;
  text-align: center;
  color: #969799;
  font-size: 14px;
}

:deep(.text-green) {
  color: #07c160;
  font-weight: bold;
}

:deep(.text-red) {
  color: #ee0a24;
  font-weight: bold;
}
</style>
