<template>
  <el-row :gutter="16">
    <el-col :xs="24" :md="12">
      <el-card>
        <template #header>创建车位</template>
        <el-form :model="createForm" label-position="top" @submit.prevent>
          <el-form-item label="编号(code)">
            <el-input v-model.trim="createForm.code" />
          </el-form-item>
          <el-form-item label="区域(area)">
            <el-input v-model.trim="createForm.area" />
          </el-form-item>
          <el-form-item label="类型(type)">
            <el-input v-model.trim="createForm.type" />
          </el-form-item>
          <el-form-item label="状态(status)">
            <el-select v-model="createForm.status" style="width: 100%">
              <el-option label="FREE" value="FREE" />
              <el-option label="BOOKED" value="BOOKED" />
              <el-option label="OCCUPIED" value="OCCUPIED" />
              <el-option label="LOCKED" value="LOCKED" />
            </el-select>
          </el-form-item>
          <el-button type="primary" :loading="creating" @click="onCreate">提交</el-button>
        </el-form>
      </el-card>
    </el-col>

    <el-col :xs="24" :md="12">
      <el-card>
        <template #header>批量创建</template>
        <div class="muted">每行一条 JSON，例如：{"code":"P-1","area":"A","type":"NORMAL","status":"FREE"}</div>
        <el-input v-model="batchText" type="textarea" :rows="10" />
        <div class="actions">
          <el-button type="primary" :loading="batching" @click="onBatchCreate">批量提交</el-button>
        </div>
      </el-card>
    </el-col>

    <el-col :xs="24" :md="12" style="margin-top: 16px">
      <el-card>
        <template #header>强制修改状态</template>
        <el-form :model="updateForm" label-position="top" @submit.prevent>
          <el-form-item label="车位ID">
            <el-input-number v-model="updateForm.id" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="目标状态">
            <el-select v-model="updateForm.status" style="width: 100%">
              <el-option label="FREE" value="FREE" />
              <el-option label="BOOKED" value="BOOKED" />
              <el-option label="OCCUPIED" value="OCCUPIED" />
              <el-option label="LOCKED" value="LOCKED" />
            </el-select>
          </el-form-item>
          <el-button type="primary" :loading="updating" @click="onUpdateStatus">提交</el-button>
        </el-form>
      </el-card>
    </el-col>

    <el-col :xs="24" :md="12" style="margin-top: 16px">
      <el-card>
        <template #header>最近操作结果</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="创建结果">{{ lastCreated ? JSON.stringify(lastCreated) : "-" }}</el-descriptions-item>
          <el-descriptions-item label="批量插入条数">{{ lastBatchInserted ?? "-" }}</el-descriptions-item>
          <el-descriptions-item label="状态更新结果">{{ lastUpdated ? JSON.stringify(lastUpdated) : "-" }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { createSpace, createSpacesBatch, forceUpdateSpaceStatus, type ParkingSpace, type ParkingSpaceStatus } from "../api/admin";
import { getErrorMessage } from "../utils/error";

const creating = ref(false);
const batching = ref(false);
const updating = ref(false);

const lastCreated = ref<ParkingSpace | null>(null);
const lastUpdated = ref<ParkingSpace | null>(null);
const lastBatchInserted = ref<number | null>(null);

const createForm = reactive<{ code: string; area: string; type: string; status: ParkingSpaceStatus }>({
  code: "",
  area: "",
  type: "NORMAL",
  status: "FREE"
});

const batchText = ref("");

const updateForm = reactive<{ id: number; status: ParkingSpaceStatus }>({
  id: 1,
  status: "FREE"
});

function requireText(v: string, label: string) {
  if (!v.trim()) {
    throw new Error(`${label}不能为空`);
  }
}

async function onCreate() {
  try {
    requireText(createForm.code, "code");
    requireText(createForm.area, "area");
    requireText(createForm.type, "type");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "参数错误"));
    return;
  }

  creating.value = true;
  try {
    lastCreated.value = await createSpace(createForm);
    ElMessage.success("创建成功");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "创建失败"));
  } finally {
    creating.value = false;
  }
}

async function onBatchCreate() {
  const lines = batchText.value
    .split("\n")
    .map((s) => s.trim())
    .filter(Boolean);
  if (lines.length === 0) {
    ElMessage.error("请输入批量数据");
    return;
  }
  let parsed: Array<{ code: string; area: string; type: string; status?: ParkingSpaceStatus }> = [];
  try {
    parsed = lines.map((l) => JSON.parse(l));
  } catch {
    ElMessage.error("JSON 解析失败");
    return;
  }
  batching.value = true;
  try {
    lastBatchInserted.value = await createSpacesBatch(parsed);
    ElMessage.success("批量创建成功");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "批量创建失败"));
  } finally {
    batching.value = false;
  }
}

async function onUpdateStatus() {
  if (!updateForm.id || updateForm.id < 1) {
    ElMessage.error("请输入正确的车位ID");
    return;
  }
  updating.value = true;
  try {
    lastUpdated.value = await forceUpdateSpaceStatus(updateForm.id, updateForm.status);
    ElMessage.success("更新成功");
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, "更新失败"));
  } finally {
    updating.value = false;
  }
}
</script>

<style scoped>
.actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.muted {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 16px;
}
</style>
