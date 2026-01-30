<template>
  <div class="register-page">
    <div class="header">
      <h1>注册账号</h1>
      <p>欢迎加入智停管家</p>
    </div>

    <van-form @submit="onSubmit" @failed="onFailed">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
        <van-field
          v-model="form.confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[{ required: true, message: '请确认密码' }, { validator: validatePass, message: '两次密码不一致' }]"
        />
        <van-field
          v-model="form.phone"
          type="tel"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请填写手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
        />
      </van-cell-group>

      <div style="margin: 24px 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          立即注册
        </van-button>
        <div class="login-link" @click="router.push('/login')">
          已有账号？去登录
        </div>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { useAuthStore } from '../stores/auth';
import { getErrorMessage } from '../utils/error';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
});

const validatePass = (val: string) => {
  return val === form.password;
};

// 表单校验失败的回调
const onFailed = (errorInfo: any) => {
  showToast('表单校验失败，请检查输入');
};

const onSubmit = async () => {
  loading.value = true;
  try {
    // Determine if phone is empty string, pass undefined or null if needed
    // authStore.register expects (username, password, phone?)
    await authStore.register(
      form.username,
      form.password,
      form.phone
    );
    showToast('注册成功');
    router.replace('/login');
  } catch (e: unknown) {
    showToast(getErrorMessage(e, '注册失败'));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 40px;
}

.header {
  padding: 0 24px 32px;
}

.header h1 {
  font-size: 28px;
  color: #323233;
  margin-bottom: 8px;
}

.header p {
  font-size: 14px;
  color: #969799;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #1989fa;
  font-size: 14px;
  cursor: pointer;
}
</style>
