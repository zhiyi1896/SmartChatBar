<template>
  <div class="card-panel notice-page">
    <div class="page-head">
      <div>
        <h1>消息通知</h1>
        <p>评论、点赞、关注等动态会集中展示在这里。</p>
      </div>
      <div class="head-actions">
        <el-tag type="danger">未读 {{ stats.unread || 0 }}</el-tag>
        <el-button @click="readAll">全部已读</el-button>
      </div>
    </div>

    <div class="stat-row">
      <el-tag>点赞 {{ stats.likeUnread || 0 }}</el-tag>
      <el-tag type="success">评论 {{ stats.commentUnread || 0 }}</el-tag>
      <el-tag type="warning">关注 {{ stats.followUnread || 0 }}</el-tag>
    </div>

    <div v-if="Object.keys(grouped).length" class="group-list">
      <section v-for="(items, key) in grouped" :key="key" class="group-card">
        <h3>{{ labelMap[key] || key }}</h3>
        <div class="notice-list">
          <div v-for="item in items" :key="item.id" class="notice-item">
            <div>
              <strong>{{ item.type }}</strong>
              <p>{{ item.content }}</p>
            </div>
            <span>{{ item.createTime }}</span>
          </div>
        </div>
      </section>
    </div>
    <el-empty v-else description="还没有通知" />
  </div>
</template>

<script setup>
import request from '../api/request'

const grouped = ref({})
const stats = ref({ unread: 0, total: 0 })
const labelMap = {
  LIKE: '点赞通知',
  COMMENT: '评论通知',
  FOLLOW: '关注通知'
}

async function loadNotifications() {
  const [{ data: groupData }, { data: statsData }] = await Promise.all([
    request.get('/notification/grouped'),
    request.get('/notification/stats')
  ])
  grouped.value = groupData.data || {}
  stats.value = statsData.data || {}
}

async function readAll() {
  await request.post('/notification/read-all')
  await loadNotifications()
}

onMounted(loadNotifications)
</script>

<style scoped>
.notice-page { padding: 32px; }
.page-head { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.page-head p { color: var(--muted); }
.head-actions, .stat-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.stat-row { margin: 20px 0; }
.group-list { display: grid; gap: 20px; }
.group-card h3 { margin: 0 0 12px; }
.notice-list { display: grid; gap: 14px; }
.notice-item { display: flex; justify-content: space-between; gap: 20px; padding: 18px; border-radius: 18px; background: rgba(255, 248, 239, 0.78); border: 1px solid var(--line); }
.notice-item p { margin: 8px 0 0; color: var(--muted); }
</style>
