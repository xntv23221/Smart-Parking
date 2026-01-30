<template>
  <div class="parking-lots-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>停车场管理</span>
          <el-button v-if="auth.isAdmin" type="primary" size="small" @click="showCreateDialog">添加停车场</el-button>
        </div>
      </template>

      <el-table :data="parkingLots" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="address" label="地址" min-width="150" />
        <el-table-column label="总车位" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.totalSpots }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可用车位" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.availableSpots }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editLot(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑停车场' : '添加停车场'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <template v-if="auth.isAdmin">
          <el-form-item label="名称">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="form.address" />
          </el-form-item>
          <el-form-item label="经度">
            <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" />
          </el-form-item>
          <el-form-item label="纬度">
            <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="开放" value="OPEN" />
              <el-option label="关闭" value="CLOSED" />
              <el-option label="维护中" value="MAINTENANCE" />
            </el-select>
          </el-form-item>
          <el-form-item label="管理员ID">
            <el-input-number v-model="form.managerId" style="width: 100%" placeholder="请输入管理员用户ID" />
          </el-form-item>
        </template>
        
        <el-form-item label="状态" v-if="!auth.isAdmin">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="开放" value="OPEN" />
            <el-option label="关闭" value="CLOSED" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>

        <el-form-item label="总车位">
          <el-input-number v-model="form.totalSpots" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="可用车位">
          <el-input-number v-model="form.availableSpots" :min="0" :max="form.totalSpots" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { listParkingLots, updateParkingLot, type ParkingLot } from "../api/admin";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const loading = ref(false);
const submitting = ref(false);
const parkingLots = ref<ParkingLot[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);

const form = reactive<Partial<ParkingLot>>({
  id: undefined,
  name: "",
  address: "",
  totalSpots: 0,
  availableSpots: 0,
  latitude: 0,
  longitude: 0,
  status: "OPEN",
  managerId: undefined
});

function getStatusType(status: string) {
  switch (status) {
    case "OPEN": return "success";
    case "CLOSED": return "info";
    case "MAINTENANCE": return "warning";
    default: return "";
  }
}

async function fetchParkingLots() {
  loading.value = true;
  try {
    parkingLots.value = await listParkingLots();
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false;
  }
}

function showCreateDialog() {
  if (!auth.isAdmin) return;
  isEdit.value = false;
  Object.assign(form, {
    id: undefined,
    name: "",
    address: "",
    totalSpots: 100,
    availableSpots: 100,
    latitude: 0,
    longitude: 0,
    status: "OPEN",
    managerId: undefined
  });
  dialogVisible.value = true;
}

function editLot(row: ParkingLot) {
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
}

async function onSubmit() {
  if (!form.id && isEdit.value) return;
  
  submitting.value = true;
  try {
    if (isEdit.value && form.id) {
      await updateParkingLot(form.id, form);
      ElMessage.success("更新成功");
    } else {
      await createParkingLot(form);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    fetchParkingLots();
  } catch (e) {
    // handled
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchParkingLots();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
