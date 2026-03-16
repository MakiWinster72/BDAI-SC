import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AchievementsView from '../views/AchievementsView.vue'
import MyInfosView from '../views/MyInfosView.vue'
import StudentInfoView from '../views/StudentInfoView.vue'
import SettingsView from '../views/SettingsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/myinfos' },
    { path: '/achievements', name: 'achievements', component: AchievementsView, meta: { requiresAuth: true } },
    { path: '/myinfos', name: 'myinfos', component: MyInfosView, meta: { requiresAuth: true } },
    { path: '/settings', name: 'settings', component: SettingsView, meta: { requiresAuth: true } },
    {
      path: '/student-info',
      name: 'student-info',
      component: StudentInfoView,
      meta: { requiresAuth: true, allowedRoles: ['TEACHER', 'ADMIN'] }
    },
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
    return '/myinfos'
  }
  if (to.meta.allowedRoles) {
    const raw = JSON.parse(localStorage.getItem('gcsc_user') || '{}')
    const role = raw.role || 'STUDENT'
    if (!to.meta.allowedRoles.includes(role)) {
      return '/myinfos'
    }
  }
  return true
})

export default router
