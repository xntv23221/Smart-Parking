<template>
  <el-card>
    <template #header>
      <div class="header">
        <div>订单查询</div>
        <div class="filters">
          <el-input-number v-model="userId" :min="1" controls-position="right" />
          <el-input-number v-model="limit" :min="1" :max="200" controls-position="right" />
          <el-button type="primary" :loading="loading" @click="load">查询</el-button>
        </div>
      </div>
    </template>
    <el-table :data="orders" style="width: 100%" :empty-text="emptyText">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="parkingSpaceId" label="车位ID" width="100" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="entryTime" label="入场时间" min-width="160" />
      <el-table-column prop="exitTime" label="出场时间" min-width="160" />
      <el-table-column prop="paidFee" label="实收(元)" width="110" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import { listOrdersByUser, type ParkingOrder } from "../api/admin";
import { getErrorMessage } from "../utils/error";

const userId = ref<number>(1);
const limit = ref<number>(50);
const loading = ref(false);
const orders = ref<ParkingOrder[]>([]);

const emptyText = computed(() => (loading.value ? "加载中..." : "暂无数据"));

async function load() {
  if (!userId.value || userId.value < 1) {
    ElMessage.error("请输入正确的 userId");
    return;
  }
  loading.value = true;
  try {
    orders.value = await listOrdersByUser(userId.value, limit.value);
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "查询失败"));
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.filters {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
