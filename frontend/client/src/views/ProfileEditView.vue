<template>
  <div class="page">
    <van-nav-bar title="个人信息" left-arrow @click-left="router.back()" />
    
    <div class="avatar-section">
      <van-uploader v-model="fileList" :after-read="afterRead" max-count="1" reupload>
        <div class="avatar-wrapper">
          <van-image
            round
            width="80px"
            height="80px"
            :src="userInfo.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
          />
          <div class="edit-hint">点击更换</div>
        </div>
      </van-uploader>
    </div>

    <van-cell-group inset title="基本信息">
      <van-field v-model="userInfo.username" label="用户名" readonly label-class="label-readonly" />
      <van-field v-model="userInfo.nickname" label="昵称" placeholder="设置个性昵称" />
      <van-field v-model="userInfo.email" label="邮箱" placeholder="绑定邮箱地址" />
      
      <van-cell title="手机号" :value="maskPhone(userInfo.phone)" />
    </van-cell-group>

    <van-cell-group inset title="安全设置" class="mt-3">
      <van-field 
        v-model="passwordForm.newPassword" 
        type="password" 
        label="新密码" 
        placeholder="若不修改请留空" 
      />
      <van-field 
        v-model="passwordForm.confirmPassword" 
        type="password" 
        label="确认密码" 
        placeholder="再次输入新密码" 
      />
    </van-cell-group>

    <div class="actions">
      <van-button type="primary" block :loading="saving" @click="onSave">保存修改</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { getProfile, updateProfile, updatePassword } from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const saving = ref(false);
const fileList = ref([]);

const maskPhone = (phone?: string) => {
  if (!phone) return '-';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

const userInfo = ref({
  username: "",
  phone: "",
  email: "",
  nickname: "",
  avatarUrl: ""
});

const passwordForm = ref({
  newPassword: "",
  confirmPassword: ""
});

onMounted(async () => {
  try {
    const res = await getProfile();
    userInfo.value = res;
  } catch (error) {
    showToast(getErrorMessage(error, "获取信息失败"));
  }
});

const afterRead = (file: any) => {
  // In a real app, upload file to server here and get URL
  // For now, we simulate by using base64
  userInfo.value.avatarUrl = file.content;
  showToast("头像已选择，保存后生效");
};

const onSave = async () => {
  saving.value = true;
  try {
    // 1. Update Profile
    await updateProfile({
      nickname: userInfo.value.nickname,
      email: userInfo.value.email,
      avatarUrl: userInfo.value.avatarUrl
    });

    // 2. Update Password if provided
    if (passwordForm.value.newPassword) {
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        showToast("两次密码输入不一致");
        saving.value = false;
        return;
      }
      if (passwordForm.value.newPassword.length < 6) {
        showToast("密码长度不能少于6位");
        saving.value = false;
        return;
      }
      await updatePassword(passwordForm.value.newPassword);
    }

    showToast("保存成功");
    setTimeout(() => router.back(), 1000);
  } catch (error) {
    showToast(getErrorMessage(error, "保存失败"));
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.page {
  background: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 20px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  padding: 30px 0;
  background: #fff;
  margin-bottom: 12px;
}

.avatar-wrapper {
  position: relative;
  text-align: center;
}

.edit-hint {
  font-size: 12px;
  color: #969799;
  margin-top: 8px;
}

.actions {
  padding: 24px 16px;
}

.label-readonly {
  color: #969799;
}

.mt-3 {
  margin-top: 12px;
}
</style>
