import { defineStore } from 'pinia'
import request from '../api/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    profile: JSON.parse(localStorage.getItem('profile') || 'null')
  }),
  actions: {
    async login(payload) {
      const { data } = await request.post('/auth/login', payload)
      this.token = data.data.token
      this.profile = {
        userId: data.data.userId,
        nickname: data.data.nickname,
        avatar: data.data.avatar
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('profile', JSON.stringify(this.profile))
    },
    setAuth(payload) {
      this.token = payload.token
      this.profile = payload.profile
      localStorage.setItem('token', payload.token)
      localStorage.setItem('profile', JSON.stringify(payload.profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('token')
      localStorage.removeItem('profile')
    }
  }
})
