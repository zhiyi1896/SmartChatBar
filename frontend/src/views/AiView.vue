<template>
  <div class="ai-layout card-panel">
    <div class="chat-head">
      <div>
        <h1>AI 助手</h1>
        <p>支持热帖查询、帖子总结、内容审核、搜索帖子和查看用户主页。</p>
      </div>
      <div class="preset-actions">
        <el-button @click="quickAsk('最近有什么热帖')">热帖</el-button>
        <el-button @click="quickAsk('帮我审核这段发帖内容是否合规')">审核</el-button>
        <el-button @click="quickAsk('帮我搜索一下简历相关帖子')">搜索</el-button>
      </div>
    </div>
    <div class="chat-log">
      <div v-for="(item, index) in logs" :key="index" :class="['chat-item', item.role]">
        <strong>{{ item.role === 'user' ? '我' : 'AI' }}</strong>
        <p>{{ item.content }}</p>
      </div>
    </div>
    <div class="chat-form">
      <el-input v-model="query" type="textarea" :rows="3" placeholder="问点什么，比如：最近有什么热帖？" @keyup.ctrl.enter="submit" />
      <div class="form-actions">
        <span class="tip">Ctrl + Enter 发送</span>
        <el-button type="primary" @click="submit">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '../api/request'

const query = ref('')
const logs = ref([])

function quickAsk(text) {
  query.value = text
  submit()
}

async function submit() {
  if (!query.value.trim()) return
  logs.value.push({ role: 'user', content: query.value })
  const current = query.value
  query.value = ''
  try {
    const { data } = await request.post('/ai/ask/user', null, { params: { query: current } })
    logs.value.push({ role: 'ai', content: data.data })
  } catch {
    ElMessage.error('AI 服务暂时不可用')
  }
}
</script>

<style scoped>
.ai-layout { padding: 32px; display: grid; gap: 20px; }
.chat-head { display: flex; justify-content: space-between; gap: 16px; align-items: start; }
.preset-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.chat-log { display: grid; gap: 14px; min-height: 320px; }
.chat-item { padding: 18px; border-radius: 18px; background: rgba(255,248,239,.78); border: 1px solid var(--line); }
.chat-item.user { background: rgba(184,92,56,.12); }
.chat-item p { margin: 8px 0 0; white-space: pre-wrap; line-height: 1.7; }
.chat-form { display: grid; gap: 12px; }
.form-actions { display: flex; justify-content: space-between; align-items: center; }
.tip { color: var(--muted); font-size: 13px; }
</style>
