<template>
  <div class="detail-wrap">
    <section class="card-panel detail-page">
      <h1>{{ detail.title }}</h1>
      <div class="meta">
        <RouterLink :to="`/profile/${detail.userId}`">{{ detail.authorName }}</RouterLink>
        <span>{{ detail.createTime }}</span>
        <span>👍 {{ detail.likeCount || 0 }}</span>
        <span>💬 {{ detail.commentCount || 0 }}</span>
      </div>
      <article class="content">{{ detail.content }}</article>
      <div class="action-row">
        <el-button type="primary" @click="toggleLike">点赞</el-button>
        <el-button @click="toggleFollow">关注作者</el-button>
      </div>
    </section>

    <section class="card-panel comment-page">
      <div class="comment-head">
        <h2>评论区</h2>
      </div>
      <el-form label-position="top">
        <el-form-item label="发表评论">
          <el-input v-model="commentForm.content" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-button type="primary" @click="publishComment">发表评论</el-button>
      </el-form>
      <div v-if="comments.length" class="comment-list">
        <div v-for="item in comments" :key="item.id" class="comment-item">
          <div class="comment-top">
            <strong>{{ item.authorName }}</strong>
            <span>{{ item.createTime }}</span>
          </div>
          <p>{{ item.content }}</p>
          <div class="comment-actions">
            <el-button link type="primary" @click="prepareReply(item)">回复</el-button>
          </div>
          <div v-if="replyingTo === item.id" class="reply-box">
            <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="回复这条评论" />
            <div class="reply-buttons">
              <el-button type="primary" size="small" @click="submitReply(item)">发送回复</el-button>
              <el-button size="small" @click="cancelReply">取消</el-button>
            </div>
          </div>
          <div v-if="item.children?.length" class="reply-list">
            <div v-for="child in item.children" :key="child.id" class="reply-item">
              <strong>{{ child.authorName }}</strong>
              <span v-if="child.replyUserName"> 回复 {{ child.replyUserName }}</span>
              <p>{{ child.content }}</p>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有评论，抢个沙发吧" />
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useRoute } from 'vue-router'

const route = useRoute()
const detail = ref({})
const comments = ref([])
const commentForm = reactive({ content: '' })
const replyingTo = ref(null)
const replyContent = ref('')

async function loadDetail() {
  const { data } = await request.get(`/post/detail/${route.params.id}`)
  detail.value = data.data || {}
}

async function loadComments() {
  const { data } = await request.get('/comment/list', { params: { targetId: route.params.id, type: 1 } })
  comments.value = data.data || []
}

async function publishComment() {
  if (!commentForm.content.trim()) return
  await request.post('/comment/publish', {
    targetId: Number(route.params.id),
    type: 1,
    content: commentForm.content
  })
  commentForm.content = ''
  ElMessage.success('评论成功')
  await Promise.all([loadComments(), loadDetail()])
}

function prepareReply(item) {
  replyingTo.value = item.id
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

async function submitReply(item) {
  if (!replyContent.value.trim()) return
  await request.post('/comment/publish', {
    targetId: Number(route.params.id),
    type: 1,
    parentId: item.id,
    replyUserId: item.userId,
    content: replyContent.value
  })
  ElMessage.success('回复成功')
  cancelReply()
  await Promise.all([loadComments(), loadDetail()])
}

async function toggleLike() {
  await request.post('/like/toggle', {
    targetType: 'post',
    targetId: Number(route.params.id),
    targetOwnerId: detail.value.userId
  })
  ElMessage.success('操作成功')
  await loadDetail()
}

async function toggleFollow() {
  await request.post('/follow/toggle', { targetUserId: detail.value.userId })
  ElMessage.success('关注状态已更新')
}

onMounted(async () => {
  await Promise.all([loadDetail(), loadComments()])
})
</script>

<style scoped>
.detail-wrap { display: grid; gap: 24px; }
.detail-page, .comment-page { padding: 32px; }
.meta { display: flex; gap: 16px; flex-wrap: wrap; color: var(--muted); margin: 12px 0 24px; }
.content { white-space: pre-wrap; line-height: 1.9; }
.action-row { margin-top: 24px; display: flex; gap: 12px; flex-wrap: wrap; }
.comment-head { margin-bottom: 16px; }
.comment-list { display: grid; gap: 14px; margin-top: 22px; }
.comment-item { padding: 18px; border-radius: 18px; background: rgba(255,248,239,.78); border: 1px solid var(--line); }
.comment-top { display: flex; justify-content: space-between; gap: 12px; }
.comment-item p { margin: 10px 0 0; line-height: 1.8; }
.comment-actions { margin-top: 10px; }
.reply-box { margin-top: 12px; display: grid; gap: 10px; }
.reply-buttons { display: flex; gap: 10px; }
.reply-list { display: grid; gap: 10px; margin-top: 14px; padding-left: 18px; border-left: 2px solid rgba(184,92,56,.15); }
.reply-item p { margin: 6px 0 0; }
</style>
