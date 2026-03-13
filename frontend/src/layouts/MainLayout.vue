<template>
  <div class="page-shell">
    <header class="hero-bar">
      <div class="container nav-row">
        <RouterLink class="brand" to="/">牛客社区</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/hot">热榜</RouterLink>
          <RouterLink to="/search">搜索</RouterLink>
          <RouterLink to="/ai">AI助手</RouterLink>
          <RouterLink to="/editor">发布帖子</RouterLink>
          <RouterLink to="/messages">私信</RouterLink>
          <RouterLink to="/notifications">通知</RouterLink>
        </nav>
        <div class="nav-actions">
          <template v-if="userStore.profile">
            <RouterLink :to="`/profile/${userStore.profile.userId}`" class="welcome">{{ userStore.profile.nickname }}</RouterLink>
            <RouterLink to="/settings"><el-button round>设置</el-button></RouterLink>
            <el-button round @click="logout">退出</el-button>
          </template>
          <template v-else>
            <RouterLink to="/login"><el-button round>登录</el-button></RouterLink>
            <RouterLink to="/register"><el-button type="primary" round>注册</el-button></RouterLink>
          </template>
        </div>
      </div>
    </header>
    <main class="container content-area">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.hero-bar {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(14px);
  background: rgba(247, 241, 232, 0.72);
  border-bottom: 1px solid rgba(100, 68, 38, 0.08);
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 76px;
  gap: 16px;
}

.brand {
  font-size: 28px;
  font-weight: 800;
  color: var(--brand-deep);
}

.nav-links {
  display: flex;
  gap: 24px;
  color: var(--muted);
  flex-wrap: wrap;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome {
  color: var(--brand-deep);
  font-weight: 600;
}

.content-area {
  padding: 28px 0 40px;
}
</style>
