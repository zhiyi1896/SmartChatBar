<template>
  <div class="auth-shell card-panel">
    <div class="auth-side register-side">
      <h1>加入牛客社区</h1>
      <p>用邮箱验证码完成注册，注册成功后直接拿到登录态，和文档中的方案保持一致。</p>
    </div>
    <div class="auth-form">
      <h2>邮箱注册</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="form.code" />
            <el-button @click="sendCode">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button type="primary" class="full-btn" @click="submit">注册并登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ nickname: '', email: '', code: '', password: '' })

async function sendCode() {
  await request.post('/auth/send-code', { email: form.email })
  ElMessage.success('验证码已发送')
}

async function submit() {
  const { data } = await request.post('/auth/register', form)
  userStore.setAuth({
    token: data.data.token,
    profile: {
      userId: data.data.userId,
      nickname: data.data.nickname,
      avatar: data.data.avatar
    }
  })
  ElMessage.success('注册成功')
  router.push('/')
}
</script>

<style scoped>
.auth-shell {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  overflow: hidden;
}

.register-side {
  background: linear-gradient(145deg, rgba(216, 162, 94, 0.94), rgba(184, 92, 56, 0.92));
}

.auth-side {
  padding: 48px;
  color: white;
}

.auth-form {
  padding: 48px;
}

.code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.full-btn {
  width: 100%;
}

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }
}
</style>
