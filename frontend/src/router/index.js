import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import AchievementsView from '@/views/AchievementsView.vue'
import MyInfosView from '@/views/MyInfosView.vue'
import NotificationsView from '@/views/NotificationsView.vue'
import StudentInfoView from '@/views/StudentInfoView.vue'
import SettingsView from '@/views/SettingsView.vue'
import AdminView from '@/views/AdminView.vue'
import LogsView from '@/views/LogsView.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import ChangePasswordView from '@/views/ChangePasswordView.vue'
import { checkStudentMustChangePassword } from '@/config/authConfig'
import { loadUser } from '@/utils/userStorage'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.name === 'myinfos' && from.name !== 'myinfos') {
      return { top: 0, left: 0, behavior: 'instant' }
    }
    // 保持当前滚动位置不变
    return { top: window.scrollY, left: window.scrollX, behavior: 'instant' }
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
        },
        {
          path: 'logs',
          name: 'logs',
          component: LogsView,
          meta: { allowedRoles: ['ADMIN'] }
        },
        {
          path: 'class-reviews',
          name: 'class-reviews',
          redirect: { path: '/notifications', query: { panel: 'class-reviews', category: 'pending' } },
        }
      ]
    },
    { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
    {
      path: '/change-password',
      name: 'change-password',
      component: ChangePasswordView,
      meta: { requiresAuth: true, forcePasswordChange: true },
    },
  ]
})

router.beforeEach((to) => {
  const isLoggedIn = Boolean(localStorage.getItem('bdai_sc_token'))
  const user = loadUser()
  const mustChangePassword = checkStudentMustChangePassword(user)

  if (to.meta.requiresAuth && !isLoggedIn) {
    return '/login'
  }

  if (isLoggedIn && mustChangePassword && to.path !== '/change-password') {
    return '/change-password'
  }

  if (to.path === '/change-password') {
    if (!isLoggedIn) {
      return '/login'
    }
    if (!mustChangePassword) {
      return '/myinfos'
    }
    return true
  }

  if (to.meta.guestOnly && isLoggedIn) {
    return mustChangePassword ? '/change-password' : '/myinfos'
  }
  if (to.meta.allowedRoles) {
    const role = loadUser().role
    if (!to.meta.allowedRoles.includes(role)) {
      return '/myinfos'
    }
  }
  if (to.path === '/register') {
    const allowReg = localStorage.getItem('gcsc_allowRegistration')
    if (allowReg !== '1') {
      return '/login'
    }
  }
  return true
})

export default router
