import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import PostDetailView from '../views/PostDetailView.vue'
import EditorView from '../views/EditorView.vue'
import MessagesView from '../views/MessagesView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import SearchView from '../views/SearchView.vue'
import ProfileView from '../views/ProfileView.vue'
import HotView from '../views/HotView.vue'
import AiView from '../views/AiView.vue'
import SettingsView from '../views/SettingsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'post/:id', name: 'post-detail', component: PostDetailView },
        { path: 'editor', name: 'editor', component: EditorView },
        { path: 'messages', name: 'messages', component: MessagesView },
        { path: 'notifications', name: 'notifications', component: NotificationsView },
        { path: 'search', name: 'search', component: SearchView },
        { path: 'profile/:id', name: 'profile', component: ProfileView },
        { path: 'hot', name: 'hot', component: HotView },
        { path: 'ai', name: 'ai', component: AiView },
        { path: 'settings', name: 'settings', component: SettingsView }
      ]
    },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView }
  ]
})

export default router
