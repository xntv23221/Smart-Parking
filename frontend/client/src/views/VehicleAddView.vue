<template>
  <div class="page">
    <van-nav-bar title="添加车辆" left-arrow @click-left="router.back()" />
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.plateNumber"
          name="plateNumber"
          label="车牌号"
          placeholder="请输入车牌号"
          :rules="[{ required: true, message: '请填写车牌号' }]"
        />
        <van-field
          v-model="form.brand"
          name="brand"
          label="品牌"
          placeholder="例如：大众"
        />
        <van-field
          v-model="form.model"
          name="model"
          label="型号"
          placeholder="例如：帕萨特"
        />
        <van-field
          v-model="form.color"
          name="color"
          label="颜色"
          placeholder="例如：黑色"
        />
        <van-field name="vehicleType" label="车辆类型">
          <template #input>
            <van-radio-group v-model="form.vehicleType" direction="horizontal">
              <van-radio :name="1">小型车</van-radio>
              <van-radio :name="2">大型车</van-radio>
              <van-radio :name="3">新能源</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field name="isDefault" label="设为默认">
          <template #input>
            <van-switch v-model="form.isDefault" size="20" />
          </template>
        </van-field>
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { addVehicle } from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const submitting = ref(false);

const form = reactive({
  plateNumber: "",
  brand: "",
  model: "",
  color: "",
  vehicleType: 1,
  isDefault: false
});

const onSubmit = async () => {
  submitting.value = true;
  try {
    await addVehicle(form);
    showToast("添加成功");
    router.back();
  } catch (error) {
    showToast(getErrorMessage(error, "添加失败"));
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.page {
  background: #f7f8fa;
  min-height: 100vh;
  padding-top: 12px;
}
</style>
