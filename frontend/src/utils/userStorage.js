/**
 * userStorage - 用户数据持久化工具
 *
 * loadUser / saveUser 在多个视图和组件中重复定义，统一管理。
 */

const STORAGE_KEY = 'gcsc_user';

/**
 * 从 localStorage 加载当前用户信息
 * @returns {{ username: string, displayName: string, avatarUrl: string, role: string, studentNo: string, className: string, college: string, studentCategory: string }}
 */
export function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
    return {
      username: raw.username || '',
      displayName: raw.displayName || '',
      avatarUrl: raw.avatarUrl || '',
      role: raw.role || 'STUDENT',
      studentNo: raw.studentNo || '',
      className: raw.className || '',
      college: raw.college || '',
      studentCategory: raw.studentCategory || '',
    };
  } catch {
    return {
      username: '',
      displayName: '',
      avatarUrl: '',
      role: 'STUDENT',
      studentNo: '',
      className: '',
      college: '',
      studentCategory: '',
    };
  }
}

/**
 * 保存用户信息到 localStorage
 * @param {object} data
 */
export function saveUser(data) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}
