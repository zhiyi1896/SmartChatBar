<template>
  <div class="message-layout">
    <section class="card-panel conv-panel">
      <div class="panel-title">最近对话</div>
      <div v-if="conversations.length" class="conv-list">
        <button v-for="item in conversations" :key="item.conversationId" class="conv-item" @click="selectConversation(item)">
          <div>
            <strong>{{ item.userName }}</strong>
            <p>{{ item.lastMessage }}</p>
          </div>
          <div class="conv-right">
            <span>{{ item.lastMessageTime }}</span>
            <el-badge v-if="item.unreadCount" :value="item.unreadCount" />
          </div>
        </button>
      </div>
      <el-empty v-else description="还没有私信记录" />
    </section>
    <section class="card-panel msg-panel">
      <div class="panel-head">
        <div class="panel-title">聊天记录</div>
        <el-button v-if="current" @click="markRead">标记已读</el-button>
      </div>
      <div v-if="messages.length" class="msg-list">
        <div v-for="item in messages" :key="item.id" :class="['msg-item', { self: item.self }]">
          <div class="bubble">{{ item.content }}</div>
          <span>{{ item.time }}</span>
        </div>
      </div>
      <el-empty v-else description="选择一个对话开始查看消息" />
      <div v-if="current" class="send-box">
        <el-input v-model="sendForm.content" type="textarea" :rows="3" placeholder="输入消息内容" />
        <el-button type="primary" @click="sendMessage">发送消息</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '../api/request'

const conversations = ref([])
const messages = ref([])
const current = ref(null)
const sendForm = reactive({ content: '' })
let ws = null

function connectWs() {
  const token = localStorage.getItem('token')
  if (!token) return
  ws = new WebSocket(`ws://localhost:5173/ws?token=${token}`)
  ws.onmessage = (event) => {
    const payload = JSON.parse(event.data)
    if (payload.type === 'NEW_MESSAGE') {
      if (current.value && payload.data.fromUserId === current.value.userId) {
        messages.value.push({
          id: payload.data.id,
          fromUserId: payload.data.fromUserId,
          toUserId: payload.data.toUserId,
          content: payload.data.content,
          time: payload.data.createTime,
          self: false,
          read: false
        })
      }
      loadConversations()
    }
  }
}

async function loadConversations() {
  const { data } = await request.get('/message/conversations')
  conversations.value = data.data.list || []
}

async function selectConversation(item) {
  current.value = item
  const { data } = await request.get(`/message/recent/${item.userId}`)
  messages.value = (data.data || []).slice().reverse()
}

async function markRead() {
  if (!current.value) return
  await request.post(`/message/read/${current.value.conversationId}`)
  await loadConversations()
}

async function sendMessage() {
  if (!current.value || !sendForm.content.trim()) return
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'SEND',
      data: {
        toUserId: current.value.userId,
        content: sendForm.content
      }
    }))
  } else {
    await request.post('/message/send', {
      toUserId: current.value.userId,
      content: sendForm.content
    })
  }
  messages.value.push({
    id: Date.now(),
    fromUserId: 0,
    toUserId: current.value.userId,
    content: sendForm.content,
    time: '刚刚',
    self: true,
    read: false
  })
  sendForm.content = ''
  ElMessage.success('发送成功')
  await loadConversations()
}

onMounted(() => {
  connectWs()
  loadConversations()
})

onBeforeUnmount(() => {
  if (ws) ws.close()
})
</script>

<style scoped>
.message-layout { display: grid; grid-template-columns: 360px 1fr; gap: 24px; }
.conv-panel, .msg-panel { padding: 24px; min-height: 520px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.panel-title { font-size: 20px; font-weight: 700; }
.conv-list, .msg-list { display: grid; gap: 12px; }
.conv-item { display: flex; justify-content: space-between; gap: 16px; text-align: left; border: 1px solid var(--line); border-radius: 16px; padding: 16px; background: rgba(255, 248, 239, 0.72); }
.conv-right { display: grid; justify-items: end; gap: 8px; }
.conv-item p { margin: 6px 0 0; color: var(--muted); }
.msg-item { display: grid; justify-items: start; }
.msg-item.self { justify-items: end; }
.bubble { max-width: 70%; padding: 14px 18px; border-radius: 18px; background: rgba(255, 248, 239, 0.92); }
.msg-item.self .bubble { background: rgba(184, 92, 56, 0.92); color: white; }
.send-box { display: grid; gap: 12px; margin-top: 18px; }
@media (max-width: 900px) { .message-layout { grid-template-columns: 1fr; } }
</style>
