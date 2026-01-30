<template>
  <div class="page">
    <van-cell-group inset>
      <van-cell title="我的订单" :value="loading ? '加载中...' : `共${orders.length}条`" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-field v-model.number="orderId" type="number" label="订单ID" placeholder="输入订单ID操作" />
      <van-field v-model="timeText" label="时间" placeholder="ISO时间(可留空自动填)" />
      <div class="actions">
        <van-button block :loading="acting" @click="onEntry">入场</van-button>
        <van-button block :loading="acting" @click="onExit">出场</van-button>
        <van-button type="primary" block :loading="acting" @click="onPay">支付</van-button>
      </div>
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="订单列表" />
      <van-cell
        v-for="o in orders"
        :key="o.recordId"
        :title="`订单#${o.recordId}`"
        :label="`车位:${o.spotId || '未分配'} 入:${formatTime(o.entryTime)}`"
      >
        <template #value>
          <div class="order-value">
            <span class="status-tag">{{ ParkingOrderStatusText[o.status] || o.status }}</span>
            <van-button 
              size="mini" 
              plain 
              type="danger" 
              @click.stop="onCancel(o)" 
              v-if="o.status === 0"
              style="margin-right: 5px"
            >取消预约</van-button>
            <van-button 
              size="mini" 
              plain 
              type="primary" 
              @click.stop="contactManager(o)" 
              v-if="hasManager(o)"
            >联系管理</van-button>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <div class="footer">
      <van-button type="primary" block :loading="loading" @click="load">刷新列表</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { showToast, showConfirmDialog } from "vant";
import { entry, exit, listOrders, pay, cancel, getParkingLots, type ParkingOrder, type ParkingLot, ParkingOrderStatusText } from "../api/client";
import { getErrorMessage } from "../utils/error";
import { formatTime } from "../utils/format";

const router = useRouter();
const loading = ref(false);
const acting = ref(false);
const orders = ref<ParkingOrder[]>([]);
const parkingLots = ref<ParkingLot[]>([]);

const orderId = ref<number>(1);
const timeText = ref("");

const nowIso = computed(() => new Date().toISOString().slice(0, 19));

onMounted(async () => {
  try {
    parkingLots.value = await getParkingLots();
  } catch {
    // ignore
  }
  load();
});

/**
 * 检查订单关联的停车场是否有管理员
 * @param o 订单对象
 */
function hasManager(o: ParkingOrder) {
  if (!o.lotId) return false;
  const lot = parkingLots.value.find(l => l.lotId === o.lotId);
  return lot && lot.managerId;
}

/**
 * 跳转到与管理员的聊天页面
 * @param o 订单对象
 */
function contactManager(o: ParkingOrder) {
  if (!o.lotId) return;
  const lot = parkingLots.value.find(l => l.lotId === o.lotId);
  if (lot && lot.managerId) {
    router.push({
      path: '/message',
      query: { targetId: lot.managerId, targetRole: 'manager' }
    });
  } else {
    showToast("该停车场暂无管理员");
  }
}

const onCancel = async (o: ParkingOrder) => {
  try {
    await showConfirmDialog({
      title: '取消预约',
      message: '确定要取消当前预约吗？',
    });
    
    acting.value = true;
    const recordId = o.recordId;
    await cancel(recordId);
    showToast("预约已取消");
    load();
  } catch (e: any) {
    if (e !== 'cancel') {
      showToast(e.message || "取消失败");
    }
  } finally {
    acting.value = false;
  }
};

/**
 * 加载订单列表
 */
async function load() {
  loading.value = true;
  try {
    orders.value = await listOrders(50);
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "加载失败"));
  } finally {
    loading.value = false;
  }
}

function ensureOrderId() {
  if (!orderId.value || orderId.value < 1) {
    throw new Error("请输入正确的订单ID");
  }
}

async function onEntry() {
  try {
    ensureOrderId();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "参数错误"));
    return;
  }
  acting.value = true;
  try {
    await entry(orderId.value, timeText.value || nowIso.value);
    showToast("入场成功");
    await load();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "入场失败"));
  } finally {
    acting.value = false;
  }
}

async function onExit() {
  try {
    ensureOrderId();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "参数错误"));
    return;
  }
  acting.value = true;
  try {
    await exit(orderId.value, timeText.value || nowIso.value);
    showToast("出场成功");
    await load();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "出场失败"));
  } finally {
    acting.value = false;
  }
}

async function onPay() {
  try {
    ensureOrderId();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "参数错误"));
    return;
  }
  acting.value = true;
  try {
    await pay(orderId.value);
    showToast("支付成功");
    await load();
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "支付失败"));
  } finally {
    acting.value = false;
  }
}
</script>

<style scoped>
.page {
  padding-bottom: 20px;
  background: #f7f8fa;
  min-height: 100vh;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding: 0 16px;
}
.footer {
  margin: 16px;
}
.order-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.status-tag {
  color: #969799;
}
</style>
