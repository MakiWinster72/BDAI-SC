<script setup>
import { onMounted, reactive, shallowRef } from "vue";
import MobileCapsule from "@/components/MobileCapsule.vue";
import AdminBackupTab from "@/components/admin/AdminBackupTab.vue";
import AdminReviewSettingsTab from "@/components/admin/AdminReviewSettingsTab.vue";
import AdminSystemSettingsTab from "@/components/admin/AdminSystemSettingsTab.vue";
import AdminUploadSettingsTab from "@/components/admin/AdminUploadSettingsTab.vue";
import AdminUsersTab from "@/components/admin/AdminUsersTab.vue";
import AdminUserAddModal from "@/components/admin/AdminUserAddModal.vue";
import AdminUserEditModal from "@/components/admin/AdminUserEditModal.vue";
import {
  ADMIN_TABS,
  ATTACHMENT_TYPE_OPTIONS,
  ROLE_OPTIONS,
  getAdminRoleLabel,
} from "@/config/adminConfig";
import { useAdminBackup } from "@/composables/useAdminBackup";
import { useAdminSettingsPanels } from "@/composables/useAdminSettingsPanels";
import { useAdminSystemSettings } from "@/composables/useAdminSystemSettings";
import { useAdminUsers } from "@/composables/useAdminUsers";
import { useDashboardShell } from "@/composables/useDashboardShell";

const { openSidebar: openDashboardSidebar } = useDashboardShell();
const activeSection = shallowRef("upload");

const backup = reactive(useAdminBackup());
const systemPanel = reactive(useAdminSystemSettings());
const settingsPanels = reactive(useAdminSettingsPanels(activeSection));
const users = reactive(useAdminUsers(activeSection));

function switchSection(sectionKey) {
  activeSection.value = sectionKey;
  settingsPanels.saveMessage = "";
}

async function loadPage() {
  await Promise.all([
    settingsPanels.loadSettingsPanels(),
    systemPanel.fetchSystemSettings(),
  ]);
}

onMounted(() => {
  loadPage();
  users.loadUsers();
});
</script>

