<template>
  <div class="settings-grid">
    <section class="card-panel settings-card">
      <h2>个人资料</h2>
      <el-form :model="profileForm" label-position="top">
        <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
        <el-form-item label="头像链接"><el-input v-model="profileForm.avatar" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="profileForm.bio" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="上传头像文件">
          <input type="file" @change="uploadAvatar" />
        </el-form-item>
        <el-button type="primary" @click="saveProfile">保存资料</el-button>
      </el-form>
    </section>
    <section class="card-panel settings-card">
      <h2>修改密码</h2>
      <el-form :model="passwordForm" label-position="top">
        <el-form-item label="旧密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
        <el-button type="primary" @click="savePassword">更新密码</el-button>
      </el-form>
    </section>
    <section class="card-panel settings-card">
      <h2>修改邮箱</h2>
      <el-form :model="emailForm" label-position="top">
        <el-form-item label="新邮箱"><el-input v-model="emailForm.email" /></el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="emailForm.code" />
            <el-button @click="sendEmailCode">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-button type="primary" @click="saveEmail">更新邮箱</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const profileForm = reactive({ nickname: userStore.profile?.nickname || '', avatar: '', bio: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const emailForm = reactive({ email: '', code: '' })

async function uploadAvatar(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await request.post('/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  profileForm.avatar = data.data
  ElMessage.success('头像上传成功')
}

async function saveProfile() {
  await request.put('/profile/me', profileForm)
  ElMessage.success('资料已更新')
}

async function savePassword() {
  await request.put('/profile/password', passwordForm)
  ElMessage.success('密码已更新，请重新登录')
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
}

async function sendEmailCode() {
  await request.post('/profile/email/code', { email: emailForm.email })
  ElMessage.success('验证码已发送')
}

async function saveEmail() {
  await request.put('/profile/email', emailForm)
  ElMessage.success('邮箱已更新')
  emailForm.code = ''
}
</script>

<style scoped>
.settings-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.settings-card { padding: 28px; }
.code-row { display: grid; grid-template-columns: 1fr auto; gap: 12px; }
@media (max-width: 900px) { .settings-grid { grid-template-columns: 1fr; } }
</style>
