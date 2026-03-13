<template>
  <div class="profile-layout">
    <section class="card-panel profile-card">
      <div class="avatar-ring">{{ initial }}</div>
      <h1>{{ profile.nickname }}</h1>
      <p>{{ profile.bio || '这个人很酷，还没写简介。' }}</p>
      <div class="meta-grid">
        <div><strong>{{ profile.postCount || 0 }}</strong><span>帖子</span></div>
        <div><strong>{{ profile.followerCount || 0 }}</strong><span>粉丝</span></div>
        <div><strong>{{ profile.followingCount || 0 }}</strong><span>关注</span></div>
      </div>
      <div class="badge-row">
        <span v-for="badge in profile.badges || []" :key="badge" class="badge-chip">{{ badge }}</span>
      </div>
    </section>
    <section class="card-panel info-card">
      <h2>账号信息</h2>
      <div class="info-item"><span>邮箱</span><strong>{{ profile.email }}</strong></div>
      <div class="info-item"><span>角色</span><strong>{{ profile.role }}</strong></div>
      <div class="info-item"><span>收到点赞</span><strong>{{ profile.receivedLikeCount || 0 }}</strong></div>
      <h2 style="margin-top: 12px">Ta 的帖子</h2>
      <RouterLink v-for="post in posts" :key="post.id" :to="`/post/${post.id}`" class="post-link">
        <strong>{{ post.title }}</strong>
        <span>{{ post.createTime }}</span>
      </RouterLink>
    </section>
  </div>
</template>

<script setup>
import request from '../api/request'
import { useRoute } from 'vue-router'

const route = useRoute()
const profile = ref({})
const posts = ref([])
const initial = computed(() => (profile.value.nickname || '牛').slice(0, 1))

async function loadProfile() {
  const [{ data: profileData }, { data: postData }] = await Promise.all([
    request.get(`/profile/${route.params.id}`),
    request.get(`/profile/${route.params.id}/posts`)
  ])
  profile.value = profileData.data || {}
  posts.value = postData.data || []
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.profile-card, .info-card { padding: 30px; }
.avatar-ring { width: 92px; height: 92px; border-radius: 50%; display: grid; place-items: center; background: linear-gradient(135deg, var(--gold), var(--brand)); color: white; font-size: 34px; font-weight: 700; }
.meta-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 20px; }
.meta-grid div, .info-item, .post-link { padding: 14px; border-radius: 16px; background: rgba(255, 248, 239, 0.78); border: 1px solid var(--line); }
.meta-grid strong, .info-item strong { display: block; font-size: 22px; }
.meta-grid span, .info-item span { color: var(--muted); }
.badge-row { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 18px; }
.badge-chip { padding: 8px 12px; border-radius: 999px; background: rgba(184, 92, 56, 0.12); color: var(--brand-deep); }
.info-card { display: grid; gap: 12px; }
.post-link { display: flex; justify-content: space-between; gap: 12px; }
@media (max-width: 900px) { .profile-layout { grid-template-columns: 1fr; } }
</style>
