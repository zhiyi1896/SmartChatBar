<template>
  <div class="search-page card-panel">
    <div class="search-head">
      <div>
        <h1>搜索帖子</h1>
        <p>先用数据库查询还原搜索能力，后续再接 ES 高亮和同步。</p>
      </div>
      <div class="search-form">
        <el-input v-model="keyword" placeholder="输入关键词搜索帖子" @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
      </div>
    </div>
    <div v-if="list.length" class="result-list">
      <RouterLink v-for="item in list" :key="item.id" :to="`/post/${item.id}`" class="result-item">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
        <span>{{ item.authorName }} · {{ item.createTime }}</span>
      </RouterLink>
    </div>
    <el-empty v-else description="输入关键词开始搜索" />
  </div>
</template>

<script setup>
import request from '../api/request'

const keyword = ref('')
const list = ref([])

async function load() {
  if (!keyword.value) {
    list.value = []
    return
  }
  const { data } = await request.get('/search', { params: { keyword: keyword.value } })
  list.value = data.data.list || []
}
</script>

<style scoped>
.search-page {
  padding: 32px;
}

.search-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  margin-bottom: 24px;
}

.search-form {
  display: flex;
  gap: 12px;
  min-width: min(480px, 100%);
}

.result-list {
  display: grid;
  gap: 14px;
}

.result-item {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 248, 239, 0.78);
  border: 1px solid var(--line);
}

.result-item h3 {
  margin: 0 0 8px;
}

.result-item p {
  margin: 0 0 10px;
  color: var(--muted);
}

.result-item span {
  color: var(--muted);
  font-size: 14px;
}

@media (max-width: 900px) {
  .search-head {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form {
    min-width: auto;
  }
}
</style>