<template>
  <main class="admin-shell">
    <header class="admin-header">
      <h1 class="admin-title">后台管理</h1>
    </header>

    <nav class="admin-tabs" role="tablist" aria-label="管理功能分类">
      <button
        v-for="tab in ADMIN_TABS"
        :key="tab.key"
        class="admin-tab"
        :class="{ active: activeSection === tab.key }"
        role="tab"
        :aria-selected="activeSection === tab.key"
        type="button"
        @click="switchSection(tab.key)"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" :d="tab.icon" />
        </svg>
        {{ tab.label }}
      </button>
    </nav>

    <Transition name="section-fade" mode="out-in">
      <div :key="activeSection" class="admin-content">
        <AdminUploadSettingsTab
          v-if="activeSection === 'upload'"
          :form="settingsPanels.form"
          :attachment-type-options="ATTACHMENT_TYPE_OPTIONS"
          :error-message="settingsPanels.uploadErrorMessage"
          :saving="settingsPanels.uploadSaving"
          @update-form-field="settingsPanels.updateUploadFormField"
          @reset="settingsPanels.syncFormFromSettings"
          @save="settingsPanels.handleSubmit"
        />

        <AdminReviewSettingsTab
          v-else-if="activeSection === 'review'"
          :review-form="settingsPanels.reviewForm"
          :error-message="settingsPanels.activeErrorMessage"
          :saving="settingsPanels.activeSaving"
          @update-review-field="settingsPanels.updateReviewFormField"
          @reset="settingsPanels.syncReviewFormFromSettings"
          @save="settingsPanels.handleReviewSubmit"
        />

        <AdminUsersTab
          v-else-if="activeSection === 'users'"
          v-model:user-search="users.userSearch"
          v-model:user-role-filter="users.userRoleFilter"
          :users="users.users"
          :users-loading="users.usersLoading"
          :users-error="users.usersError"
          :user-total="users.userTotal"
          :role-options="ROLE_OPTIONS"
          :selected-user-ids="users.selectedUserIds"
          :some-selected="users.someSelected"
          :all-page-selected="users.allPageSelected"
          :user-current-page="users.userCurrentPage"
          :user-pages="users.userPages"
          :get-role-label="getAdminRoleLabel"
          @open-add-user="users.openAddUserModal"
          @select-all-page="users.selectAllPage"
          @select-all-filtered="users.selectAllFiltered"
          @clear-selection="users.selectedUserIds = new Set()"
          @delete-selected="users.handleDeleteSelectedUsers"
          @toggle-select-all-page="users.toggleSelectAllPage"
          @toggle-user-select="users.toggleUserSelect"
          @edit-user="users.openEditModal"
          @update:user-current-page="users.loadUsers"
        />

        <AdminBackupTab
          v-else-if="activeSection === 'backup'"
          :backup-form="backup.backupForm"
          :backup-loading="backup.backupLoading"
          :restore-loading="backup.restoreLoading"
          :storage-data="backup.storageData"
          :storage-loading="backup.storageLoading"
          :storage-error="backup.storageError"
          :storage-deleting="backup.storageDeleting"
          :storage-label="backup.storageLabel"
          :bar-width="backup.barWidth"
          @backup-db="backup.handleBackupDb"
          @backup-zip="backup.handleBackupZip"
          @sql-file-change="backup.onSqlFileChange"
          @zip-file-change="backup.onZipFileChange"
          @restore-db="backup.handleRestore"
          @restore-attachments="backup.handleRestoreAttachments"
          @refresh-storage="backup.fetchStorageAnalysis"
          @delete-storage="backup.handleDeleteStorage"
        />

        <AdminSystemSettingsTab
          v-else-if="activeSection === 'other'"
          :settings="systemPanel.systemSettings"
          :message="systemPanel.systemSettingsMsg"
          @update-setting="systemPanel.updateSystemSetting"
          @save="systemPanel.handleSaveSystemSettings"
          @step-threshold="systemPanel.stepThreshold"
        />
      </div>
    </Transition>

    <AdminUserEditModal
      :modal="users.editModal"
      :current-major-options="users.currentMajorOptions"
      @close="users.closeEditModal"
      @save="users.handleUpdateUser"
      @delete="users.handleDeleteUser"
      @add-class="users.addTeacherAssignedClass"
      @remove-class="users.removeTeacherAssignedClass"
    />

    <AdminUserAddModal
      :modal="users.addUserModal"
      :import-file-ref="users.importFileRef"
      @close="users.closeAddUserModal"
      @create="users.handleCreateUser"
      @import-file="users.handleImportFile"
    />

    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template #right>
        <button
          v-for="tab in ADMIN_TABS"
          :key="tab.key"
          class="capsule-action admin-capsule-btn"
          :class="{ 'capsule-active': activeSection === tab.key }"
          type="button"
          :aria-label="tab.label"
          @click="switchSection(tab.key)"
        >
          <span class="capsule-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path :d="tab.icon" />
            </svg>
          </span>
          <span class="admin-capsule-label">{{ tab.shortLabel }}</span>
        </button>
      </template>
    </MobileCapsule>
  </main>
</template>

<style>
@import "@/assets/styles/admin-view.css";
</style>

<style scoped>
:deep(.capsule-left) {
  padding: 10px 2px 10px 6px;
}
:deep(.capsule-right) {
  flex: 1;
  justify-content: space-evenly;
  padding: 10px 6px 10px 2px;
}

.admin-capsule-btn {
  flex-shrink: 0;
  flex-direction: column;
  gap: 1px;
  color: var(--primary);
  padding: 6px clamp(3px, 2.2vw, 10px);
  border: 1px solid rgba(100, 12, 114, 0.12);
}
.admin-capsule-btn .capsule-icon {
  flex-shrink: 0;
}

.admin-capsule-label {
  font-size: 9px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.02em;
}

.capsule-action:active {
  background: rgba(100, 12, 114, 0.08);
}

.capsule-action.capsule-active {
  color: #fff;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

@media (max-width: 840px) {
  .admin-shell {
    padding-bottom: calc(140px + env(safe-area-inset-bottom, 0px));
  }
}
</style>
