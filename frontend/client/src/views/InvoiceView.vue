<template>
  <div class="page">
    <van-nav-bar title="电子发票" left-arrow right-text="开票" @click-left="router.back()" @click-right="showApply = true" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-if="invoices.length === 0 && !loading" class="empty-state">
          <van-empty description="暂无开票记录" />
        </div>
        
        <van-cell
          v-for="item in invoices"
          :key="item.invoiceId"
          :title="item.title"
          :value="item.status === 1 ? '已开票' : '处理中'"
          :label="`金额: ¥${item.amount} | 时间: ${formatTime(item.createdAt)}`"
        />
      </van-list>
    </van-pull-refresh>

    <!-- Apply Dialog -->
    <van-dialog v-model:show="showApply" title="申请开票" show-cancel-button @confirm="onApply">
      <van-form ref="formRef">
        <van-cell-group inset>
          <van-field v-model="form.amount" type="number" label="金额" placeholder="开票金额" :rules="[{ required: true }]" />
          <van-field v-model="form.title" label="抬头" placeholder="发票抬头" :rules="[{ required: true }]" />
          <van-field v-model="form.taxNo" label="税号" placeholder="纳税人识别号" :rules="[{ required: true }]" />
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { getMyInvoices, applyInvoice, type Invoice } from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const invoices = ref<Invoice[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const showApply = ref(false);

const form = reactive({
  amount: "",
  title: "",
  taxNo: ""
});

const onLoad = async () => {
  if (refreshing.value) {
    invoices.value = [];
    refreshing.value = false;
  }

  try {
    const res = await getMyInvoices();
    invoices.value = res;
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

const onApply = async () => {
  if (!form.amount || !form.title || !form.taxNo) {
    showToast("请填写完整信息");
    return false;
  }
  
  try {
    await applyInvoice({
      amount: Number(form.amount),
      title: form.title,
      taxNo: form.taxNo
    });
    showToast("申请提交成功");
    onRefresh();
    return true;
  } catch (error) {
    showToast(getErrorMessage(error, "申请失败"));
    return false;
  }
};

const formatTime = (iso: string | any) => {
  if (!iso) return '-';
  if (typeof iso === 'string') {
    return iso.replace('T', ' ').slice(0, 16);
  }
  return new Date(iso).toLocaleString();
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
