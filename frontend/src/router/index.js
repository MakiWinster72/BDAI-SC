import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AchievementsView from '../views/AchievementsView.vue'
import MyInfosView from '../views/MyInfosView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import StudentInfoView from '../views/StudentInfoView.vue'
import SettingsView from '../views/SettingsView.vue'
import AdminView from '../views/AdminView.vue'
import DashboardLayout from '../layouts/DashboardLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.path !== from.path) {
      return { top: 0 }
    }
    return false
  },
  routes: [
    {
      path: '/',
      component: DashboardLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/myinfos' },
        { path: 'notifications', name: 'notifications', component: NotificationsView },
        { path: 'achievements', name: 'achievements', component: AchievementsView },
        { path: 'myinfos', name: 'myinfos', component: MyInfosView },
        { path: 'settings', name: 'settings', component: SettingsView },
        {
          path: 'student-info',
          name: 'student-info',
          component: StudentInfoView,
          meta: { allowedRoles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'admin',
          name: 'admin',
          component: AdminView,
          meta: { allowedRoles: ['ADMIN'] }
        }
      ]
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
  if (to.path === '/register') {
    const allowReg = localStorage.getItem('gcsc_allowRegistration')
    if (allowReg === '0') {
      return '/login'
    }
  }
  return true
})

export default router
