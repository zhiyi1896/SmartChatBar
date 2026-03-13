<template>
  <div class="auth-shell card-panel">
    <div class="auth-side">
      <h1>欢迎回来</h1>
      <p>登录后可以发帖、评论、关注、私信，也能继续使用 AI 助手的上下文记忆。</p>
    </div>
    <div class="auth-form">
      <h2>邮箱登录</h2>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="full-btn" @click="submit">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ email: '', password: '' })

async function submit() {
  await userStore.login(form)
  ElMessage.success('登录成功')
  router.push('/')
}
</script>

<style scoped>
.auth-shell {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  overflow: hidden;
}

.auth-side {
  padding: 48px;
  background: linear-gradient(145deg, rgba(184, 92, 56, 0.92), rgba(140, 59, 29, 0.9));
  color: white;
}

.auth-form {
  padding: 48px;
}

.full-btn {
  width: 100%;
  margin-top: 12px;
}

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }
}
</style>
