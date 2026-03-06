import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true } },
    { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } }
  ]
})

router.beforeEach((to) => {
  const isLoggedIn = Boolean(localStorage.getItem('gcsc_token'))
  if (to.meta.requiresAuth && !isLoggedIn) {
    return '/login'
  }
  if (to.meta.guestOnly && isLoggedIn) {
    return '/home'
  }
  return true
})

export default router
