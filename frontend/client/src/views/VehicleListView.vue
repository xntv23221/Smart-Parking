<template>
  <div class="page">
    <van-nav-bar title="我的爱车" left-arrow right-text="添加" @click-left="router.back()" @click-right="router.push('/vehicles/add')" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-if="vehicles.length === 0 && !loading" class="empty-state">
          <van-empty description="暂无车辆信息" />
        </div>
        
        <van-swipe-cell v-for="item in vehicles" :key="item.vehicleId">
          <div class="vehicle-card">
            <div class="card-left">
              <div class="plate">{{ item.plateNumber }}</div>
              <div class="desc">{{ item.brand }} {{ item.model }} {{ item.color }}</div>
            </div>
            <div class="card-right">
              <van-tag v-if="item.isDefault" type="primary">默认</van-tag>
              <van-button v-else size="mini" @click="setAsDefault(item)">设为默认</van-button>
            </div>
          </div>
          <template #right>
            <van-button square text="删除" type="danger" class="delete-button" @click="onDelete(item)" />
          </template>
        </van-swipe-cell>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { showToast, showDialog } from "vant";
import { getMyVehicles, deleteVehicle, setDefaultVehicle, type Vehicle } from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const vehicles = ref<Vehicle[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);

const onLoad = async () => {
  if (refreshing.value) {
    vehicles.value = [];
    refreshing.value = false;
  }

  try {
    const res = await getMyVehicles();
    vehicles.value = res;
    finished.value = true;
  } catch (error) {
    showToast(getErrorMessage(error, "加载失败"));
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

const onRefresh = () => {
  finished.value = false;
  loading.value = true;
  onLoad();
};

const onDelete = (item: Vehicle) => {
  showDialog({
    title: '确认删除',
    message: `确定要删除车辆 ${item.plateNumber} 吗？`,
    showCancelButton: true,
  }).then(async () => {
    try {
      await deleteVehicle(item.vehicleId);
      showToast('删除成功');
      onRefresh();
    } catch (error) {
      showToast(getErrorMessage(error, "删除失败"));
    }
  });
};

const setAsDefault = async (item: Vehicle) => {
  try {
    await setDefaultVehicle(item.vehicleId);
    showToast('设置成功');
    onRefresh();
  } catch (error) {
    showToast(getErrorMessage(error, "设置失败"));
  }
};
</script>

<style scoped>
.page {
  background: #f7f8fa;
  min-height: 100vh;
}

.vehicle-card {
  background: #fff;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f5f6f7;
}

.plate {
  font-size: 18px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.desc {
  font-size: 14px;
  color: #969799;
}

.delete-button {
  height: 100%;
}

.empty-state {
  padding-top: 40px;
}
</style>
