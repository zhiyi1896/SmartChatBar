<template>
  <div class="hot-page card-panel">
    <div class="head-row">
      <div>
        <h1>社区热榜</h1>
        <p>结合浏览、评论、点赞的热度排序，后端会定时刷新。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div v-if="list.length" class="hot-list">
      <RouterLink v-for="(item, index) in list" :key="item.id" :to="`/post/${item.id}`" class="hot-item">
        <div class="rank-no">{{ index + 1 }}</div>
        <div class="hot-main">
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
          <span>{{ item.authorName }} · 👍 {{ item.likeCount }} · 💬 {{ item.commentCount }}</span>
        </div>
      </RouterLink>
    </div>
    <el-empty v-else description="热榜数据还在准备中" />
  </div>
</template>

<script setup>
import request from '../api/request'

const list = ref([])

async function load() {
  const { data } = await request.get('/ranking/hot')
  list.value = data.data.list || []
}

onMounted(load)
</script>

<style scoped>
.hot-page { padding: 32px; }
.head-row { display: flex; justify-content: space-between; align-items: end; margin-bottom: 24px; }
.hot-list { display: grid; gap: 14px; }
.hot-item { display: grid; grid-template-columns: 64px 1fr; gap: 16px; padding: 18px; border: 1px solid var(--line); border-radius: 20px; background: rgba(255,248,239,.8); }
.rank-no { width: 52px; height: 52px; border-radius: 16px; display:grid; place-items:center; font-size: 22px; font-weight: 700; background: linear-gradient(135deg,var(--gold),var(--brand)); color: white; }
.hot-main h3 { margin: 0 0 8px; }
.hot-main p { margin: 0 0 10px; color: var(--muted); }
.hot-main span { color: var(--muted); font-size: 14px; }
</style>
