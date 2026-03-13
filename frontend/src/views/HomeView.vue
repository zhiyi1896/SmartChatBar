<template>
  <div class="home-grid">
    <section class="card-panel hero-card">
      <span class="hero-tag">论坛 · 技术 · 成长</span>
      <h1>把项目、面经和思考，沉淀成值得被讨论的内容。</h1>
      <p>现在首页已经接了帖子流、热榜入口和 AI 助手入口，后续继续细化互动与管理体验。</p>
      <div class="hero-actions">
        <RouterLink to="/editor"><el-button type="primary" size="large" round>开始发帖</el-button></RouterLink>
        <RouterLink to="/hot"><el-button size="large" round>查看热榜</el-button></RouterLink>
        <RouterLink to="/ai"><el-button size="large" round>打开AI助手</el-button></RouterLink>
      </div>
    </section>

    <aside class="card-panel side-card">
      <div class="side-title">社区节奏</div>
      <div class="stat-item"><strong>{{ hotCount }}</strong><span>热榜条目</span></div>
      <div class="stat-item"><strong>{{ noticeUnread }}</strong><span>未读通知</span></div>
      <div class="stat-item"><strong>LangChain</strong><span>AI 助手</span></div>
    </aside>

    <section class="card-panel post-list">
      <div class="list-header">
        <h2>最新帖子</h2>
        <el-input v-model="keyword" placeholder="搜索标题或正文" clearable style="max-width: 300px" @keyup.enter="loadPosts" />
      </div>
      <div v-if="posts.length" class="post-items">
        <RouterLink v-for="post in posts" :key="post.id" :to="`/post/${post.id}`" class="post-item">
          <div class="post-main">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
          </div>
          <div class="post-meta">
            <span>{{ post.authorName }}</span>
            <span>{{ post.createTime }}</span>
          </div>
        </RouterLink>
      </div>
      <el-empty v-else description="还没有帖子，先发布第一篇吧" />
    </section>
  </div>
</template>

<script setup>
import request from '../api/request'

const posts = ref([])
const keyword = ref('')
const hotCount = ref(0)
const noticeUnread = ref(0)

async function loadPosts() {
  const { data } = await request.get('/post/list', { params: { page: 1, pageSize: 10, keyword: keyword.value } })
  posts.value = data.data.list || []
}

async function loadHot() {
  try {
    const { data } = await request.get('/ranking/hot')
    hotCount.value = data.data.list?.length || 0
  } catch {
    hotCount.value = 0
  }
}

async function loadNoticeStats() {
  try {
    const { data } = await request.get('/notification/stats')
    noticeUnread.value = data.data.unread || 0
  } catch {
    noticeUnread.value = 0
  }
}

async function recordUv() {
  try {
    await request.get('/stats/uv/record')
  } catch {
    // ignore
  }
}

onMounted(() => {
  recordUv()
  loadPosts()
  loadHot()
  loadNoticeStats()
})
</script>

<style scoped>
.home-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}
.hero-card { padding: 32px; }
.hero-card h1 { margin: 16px 0; font-size: 44px; line-height: 1.15; }
.hero-tag { display: inline-block; padding: 8px 14px; border-radius: 999px; background: rgba(184, 92, 56, 0.12); color: var(--brand-deep); }
.hero-actions { display: flex; gap: 12px; margin-top: 24px; flex-wrap: wrap; }
.side-card, .post-list { padding: 24px; }
.side-title { font-size: 18px; font-weight: 700; margin-bottom: 20px; }
.stat-item { display: flex; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid var(--line); }
.post-list { grid-column: 1 / -1; }
.list-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
.post-items { display: grid; gap: 16px; }
.post-item { display: grid; gap: 12px; padding: 20px; border-radius: 18px; background: rgba(255, 248, 239, 0.72); border: 1px solid rgba(100, 68, 38, 0.08); transition: transform 0.2s ease; }
.post-item:hover { transform: translateY(-2px); }
.post-main h3 { margin: 0 0 8px; }
.post-main p { margin: 0; color: var(--muted); line-height: 1.7; }
.post-meta { display: flex; justify-content: space-between; color: var(--muted); font-size: 14px; }
@media (max-width: 900px) { .home-grid { grid-template-columns: 1fr; } .hero-card h1 { font-size: 34px; } }
</style>
