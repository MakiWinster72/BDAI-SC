import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import HomeView from '../views/HomeView.vue'
import AchievementsView from '../views/AchievementsView.vue'
import MyInfosView from '../views/MyInfosView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true } },
    { path: '/congra', name: 'congra', component: HomeView, meta: { requiresAuth: true } },
    { path: '/memory', name: 'memory', component: HomeView, meta: { requiresAuth: true } },
    { path: '/contacts', name: 'contacts', component: HomeView, meta: { requiresAuth: true } },
    { path: '/achievements', name: 'achievements', component: AchievementsView, meta: { requiresAuth: true } },
    { path: '/myinfos', name: 'myinfos', component: MyInfosView, meta: { requiresAuth: true } },
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
