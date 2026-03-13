<template>
  <div class="card-panel editor-page">
    <h1>发布帖子</h1>
    <el-form :model="form" label-position="top">
      <el-form-item label="标题"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
      <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="14" maxlength="10000" show-word-limit /></el-form-item>
      <el-button type="primary" size="large" @click="submit">立即发布</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()
const form = reactive({ title: '', content: '' })

async function submit() {
  const { data } = await request.post('/post/publish', form)
  ElMessage.success('发布成功')
  router.push(`/post/${data.data.id}`)
}
</script>

<style scoped>
.editor-page {
  padding: 32px;
}
</style>
