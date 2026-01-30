<template>
  <el-card>
    <template #header>
      <div class="header">
        <div>仪表盘</div>
        <el-button size="small" :loading="loading" @click="load">刷新</el-button>
      </div>
    </template>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="后端状态">{{ stats.status ?? "-" }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getDashboardStats } from "../api/admin";
import { getErrorMessage } from "../utils/error";

const loading = ref(false);
const stats = ref<Record<string, unknown>>({});

async function load() {
  loading.value = true;
  try {
    stats.value = await getDashboardStats();
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "加载失败"));
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
