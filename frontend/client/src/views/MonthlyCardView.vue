<template>
  <div class="page">
    <van-nav-bar title="月卡管理" left-arrow right-text="购买" @click-left="router.back()" @click-right="showBuy = true" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-if="cards.length === 0 && !loading" class="empty-state">
          <van-empty description="暂无月卡" />
        </div>
        
        <van-cell
          v-for="item in cards"
          :key="item.cardId"
          :title="item.plateNumber"
          :value="item.status === 1 ? '生效中' : '已过期'"
          :label="`有效期: ${item.startDate} 至 ${item.endDate}`"
        />
      </van-list>
    </van-pull-refresh>

    <!-- Buy Dialog -->
    <van-dialog v-model:show="showBuy" title="购买月卡" show-cancel-button @confirm="onBuy">
      <van-form>
        <van-cell-group inset>
          <van-field v-model="form.plateNumber" label="车牌号" placeholder="请输入车牌号" />
          <van-field v-model="form.lotId" type="number" label="停车场ID" placeholder="请输入停车场ID" />
          <van-field name="months" label="购买时长">
            <template #input>
              <van-radio-group v-model="form.months" direction="horizontal">
                <van-radio :name="1">1个月</van-radio>
                <van-radio :name="3">3个月</van-radio>
                <van-radio :name="12">1年</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { getMyMonthlyCards, buyMonthlyCard, type MonthlyCard } from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const cards = ref<MonthlyCard[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const showBuy = ref(false);

const form = reactive({
  plateNumber: "",
  lotId: "1",
  months: 1
});

const onLoad = async () => {
  if (refreshing.value) {
    cards.value = [];
    refreshing.value = false;
  }

  try {
    const res = await getMyMonthlyCards();
    cards.value = res;
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

const onBuy = async () => {
  if (!form.plateNumber || !form.lotId) {
    showToast("请填写完整信息");
    return false;
  }
  
  try {
    await buyMonthlyCard({
      plateNumber: form.plateNumber,
      lotId: Number(form.lotId),
      months: form.months
    });
    showToast("购买成功");
    onRefresh();
    return true;
  } catch (error) {
    showToast(getErrorMessage(error, "购买失败"));
    return false;
  }
};
</script>

<style scoped>
.page {
  background: #f7f8fa;
  min-height: 100vh;
}
.empty-state {
  padding-top: 40px;
}
</style>
