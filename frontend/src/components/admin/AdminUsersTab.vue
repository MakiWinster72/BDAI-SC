<script setup>
import { computed } from "vue";
import PaginationBar from "@/components/PaginationBar.vue";

const props = defineProps({
  users: {
    type: Array,
    default: () => [],
  },
  usersLoading: {
    type: Boolean,
    default: false,
  },
  usersError: {
    type: String,
    default: "",
  },
  userTotal: {
    type: Number,
    default: 0,
  },
  userSearch: {
    type: String,
    default: "",
  },
  userRoleFilter: {
    type: String,
    default: "",
  },
  roleOptions: {
    type: Array,
    default: () => [],
  },
  selectedUserIds: {
    type: Object,
    required: true,
  },
  someSelected: {
    type: Boolean,
    default: false,
  },
  allPageSelected: {
    type: Boolean,
    default: false,
  },
  userCurrentPage: {
    type: Number,
    default: 1,
  },
  userPages: {
    type: Number,
    default: 1,
  },
  getRoleLabel: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  "update:user-search",
  "update:user-role-filter",
  "update:user-current-page",
  "open-add-user",
  "select-all-page",
  "select-all-filtered",
  "clear-selection",
  "delete-selected",
  "toggle-select-all-page",
  "toggle-user-select",
  "edit-user",
]);

const userSearchModel = computed({
  get: () => props.userSearch,
  set: (value) => emit("update:user-search", value),
});

const userRoleFilterModel = computed({
  get: () => props.userRoleFilter,
  set: (value) => emit("update:user-role-filter", value),
});
</script>

<template>
  <div class="admin-panel-single">
    <div class="card admin-card users-card">
      <div class="users-card-header">
        <div class="users-card-meta">
          <div class="users-card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <div class="users-card-titles">
            <div class="card-kicker">用户管理</div>
            <h2 class="card-title">系统用户列表</h2>
          </div>
        </div>
        <div class="users-count-badge">
          <span class="count-num">{{ userTotal }}</span>
          <span class="count-label">位用户</span>
        </div>
        <button
          class="btn btn-primary"
          type="button"
          @click="emit('open-add-user')"
        >
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          添加用户
        </button>
      </div>

      <div class="users-toolbar">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="userSearchModel"
            class="search-input"
            type="text"
            placeholder="搜索用户名、姓名、学号…"
            aria-label="搜索用户"
          />
        </div>
        <select v-model="userRoleFilterModel" class="filter-select" aria-label="按角色筛选">
          <option value="">全部角色</option>
          <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>

      <Transition name="msg-fade">
        <div v-show="someSelected" class="users-selection-bar">
          <span class="selection-count">已选择 {{ selectedUserIds.size }} 个用户</span>
          <button class="btn btn-ghost" type="button" @click="emit('select-all-page')">
            本页
          </button>
          <button class="btn btn-ghost" type="button" @click="emit('select-all-filtered')">
            全部
          </button>
          <button class="btn btn-ghost" type="button" @click="emit('clear-selection')">
            取消
          </button>
          <button class="btn btn-danger-ghost" type="button" @click="emit('delete-selected')">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            删除
          </button>
        </div>
      </Transition>

      <div class="users-content">
        <div v-if="usersLoading" class="users-center-state">
          <svg class="loading-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
          </svg>
          <span>加载中…</span>
        </div>
        <div v-else-if="usersError" class="users-center-state">
          <svg class="state-icon error" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.770.833.192 2.5 1.732 2.5z" />
          </svg>
          <span>{{ usersError }}</span>
        </div>
        <div v-else-if="users.length === 0" class="users-center-state">
          <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span>暂无符合条件的用户</span>
        </div>
        <div v-else class="table-wrap">
          <table class="users-table" aria-label="用户列表">
            <thead>
              <tr>
                <th scope="col" class="col-checkbox">
                  <input
                    type="checkbox"
                    :checked="allPageSelected"
                    :indeterminate="someSelected && !allPageSelected"
                    aria-label="全选"
                    @change="emit('toggle-select-all-page')"
                  />
                </th>
                <th scope="col" style="width: 130px;">用户名</th>
                <th scope="col" style="width: 100px;">显示名称</th>
                <th scope="col" style="width: 80px;">角色</th>
                <th scope="col" style="width: 120px;">学号</th>
                <th scope="col">班级</th>
                <th scope="col" style="width: 120px;">备注</th>
                <th scope="col" class="col-action"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id" class="user-row">
                <td class="col-checkbox">
                  <input
                    type="checkbox"
                    :checked="selectedUserIds.has(user.id)"
                    :aria-label="`选择 ${user.displayName}`"
                    @change="emit('toggle-user-select', user.id)"
                  />
                </td>
                <td class="td-mono">{{ user.username }}</td>
                <td>{{ user.displayName }}</td>
                <td>
                  <span :class="['role-chip', 'role-' + user.role.toLowerCase()]">
                    {{ getRoleLabel(user.role) }}
                  </span>
                </td>
                <td class="td-mono">{{ user.studentNo || '—' }}</td>
                <td>{{ user.className || '—' }}</td>
                <td class="td-remark">{{ user.remark || '—' }}</td>
                <td class="td-action">
                  <button class="icon-btn" @click.stop="emit('edit-user', user)" aria-label="编辑用户">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <PaginationBar
        v-if="userPages > 1"
        :current-page="userCurrentPage"
        :total-pages="userPages"
        mode="simple"
        @update:current-page="emit('update:user-current-page', $event)"
      />
    </div>
  </div>
</template>
