<script setup>
import { computed, onMounted, reactive, ref, shallowRef, watch } from "vue";
import PaginationBar from "../components/PaginationBar.vue";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { useReviewSettings } from "../composables/useReviewSettings";
import { getUserList, updateUser, deleteUser, createUser, getAllUserIds, getSystemSettings, updateSystemSettings, downloadBackupDb, restoreBackupDb, downloadBackupAttachments, restoreBackupAttachments, updateTeacherAssignedClasses } from "../api/admin";
import { useToast } from "../composables/useToast";
import { loadUser } from "../utils/userStorage";
import { buildClassName } from "../utils/profile";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const activeSection = shallowRef("upload");

// Backup & Restore
const backupForm = reactive({
  sqlFile: null,
  zipFile: null,
});
const backupLoading = shallowRef(false);
const restoreLoading = shallowRef(false);
const { success, error } = useToast();

async function handleBackupDb() {
  backupLoading.value = true;
  try {
    const res = await downloadBackupDb();
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      error(errData.message || "备份失败");
      return;
    }
    const blob = await res.blob();
    const filename = res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] || "bdai_sc_backup.sql";
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
    success("SQL 文件已下载");
  } catch (e) {
    error("备份失败，请检查服务端 mysqldump 是否可用");
  } finally {
    backupLoading.value = false;
  }
}

async function handleBackupZip() {
  backupLoading.value = true;
  try {
    const res = await downloadBackupAttachments();
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      error(errData.message || "打包失败");
      return;
    }
    const blob = await res.blob();
    const filename = res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] || "bdai_sc_attachments.zip";
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
    success("ZIP 文件已下载");
  } catch (e) {
    error("打包失败，请稍后重试");
  } finally {
    backupLoading.value = false;
  }
}

async function handleRestore() {
  if (!backupForm.sqlFile) {
    error("请先选择 SQL 备份文件");
    return;
  }
  restoreLoading.value = true;
  try {
    const res = await restoreBackupDb(backupForm.sqlFile);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      error(data.message || "恢复失败");
      return;
    }
    success("数据库恢复成功");
    backupForm.sqlFile = null;
    const input = document.getElementById("sql-file");
    if (input) input.value = "";
  } catch (e) {
    error("恢复失败，请检查服务端 mysql 是否可用");
  } finally {
    restoreLoading.value = false;
  }
}

async function handleRestoreAttachments() {
  if (!backupForm.zipFile) {
    error("请先选择附件压缩包");
    return;
  }
  restoreLoading.value = true;
  try {
    const res = await restoreBackupAttachments(backupForm.zipFile);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      error(data.message || "恢复失败");
      return;
    }
    success("附件恢复成功");
    backupForm.zipFile = null;
    const input = document.getElementById("zip-file");
    if (input) input.value = "";
  } catch (e) {
    error("恢复失败，请稍后重试");
  } finally {
    restoreLoading.value = false;
  }
}

function onSqlFileChange(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  if (!file.name.endsWith(".sql")) {
    error("请选择 .sql 格式的备份文件");
    backupForm.sqlFile = null;
    return;
  }
  backupForm.sqlFile = file;
}

function onZipFileChange(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  if (!file.name.toLowerCase().endsWith(".zip")) {
    error("请选择 .zip 格式的压缩文件");
    backupForm.zipFile = null;
    return;
  }
  backupForm.zipFile = file;
}
const saveMessage = shallowRef("");
const sectionKey = shallowRef(0); // force section re-render for transition
const users = shallowRef([]);
const usersLoading = shallowRef(false);
const usersError = shallowRef("");
const userSearch = shallowRef("");
const userRoleFilter = shallowRef("");
const userCurrentPage = shallowRef(1);
const userPageSize = shallowRef(20);
const userTotal = shallowRef(0);
const userPages = computed(() => Math.ceil(userTotal.value / userPageSize.value));
const selectedUserIds = shallowRef(new Set());
const allFilteredSelected = shallowRef(false);
const someSelected = computed(() => selectedUserIds.value.size > 0);
const allPageSelected = computed(() => users.value.length > 0 && selectedUserIds.value.size === users.value.length);

const systemSettings = reactive({ allowRegistration: true, delayedThresholdDays: 2 });
const systemSettingsMsg = shallowRef("");

async function fetchSystemSettings() {
  try {
    const res = await getSystemSettings();
    systemSettings.allowRegistration = res.data.allowRegistration !== false;
    systemSettings.delayedThresholdDays = res.data.delayedThresholdDays || 2;
  } catch (e) {
    // ignore
  }
}

async function handleSaveSystemSettings() {
  systemSettingsMsg.value = "";
  try {
    await updateSystemSettings({ allowRegistration: systemSettings.allowRegistration, delayedThresholdDays: Number(systemSettings.delayedThresholdDays) });
    systemSettingsMsg.value = "设置已保存";
    setTimeout(() => { systemSettingsMsg.value = ""; }, 2000);
  } catch (e) {
    error("保存失败");
  }
}

function toggleUserSelect(id) {
  const s = new Set(selectedUserIds.value);
  if (s.has(id)) s.delete(id);
  else s.add(id);
  selectedUserIds.value = s;
  allFilteredSelected.value = false;
}

function selectAllPage() {
  selectedUserIds.value = new Set(users.value.map(u => u.id));
  allFilteredSelected.value = false;
}

async function selectAllFiltered() {
  try {
    const res = await getAllUserIds({
      search: userSearch.value.trim() || undefined,
      role: userRoleFilter.value || undefined,
    });
    selectedUserIds.value = new Set(res.data || []);
    allFilteredSelected.value = true;
  } catch (e) {
    error("获取用户列表失败");
  }
}

async function handleDeleteSelectedUsers() {
  if (selectedUserIds.value.size === 0) return;
  if (!confirm(`确定要删除选中的 ${selectedUserIds.value.size} 个用户吗？此操作不可恢复。`)) return;
  try {
    for (const id of selectedUserIds.value) {
      await deleteUser(id);
    }
    selectedUserIds.value = new Set();
    allFilteredSelected.value = false;
    await loadUsers(userCurrentPage.value);
    success("已删除选中的用户");
  } catch (e) {
    error(e?.response?.data?.message || "删除失败");
  }
}
const {
  settings,
  loading: uploadLoading,
  saving: uploadSaving,
  errorMessage: uploadErrorMessage,
  fetchSettings: fetchUploadSettings,
  saveSettings: saveUploadSettings,
} = useAchievementUploadSettings();
const {
  settings: reviewSettings,
  loading: reviewLoading,
  saving: reviewSaving,
  errorMessage: reviewErrorMessage,
  fetchSettings: fetchReviewSettings,
  saveSettings: saveReviewSettings,
} = useReviewSettings();

const form = reactive({
  imageMaxCount: settings.imageMaxCount,
  imageMaxSizeMb: settings.imageMaxSizeMb,
  attachmentMaxCount: settings.attachmentMaxCount,
  attachmentMaxSizeMb: settings.attachmentMaxSizeMb,
  attachmentDocumentExts: settings.attachmentDocumentExts,
  attachmentVideoExts: settings.attachmentVideoExts,
  attachmentImageExts: settings.attachmentImageExts,
  attachmentArchiveExts: settings.attachmentArchiveExts,
  supportingDocMaxCount: settings.supportingDocMaxCount,
  supportingDocMaxSizeMb: settings.supportingDocMaxSizeMb,
});
const reviewForm = reactive({
  profileReviewEnabled: reviewSettings.profileReviewEnabled,
  profileReviewAutoApprove: reviewSettings.profileReviewAutoApprove,
  achievementReviewEnabled: reviewSettings.achievementReviewEnabled,
  achievementReviewAutoApprove: reviewSettings.achievementReviewAutoApprove,
});

const imageSubtitle = computed(
  () => `最多 ${form.imageMaxCount} 张 · 单张不超过 ${form.imageMaxSizeMb}MB`,
);
const attachmentSubtitle = computed(
  () => `最多 ${form.attachmentMaxCount} 个 · 单个不超过 ${form.attachmentMaxSizeMb}MB`,
);
const supportingDocSubtitle = computed(
  () => `最多 ${form.supportingDocMaxCount} 个 · 单个不超过 ${form.supportingDocMaxSizeMb}MB`,
);
const enabledPreviewTypes = computed(() =>
  ATTACHMENT_TYPE_OPTIONS.map((item) => ({
    ...item,
    exts: parseExts(form[extFieldKey(item.key)]),
  })).filter((item) => item.exts.length),
);
const attachmentTypeSummary = computed(() =>
  enabledPreviewTypes.value.length
    ? enabledPreviewTypes.value.map((item) => item.label).join(" / ")
    : "暂无可用附件类型",
);
const activeLoading = computed(() =>
  activeSection.value === "upload" ? uploadLoading.value : reviewLoading.value,
);
const activeSaving = computed(() =>
  activeSection.value === "upload" ? uploadSaving.value : reviewSaving.value,
);
const activeErrorMessage = computed(() =>
  activeSection.value === "upload"
    ? uploadErrorMessage.value
    : reviewErrorMessage.value,
);

function extFieldKey(typeKey) {
  if (typeKey === "document") return "attachmentDocumentExts";
  if (typeKey === "video") return "attachmentVideoExts";
  if (typeKey === "image") return "attachmentImageExts";
  return "attachmentArchiveExts";
}

function parseExts(value) {
  return (value || "")
    .split(",")
    .map((item) => item.trim().toLowerCase().replace(/^\./, ""))
    .filter(Boolean);
}

async function loadPage() {
  await Promise.all([fetchUploadSettings(), fetchReviewSettings(), fetchSystemSettings()]);
  syncFormFromSettings();
  syncReviewFormFromSettings();
}

function syncFormFromSettings() {
  form.imageMaxCount = settings.imageMaxCount;
  form.imageMaxSizeMb = settings.imageMaxSizeMb;
  form.attachmentMaxCount = settings.attachmentMaxCount;
  form.attachmentMaxSizeMb = settings.attachmentMaxSizeMb;
  form.attachmentDocumentExts = settings.attachmentDocumentExts;
  form.attachmentVideoExts = settings.attachmentVideoExts;
  form.attachmentImageExts = settings.attachmentImageExts;
  form.attachmentArchiveExts = settings.attachmentArchiveExts;
  form.supportingDocMaxCount = settings.supportingDocMaxCount;
  form.supportingDocMaxSizeMb = settings.supportingDocMaxSizeMb;
}

function syncReviewFormFromSettings() {
  reviewForm.profileReviewEnabled = reviewSettings.profileReviewEnabled;
  reviewForm.profileReviewAutoApprove = reviewSettings.profileReviewAutoApprove;
  reviewForm.achievementReviewEnabled = reviewSettings.achievementReviewEnabled;
  reviewForm.achievementReviewAutoApprove =
    reviewSettings.achievementReviewAutoApprove;
}

async function handleSubmit() {
  saveMessage.value = "";
  const result = await saveUploadSettings({
    imageMaxCount: Number(form.imageMaxCount),
    imageMaxSizeMb: Number(form.imageMaxSizeMb),
    attachmentMaxCount: Number(form.attachmentMaxCount),
    attachmentMaxSizeMb: Number(form.attachmentMaxSizeMb),
    attachmentDocumentExts: form.attachmentDocumentExts,
    attachmentVideoExts: form.attachmentVideoExts,
    attachmentImageExts: form.attachmentImageExts,
    attachmentArchiveExts: form.attachmentArchiveExts,
    supportingDocMaxCount: Number(form.supportingDocMaxCount),
    supportingDocMaxSizeMb: Number(form.supportingDocMaxSizeMb),
  });
  if (result.success) {
    saveMessage.value = "上传限制已更新，成就页面会同步显示。";
    syncFormFromSettings();
    success("上传限制设置已保存");
  }
}

async function handleReviewSubmit() {
  saveMessage.value = "";
  const result = await saveReviewSettings({
    profileReviewEnabled: Boolean(reviewForm.profileReviewEnabled),
    profileReviewAutoApprove: reviewForm.profileReviewEnabled
      ? Boolean(reviewForm.profileReviewAutoApprove)
      : false,
    achievementReviewEnabled: Boolean(reviewForm.achievementReviewEnabled),
    achievementReviewAutoApprove: reviewForm.achievementReviewEnabled
      ? Boolean(reviewForm.achievementReviewAutoApprove)
      : false,
  });
  if (result.success) {
    saveMessage.value = "审核设置已更新，前台提交行为会按新规则执行。";
    syncReviewFormFromSettings();
    success("审核策略设置已保存");
  }
}

function switchSection(sectionKey_) {
  activeSection.value = sectionKey_;
  saveMessage.value = "";
}

// User management
const ROLE_LABELS = {
  ADMIN: "管理员",
  TEACHER: "教师",
  STUDENT: "学生",
  CADRE: "班干部",
};
const ROLE_OPTIONS = [
  { value: "STUDENT", label: "学生" },
  { value: "CADRE", label: "班干部" },
  { value: "TEACHER", label: "教师" },
  { value: "ADMIN", label: "管理员" },
];

const STUDENT_CATEGORY_OPTIONS = ["本科生", "研究生"];
const MAJOR_OPTIONS_BY_CATEGORY = {
  本科生: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程",
    "人工智能",
    "电子商务",
    "电子商务（大数据决策分析）",
    "大数据管理与应用",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
  研究生: [
    "管理科学与工程",
    "技术经济及管理",
    "智能科学与技术",
    "计算机技术",
    "图书情报",
  ],
};

function getRoleLabel(role) {
  return ROLE_LABELS[role] || role;
}

const currentMajorOptions = computed(() => {
  return MAJOR_OPTIONS_BY_CATEGORY[editModal.form.newClassCategory] || [];
});

async function loadUsers(page = userCurrentPage.value) {
  usersLoading.value = true;
  usersError.value = "";
  selectedUserIds.value = new Set();
  allFilteredSelected.value = false;
  try {
    const res = await getUserList({
      page,
      size: userPageSize.value,
      search: userSearch.value.trim() || undefined,
      role: userRoleFilter.value || undefined,
    });
    const payload = res.data;
    users.value = payload.data || [];
    userTotal.value = payload.total || 0;
    userCurrentPage.value = payload.page || page;
  } catch (e) {
    usersError.value = "加载用户列表失败";
  } finally {
    usersLoading.value = false;
  }
}

const editModal = reactive({
  visible: false,
  user: null,
  saving: false,
  error: "",
  form: {
    username: "",
    password: "",
    role: "",
    // Teacher assigned classes: list of full class name strings
    assignedClasses: [],
    // New class entry fields
    newClassYear: 2024,
    newClassCategory: "本科生",
    newClassMajor: "",
    newClassNo: 1,
    remark: "",
  },
});

const addUserModal = reactive({
  visible: false,
  saving: false,
  error: "",
  textarea: "",
});

const importFileRef = ref(null);

function openAddUserModal() {
  addUserModal.textarea = "";
  addUserModal.error = "";
  addUserModal.visible = true;
}

function closeAddUserModal() {
  addUserModal.visible = false;
}

async function handleCreateUser() {
  const lines = addUserModal.textarea.trim().split("\n").filter(l => l.trim());
  if (lines.length === 0) {
    addUserModal.error = "请输入用户信息";
    return;
  }
  // Parse and validate all lines first
  const parsedUsers = [];
  const errors = [];
  const seenUsernames = new Set();

  for (let i = 0; i < lines.length; i++) {
    const parts = lines[i].split(",").map(p => p.trim());
    if (parts.length < 3) {
      errors.push(`第 ${i + 1} 行：${parts[0] || '空'}，${parts[1] || '空'}，字段不足`);
      continue;
    }
    const [displayName, username, password] = parts;
    if (seenUsernames.has(username)) {
      errors.push(`第 ${i + 1} 行：${displayName}，${username}，重复`);
      continue;
    }
    seenUsernames.add(username);
    parsedUsers.push({ displayName, username, password, line: i + 1 });
  }

  if (errors.length > 0) {
    addUserModal.error = errors.slice(0, 5).join("；") + (errors.length > 5 ? `…还有 ${errors.length - 5} 条` : "");
    return;
  }

  if (parsedUsers.length === 0) {
    addUserModal.error = "没有有效用户数据";
    return;
  }

  addUserModal.saving = true;
  addUserModal.error = "";
  const created = [];
  const failed = [];
  try {
    for (const user of parsedUsers) {
      try {
        await createUser(user);
        created.push(user.username);
      } catch (e) {
        const msg = e?.response?.data?.message || "失败";
        failed.push(`${user.displayName}，${user.username}，${msg}`);
      }
    }
    await loadUsers(1);
    closeAddUserModal();
    if (failed.length > 0) {
      addUserModal.error = `已创建 ${created.length} 个用户，失败 ${failed.length} 个：${failed.slice(0, 3).join("；")}${failed.length > 3 ? "…" : ""}`;
    } else {
      success(`已创建 ${created.length} 个用户`);
    }
  } catch (e) {
    addUserModal.error = e?.response?.data?.message || "创建失败";
  } finally {
    addUserModal.saving = false;
  }
}

async function handleImportFile(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  const name = file.name.toLowerCase();
  try {
    let rawLines = [];
    if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
      const XLSX = await import("xlsx");
      const arrayBuffer = await file.arrayBuffer();
      const workbook = XLSX.read(arrayBuffer, { type: "array" });
      const sheet = workbook.Sheets[workbook.SheetNames[0]];
      const data = XLSX.utils.sheet_to_json(sheet, { header: 1 });
      rawLines = data.filter(r => r && r.length >= 3);
    } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
      const text = await file.text();
      rawLines = text.trim().split("\n").filter(l => l.trim());
    } else {
      addUserModal.error = "不支持的文件格式，请上传 xlsx、csv 或 txt 文件";
      e.target.value = "";
      return;
    }

    // Validate and find issues
    const errors = [];
    const seenUsernames = new Set();
    rawLines.forEach((row, idx) => {
      const parts = Array.isArray(row) ? row.map(p => String(p).trim()) : String(row).split(",").map(p => p.trim());
      const displayName = parts[0] || '空';
      const username = parts[1] || '空';
      if (parts.length < 3) {
        errors.push(`第 ${idx + 1} 行：${displayName}，${username}，字段不足`);
        return;
      }
      if (seenUsernames.has(username)) {
        errors.push(`第 ${idx + 1} 行：${displayName}，${username}，重复`);
      }
      seenUsernames.add(username);
    });

    const validRows = rawLines.map(r => {
      const parts = Array.isArray(r) ? r.map(p => String(p).trim()) : String(r).split(",").map(p => p.trim());
      return parts.slice(0, 3).join(",");
    });
    addUserModal.textarea = validRows.join("\n");

    if (errors.length > 0) {
      addUserModal.error = errors.slice(0, 5).join("；") + (errors.length > 5 ? `…还有 ${errors.length - 5} 条` : "");
    } else {
      addUserModal.error = "";
    }
  } catch (err) {
    addUserModal.error = "文件解析失败";
  }
  e.target.value = "";
}

async function openEditModal(user) {
  // Always get fresh user data from users array to avoid stale reference
  const freshUser = users.value.find(u => u.id === user.id) || user;
  editModal.user = freshUser;
  editModal.form.username = freshUser.username;
  editModal.form.password = "";
  editModal.form.role = freshUser.role;
  editModal.form.remark = freshUser.remark || "";
  // Load existing assigned classes for teachers
  const existingAssigned = freshUser.assignedClasses
    ? freshUser.assignedClasses.split(",").map(c => c.trim()).filter(Boolean)
    : [];
  editModal.form.assignedClasses = existingAssigned;
  editModal.form.newClassYear = 2024;
  editModal.form.newClassCategory = "本科生";
  editModal.form.newClassMajor = "";
  editModal.form.newClassNo = 1;
  editModal.error = "";
  editModal.visible = true;
}

function closeEditModal() {
  editModal.visible = false;
  editModal.user = null;
}

function addTeacherAssignedClass() {
  const { newClassYear, newClassMajor, newClassNo } = editModal.form;
  if (!newClassYear || !newClassMajor) {
    editModal.error = "请填写完整的班级信息";
    return;
  }
  const className = buildClassName(newClassYear, newClassMajor, newClassNo, "");
  if (!className) {
    editModal.error = "班级信息不完整";
    return;
  }
  if (editModal.form.assignedClasses.includes(className)) {
    editModal.error = "该班级已在列表中";
    return;
  }
  editModal.form.assignedClasses.push(className);
  editModal.form.newClassYear = 2024;
  editModal.form.newClassMajor = "";
  editModal.form.newClassNo = 1;
  editModal.error = "";
}

function removeTeacherAssignedClass(className) {
  editModal.form.assignedClasses = editModal.form.assignedClasses.filter(c => c !== className);
}

async function handleUpdateUser() {
  editModal.saving = true;
  editModal.error = "";
  try {
    const data = {};
    if (editModal.form.username && editModal.form.username !== editModal.user.username) {
      data.username = editModal.form.username;
    }
    if (editModal.form.password) {
      data.password = editModal.form.password;
    }
    if (editModal.form.role !== editModal.user.role) {
      data.role = editModal.form.role;
    }
    if (editModal.form.remark !== (editModal.user.remark || "")) {
      data.remark = editModal.form.remark;
    }
    const currentRole = editModal.form.role || editModal.user.role;
    if (currentRole === "TEACHER" || currentRole === "ADMIN") {
      data.assignedClasses = editModal.form.assignedClasses.join(",");
    }
    if (Object.keys(data).length > 0) {
      const res = await updateUser(editModal.user.id, data);
      if (res.data.success === false) {
        editModal.error = res.data.message || "更新失败";
        editModal.saving = false;
        return;
      }
    }
    await loadUsers(userCurrentPage.value);
    closeEditModal();
    success("用户信息已更新");
  } catch (e) {
    editModal.error = e?.response?.data?.message || "更新失败";
  } finally {
    editModal.saving = false;
  }
}

async function handleDeleteUser(user) {
  if (!confirm(`确定要删除用户「${user.displayName}」吗？此操作不可恢复。`)) {
    return;
  }
  try {
    await deleteUser(user.id);
    await loadUsers();
    success("用户已删除");
  } catch (e) {
    error(e?.response?.data?.message || "删除失败");
  }
}

onMounted(() => {
  loadPage();
  loadUsers();
});

// Reload from page 1 when filters change
watch([userSearch, userRoleFilter], () => {
  if (activeSection.value === "users") {
    userCurrentPage.value = 1;
    loadUsers(1);
  }
});
</script>

<template>
  <main class="admin-shell">
    <header class="admin-header">
      <h1 class="admin-title">后台管理</h1>
    </header>

    <!-- Category Tabs -->
    <nav class="admin-tabs" role="tablist" aria-label="管理功能分类">
      <button
        v-for="tab in [
          { key: 'upload', label: '上传限制', icon: 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z' },
          { key: 'review', label: '审核策略', icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z' },
          { key: 'users', label: '用户管理', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
          { key: 'backup', label: '备份与恢复', icon: 'M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4' },
          { key: 'other', label: '其他设置', icon: 'M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4' },
        ]"
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

    <!-- Section Content -->
    <Transition name="section-fade" mode="out-in">
      <div :key="activeSection" class="admin-content">

        <!-- Upload Section -->
        <div v-if="activeSection === 'upload'" class="admin-panel-grid">
          <!-- Form Card -->
          <div class="card admin-card">
            <div class="card-header">
              <div class="card-header-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">系统设置</div>
                <h2 class="card-title">成就页面上传限制</h2>
              </div>
            </div>
            <div class="card-body">

              <!-- Image Block -->
              <div class="setting-group">
                <div class="setting-group-label">
                  <span class="group-index">01</span>
                  <span class="group-title">图片设置</span>
                </div>
                <div class="field-row">
                  <div class="field-cell">
                    <label class="field-label" for="img-count">最多上传图片数</label>
                    <div class="input-wrap">
                      <input
                        id="img-count"
                        v-model.number="form.imageMaxCount"
                        class="text-input"
                        type="number"
                        min="1"
                        max="9"
                        aria-label="最多上传图片数"
                      />
                      <span class="input-unit">张</span>
                    </div>
                  </div>
                  <div class="field-cell">
                    <label class="field-label" for="img-size">单张图片最大大小</label>
                    <div class="input-wrap">
                      <input
                        id="img-size"
                        v-model.number="form.imageMaxSizeMb"
                        class="text-input"
                        type="number"
                        min="1"
                        max="200"
                        aria-label="单张图片最大大小 MB"
                      />
                      <span class="input-unit">MB</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Attachment Block -->
              <div class="setting-group">
                <div class="setting-group-label">
                  <span class="group-index">02</span>
                  <span class="group-title">附件设置</span>
                </div>
                <div class="field-row">
                  <div class="field-cell">
                    <label class="field-label" for="att-count">最多上传附件数</label>
                    <div class="input-wrap">
                      <input
                        id="att-count"
                        v-model.number="form.attachmentMaxCount"
                        class="text-input"
                        type="number"
                        min="1"
                        max="20"
                        aria-label="最多上传附件数"
                      />
                      <span class="input-unit">个</span>
                    </div>
                  </div>
                  <div class="field-cell">
                    <label class="field-label" for="att-size">单个附件最大大小</label>
                    <div class="input-wrap">
                      <input
                        id="att-size"
                        v-model.number="form.attachmentMaxSizeMb"
                        class="text-input"
                        type="number"
                        min="1"
                        max="200"
                        aria-label="单个附件最大大小 MB"
                      />
                      <span class="input-unit">MB</span>
                    </div>
                  </div>
                </div>

                <div class="ext-section">
                  <div class="ext-label-row">
                    <label class="field-label">支持的附件后缀</label>
                    <span class="field-hint">多个后缀用英文逗号隔开；留空则该类型不开放</span>
                  </div>
                  <div class="ext-grid">
                    <label
                      v-for="item in ATTACHMENT_TYPE_OPTIONS"
                      :key="item.key"
                      class="ext-card"
                    >
                      <div class="ext-card-head">
                        <img class="ext-icon" :src="item.icon" alt="" aria-hidden="true" />
                        <span class="ext-title">{{ item.label }}</span>
                      </div>
                      <input
                        v-model="form[extFieldKey(item.key)]"
                        class="ext-input"
                        type="text"
                        :aria-label="`${item.label}后缀`"
                        :placeholder="item.key === 'document'
                          ? 'docx, doc, pdf'
                          : item.key === 'video'
                            ? 'mp4, mov'
                            : item.key === 'image'
                              ? 'jpeg, jpg, png'
                              : 'zip, rar, 7z'"
                      />
                    </label>
                  </div>
                </div>
              </div>

              <!-- Supporting Docs Block -->
              <div class="setting-group">
                <div class="setting-group-label">
                  <span class="group-index">03</span>
                  <span class="group-title">证明资料</span>
                </div>
                <div class="field-row">
                  <div class="field-cell">
                    <label class="field-label" for="sd-count">最多上传数量</label>
                    <div class="input-wrap">
                      <input
                        id="sd-count"
                        v-model.number="form.supportingDocMaxCount"
                        class="text-input"
                        type="number"
                        min="1"
                        max="20"
                        aria-label="证明资料最多上传数量"
                      />
                      <span class="input-unit">个</span>
                    </div>
                  </div>
                  <div class="field-cell">
                    <label class="field-label" for="sd-size">单个文件最大大小</label>
                    <div class="input-wrap">
                      <input
                        id="sd-size"
                        v-model.number="form.supportingDocMaxSizeMb"
                        class="text-input"
                        type="number"
                        min="1"
                        max="200"
                        aria-label="证明资料单个文件最大大小 MB"
                      />
                      <span class="input-unit">MB</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Feedback -->
              <Transition name="msg-fade">
                <div v-if="activeErrorMessage" class="msg-banner error" role="alert">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  {{ activeErrorMessage }}
                </div>
                <div v-else-if="saveMessage" class="msg-banner success" role="status">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ saveMessage }}
                </div>
              </Transition>

              <!-- Actions -->
              <div class="card-actions">
                <button
                  class="btn btn-ghost"
                  type="button"
                  @click="syncFormFromSettings"
                >
                  重置
                </button>
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="activeSaving"
                  @click="handleSubmit"
                >
                  <svg v-if="activeSaving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  {{ activeSaving ? "保存中…" : "保存设置" }}
                </button>
              </div>
            </div>
          </div>

          <!-- Preview Card -->
          <div class="card admin-card preview-card">
            <div class="card-header">
              <div class="card-header-icon preview-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">实时预览</div>
                <h2 class="card-title">用户侧展示效果</h2>
              </div>
            </div>
            <div class="card-body preview-shell">
              <!-- Image Preview -->
              <div class="preview-box" aria-hidden="true">
                <div class="preview-box-header">
                  <span class="preview-box-title">图片</span>
                  <span class="preview-box-sub">{{ imageSubtitle }}</span>
                </div>
                <div class="preview-empty">
                  <div class="preview-add-btn">+</div>
                  <span class="preview-empty-text">点击添加图片</span>
                </div>
              </div>
              <!-- Attachment Preview -->
              <div class="preview-box" aria-hidden="true">
                <div class="preview-box-header">
                  <span class="preview-box-title">附件</span>
                  <span class="preview-box-sub">{{ attachmentSubtitle }}</span>
                </div>
                <div class="format-list">
                  <div
                    v-for="item in enabledPreviewTypes"
                    :key="item.key"
                    class="format-chip"
                  >
                    <img class="format-chip-icon" :src="item.icon" alt="" aria-hidden="true" />
                    <span class="format-chip-label">{{ item.label }}</span>
                    <span class="format-chip-exts">{{ item.exts.slice(0, 3).join("/") }}</span>
                  </div>
                  <div v-if="!enabledPreviewTypes.length" class="preview-empty-text">
                    暂无可用附件类型
                  </div>
                </div>
                <div class="preview-tip">
                  {{ attachmentTypeSummary || "暂无可用附件类型" }} · 单个不超过 {{ form.attachmentMaxSizeMb }}MB
                </div>
              </div>
              <!-- Supporting Docs Preview -->
              <div class="preview-box" aria-hidden="true">
                <div class="preview-box-header">
                  <span class="preview-box-title">证明资料</span>
                  <span class="preview-box-sub">{{ supportingDocSubtitle }}</span>
                </div>
                <div class="preview-empty">
                  <div class="preview-add-btn">+</div>
                  <span class="preview-empty-text">学生在审核通知中上传证明资料</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Review Section -->
        <div v-else-if="activeSection === 'review'" class="admin-panel-single">
          <div class="card admin-card">
            <div class="card-header">
              <div class="card-header-icon shield-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">审核入口</div>
                <h2 class="card-title">审核策略设置</h2>
              </div>
            </div>
            <div class="card-body toggle-list">

              <!-- Profile Review -->
              <div class="toggle-section">
                <div class="toggle-section-label">
                  <span class="group-index">01</span>
                  <span class="group-title">个人信息</span>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">开启个人信息审核</span>
                    <span class="toggle-hint">关闭后，学生提交个人信息会直接更新</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`开启个人信息审核: ${reviewForm.profileReviewEnabled ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.profileReviewEnabled"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.profileReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">默认通过</span>
                    <span class="toggle-hint">开启审核但自动通过，保留审核流程入口与记录</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`默认通过: ${reviewForm.profileReviewAutoApprove ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.profileReviewAutoApprove"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.profileReviewAutoApprove"
                      :disabled="!reviewForm.profileReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
              </div>

              <!-- Achievement Review -->
              <div class="toggle-section">
                <div class="toggle-section-label">
                  <span class="group-index">02</span>
                  <span class="group-title">成就</span>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">开启成就审核</span>
                    <span class="toggle-hint">关闭后，新增和修改成就都会直接生效</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`开启成就审核: ${reviewForm.achievementReviewEnabled ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.achievementReviewEnabled"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.achievementReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">默认通过</span>
                    <span class="toggle-hint">开启审核但自动通过，适合先保留入口再平滑切换</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`成就默认通过: ${reviewForm.achievementReviewAutoApprove ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.achievementReviewAutoApprove"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.achievementReviewAutoApprove"
                      :disabled="!reviewForm.achievementReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
              </div>

              <!-- Feedback -->
              <Transition name="msg-fade">
                <div v-if="activeErrorMessage" class="msg-banner error" role="alert">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  {{ activeErrorMessage }}
                </div>
                <div v-else-if="saveMessage" class="msg-banner success" role="status">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ saveMessage }}
                </div>
              </Transition>

              <!-- Actions -->
              <div class="card-actions">
                <button
                  class="btn btn-ghost"
                  type="button"
                  @click="syncReviewFormFromSettings"
                >
                  重置
                </button>
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="activeSaving"
                  @click="handleReviewSubmit"
                >
                  <svg v-if="activeSaving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  {{ activeSaving ? "保存中…" : "保存设置" }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Users Section -->
        <div v-else-if="activeSection === 'users'" class="admin-panel-single">
          <div class="card admin-card users-card">
            <!-- Card Header -->
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
                @click="openAddUserModal"
              >
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                </svg>
                添加用户
              </button>
            </div>

            <!-- Toolbar -->
            <div class="users-toolbar">
              <div class="search-wrap">
                <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <input
                  v-model="userSearch"
                  class="search-input"
                  type="text"
                  placeholder="搜索用户名、姓名、学号…"
                  aria-label="搜索用户"
                />
              </div>
              <select v-model="userRoleFilter" class="filter-select" aria-label="按角色筛选">
                <option value="">全部角色</option>
                <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>

            <!-- Selection Actions -->
            <Transition name="msg-fade">
              <div v-show="someSelected" class="users-selection-bar">
                <span class="selection-count">已选择 {{ selectedUserIds.size }} 个用户</span>
                <button class="btn btn-ghost" type="button" @click="selectAllPage">
                  本页
                </button>
                <button class="btn btn-ghost" type="button" @click="selectAllFiltered">
                  全部
                </button>
                <button class="btn btn-ghost" type="button" @click="selectedUserIds = new Set()">
                  取消
                </button>
                <button class="btn btn-danger-ghost" type="button" @click="handleDeleteSelectedUsers">
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  删除
                </button>
              </div>
            </Transition>

            <!-- Table or States -->
            <div class="users-content">
              <div v-if="usersLoading" class="users-center-state">
                <svg class="loading-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                </svg>
                <span>加载中…</span>
              </div>
              <div v-else-if="usersError" class="users-center-state">
                <svg class="state-icon error" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
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
                          :checked="allFilteredSelected"
                          :indeterminate="someSelected && !allFilteredSelected"
                          aria-label="全选"
                          @change="selectAllPage"
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
                          @change="toggleUserSelect(user.id)"
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
                        <button class="icon-btn" @click.stop="openEditModal(user)" aria-label="编辑用户">
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
              v-model:current-page="userCurrentPage"
              :total-pages="userPages"
              mode="simple"
              @update:current-page="loadUsers"
            />
          </div>
        </div>

        <!-- Backup & Restore Section -->
        <div v-else-if="activeSection === 'backup'" class="backup-panel">
          <!-- Backup Card -->
          <div class="admin-card backup-card">
            <div class="card-header">
              <div class="card-header-icon backup-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">数据安全</div>
                <h2 class="card-title">备份</h2>
              </div>
            </div>
            <div class="card-body backup-card-body">
              <p class="backup-desc">导出 SQL 文件，包含用户、成就、审核策略等全部业务数据。</p>
              <p class="backup-desc">导出 ZIP 压缩包，包含学生荣誉中上传的所有附件文件。</p>
              <div class="backup-two-btns">
                <button
                  class="btn btn-primary backup-btn"
                  :disabled="backupLoading"
                  type="button"
                  @click="handleBackupDb"
                >
                  <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  {{ backupLoading ? "生成中…" : "导出 SQL 文件" }}
                </button>
                <button
                  class="btn btn-primary backup-btn"
                  :disabled="backupLoading"
                  type="button"
                  @click="handleBackupZip"
                >
                  <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  {{ backupLoading ? "打包中…" : "导出 ZIP 文件" }}
                </button>
              </div>
            </div>
          </div>

          <!-- Restore Card -->
          <div class="admin-card restore-card">
            <div class="card-header">
              <div class="card-header-icon restore-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">数据安全</div>
                <h2 class="card-title">恢复</h2>
              </div>
            </div>
            <div class="card-body restore-card-body">
              <div class="restore-warning">
                <svg class="warning-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <span>数据库恢复为全覆盖，会覆盖现有数据；附件恢复为增量补充，缺失文件补回，多余文件保留。</span>
              </div>
              <div class="restore-two-items">
                <!-- Restore DB -->
                <div class="restore-item">
                  <div class="restore-item-label">恢复数据库</div>
                  <div class="restore-item-row">
                    <div class="file-input-wrap">
                      <input
                        id="sql-file"
                        type="file"
                        accept=".sql"
                        class="file-input"
                        aria-label="选择 SQL 备份文件"
                        @change="onSqlFileChange"
                      />
                      <label for="sql-file" class="file-label">
                        <svg class="file-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <span class="file-label-text">{{ backupForm.sqlFile ? backupForm.sqlFile.name : "选择 .sql 文件" }}</span>
                      </label>
                    </div>
                    <button
                      class="btn btn-danger restore-btn"
                      :disabled="restoreLoading || !backupForm.sqlFile"
                      type="button"
                      @click="handleRestore"
                    >
                      <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                        <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                      </svg>
                      {{ restoreLoading ? "恢复中…" : "恢复" }}
                    </button>
                  </div>
                </div>

                <!-- Restore Attachments -->
                <div class="restore-item">
                  <div class="restore-item-label">恢复附件</div>
                  <div class="restore-item-row">
                    <div class="file-input-wrap">
                      <input
                        id="zip-file"
                        type="file"
                        accept=".zip"
                        class="file-input"
                        aria-label="选择附件压缩包"
                        @change="onZipFileChange"
                      />
                      <label for="zip-file" class="file-label">
                        <svg class="file-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                        </svg>
                        <span class="file-label-text">{{ backupForm.zipFile ? backupForm.zipFile.name : "选择 zip 文件" }}</span>
                      </label>
                    </div>
                    <button
                      class="btn btn-danger restore-btn"
                      :disabled="restoreLoading || !backupForm.zipFile"
                      type="button"
                      @click="handleRestoreAttachments"
                    >
                      <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                        <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                      </svg>
                      {{ restoreLoading ? "恢复中…" : "恢复" }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Other Section -->
        <div v-else-if="activeSection === 'other'" class="admin-panel-single">
          <div class="card admin-card">
            <div class="card-header">
              <div class="card-header-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">系统</div>
                <h2 class="card-title">其他设置</h2>
              </div>
            </div>
            <div class="card-body">
              <div class="toggle-section">
                <div class="toggle-row">
                  <div class="toggle-copy">
                    <span class="toggle-title">开放注册</span>
                    <span class="toggle-hint">关闭后，用户将无法自行注册账号</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`开放注册: ${systemSettings.allowRegistration ? '已开启' : '已关闭'}`">
                    <input
                      v-model="systemSettings.allowRegistration"
                      type="checkbox"
                      role="switch"
                      :aria-checked="systemSettings.allowRegistration"
                      @change="handleSaveSystemSettings"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
                <div class="toggle-row">
                  <div class="toggle-copy">
                    <span class="toggle-title">待处理自动滞后</span>
                    <span class="toggle-hint">超过指定天数未处理的请求自动移入已滞后标签</span>
                  </div>
                  <div class="input-wrap" style="width: 80px;">
                    <input
                      v-model.number="systemSettings.delayedThresholdDays"
                      class="text-input"
                      type="number"
                      min="1"
                      max="30"
                      aria-label="待处理自动滞后天数"
                      @change="handleSaveSystemSettings"
                    />
                    <span class="input-unit">天</span>
                  </div>
                </div>
              </div>
              <Transition name="msg-fade">
                <div v-if="systemSettingsMsg" :class="['msg-banner', 'success']" role="status">{{ systemSettingsMsg }}</div>
              </Transition>
            </div>
          </div>
        </div>

      </div>
    </Transition>

    <!-- Edit User Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: editModal.visible }]" @click.self="closeEditModal" role="dialog" aria-modal="true" aria-label="编辑用户">
        <div class="sheet-modal user-edit-modal" @click.stop>
          <div class="modal-top-bar">
            <div class="modal-title-group">
              <h3 class="modal-title">编辑用户</h3>
              <p class="modal-subtitle">正在编辑：{{ editModal.user?.displayName }}</p>
            </div>
            <button class="modal-close-btn" type="button" aria-label="关闭" @click="closeEditModal">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div class="modal-field">
              <label class="modal-label" for="edit-username">用户名</label>
              <input
                id="edit-username"
                v-model="editModal.form.username"
                class="modal-input"
                type="text"
                placeholder="留空则不修改"
              />
            </div>
            <div class="modal-field">
              <label class="modal-label" for="edit-password">密码</label>
              <input
                id="edit-password"
                v-model="editModal.form.password"
                class="modal-input"
                type="password"
                placeholder="留空则不修改"
              />
            </div>
            <div class="modal-field">
              <label class="modal-label" for="edit-role">角色</label>
              <select id="edit-role" v-model="editModal.form.role" class="modal-select">
                <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
            <div class="modal-field">
              <label class="modal-label" for="edit-remark">备注</label>
              <input
                id="edit-remark"
                v-model="editModal.form.remark"
                class="modal-input"
                type="text"
                placeholder="如：班主任、班长、团支书等"
              />
            </div>
            <Transition name="msg-fade">
              <div v-if="editModal.form.role === 'TEACHER'" class="modal-field">
                <label class="modal-label">负责班级</label>
                <div class="class-select-hint">添加该教师负责的班级</div>
                <!-- Assigned classes list -->
                <div v-if="editModal.form.assignedClasses.length > 0" class="assigned-classes-list">
                  <div v-for="cls in editModal.form.assignedClasses" :key="cls" class="assigned-class-item">
                    <span>{{ cls }}</span>
                    <button type="button" class="remove-class-btn" @click="removeTeacherAssignedClass(cls)">×</button>
                  </div>
                </div>
                <!-- Add new class form -->
                <div class="add-class-form">
                  <input
                    v-model.number="editModal.form.newClassYear"
                    class="modal-input class-year-input"
                    type="number"
                    min="2000"
                    max="2100"
                    placeholder="年级"
                  />
                  <select v-model="editModal.form.newClassCategory" class="modal-select class-category-select">
                    <option v-for="cat in STUDENT_CATEGORY_OPTIONS" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                  <select v-model="editModal.form.newClassMajor" class="modal-select class-major-select">
                    <option value="">选择专业</option>
                    <option v-for="major in currentMajorOptions" :key="major" :value="major">{{ major }}</option>
                  </select>
                  <input
                    v-model.number="editModal.form.newClassNo"
                    class="modal-input class-no-input"
                    type="number"
                    min="1"
                    max="20"
                    placeholder="班号"
                  />
                  <button type="button" class="add-class-btn" @click="addTeacherAssignedClass">添加</button>
                </div>
              </div>
            </Transition>
            <Transition name="msg-fade">
              <div v-if="editModal.error" class="msg-banner error modal-error" role="alert">{{ editModal.error }}</div>
            </Transition>
          </div>

          <div class="modal-footer">
            <button class="btn btn-danger-ghost" type="button" @click="handleDeleteUser(editModal.user)">删除用户</button>
            <div class="modal-footer-btns">
              <button class="btn btn-ghost" type="button" @click="closeEditModal">取消</button>
              <button class="btn btn-primary" type="button" :disabled="editModal.saving" @click="handleUpdateUser">
                {{ editModal.saving ? "保存中…" : "保存" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Add User Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: addUserModal.visible }]" @click.self="closeAddUserModal" role="dialog" aria-modal="true" aria-label="添加用户">
        <div class="add-user-layout" @click.stop>
          <div class="sheet-modal user-edit-modal add-user-modal">
            <div class="modal-top-bar">
              <div class="modal-title-group">
                <h3 class="modal-title">添加用户</h3>
                <p class="modal-subtitle">默认角色为学生</p>
              </div>
              <button class="modal-close-btn" type="button" aria-label="关闭" @click="closeAddUserModal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <div class="modal-body">
              <div class="modal-field">
                <label class="modal-label" for="add-users-textarea">用户信息</label>
                <textarea
                  id="add-users-textarea"
                  v-model="addUserModal.textarea"
                  class="modal-textarea"
                  rows="8"
                  placeholder="每行一个用户：显示名称,学号(用于登录),密码&#10;示例：&#10;张三,2024001,password123&#10;李四,2024002,password456"
                ></textarea>
                <div class="field-hint" style="margin-top: 8px;">
                  <strong>注意：使用英文逗号，前后不要有空格</strong>
                </div>
                <div class="modal-field" style="margin-top: 12px;">
                  <input
                    id="import-file"
                    ref="importFileRef"
                    type="file"
                    accept=".xlsx,.xls,.csv,.txt"
                    class="file-input"
                    style="display: none;"
                    @change="handleImportFile"
                  />
                  <button
                    class="btn btn-ghost"
                    type="button"
                    @click="importFileRef.click()"
                  >
                    <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    </svg>
                    从文件导入
                  </button>
                  <span class="field-hint" style="margin-left: 8px;">支持格式：xlsx、csv、txt</span>
                </div>
              </div>
              <Transition name="msg-fade">
                <div v-if="addUserModal.error" class="msg-banner error modal-error" role="alert">{{ addUserModal.error }}</div>
              </Transition>
            </div>

            <div class="modal-footer">
              <div class="modal-footer-btns" style="margin-left: auto;">
                <button class="btn btn-ghost" type="button" @click="closeAddUserModal">取消</button>
                <button class="btn btn-primary" type="button" :disabled="addUserModal.saving" @click="handleCreateUser">
                  {{ addUserModal.saving ? "创建中…" : "创建用户" }}
                </button>
              </div>
            </div>
          </div>

          <div class="add-user-example-panel">
            <div class="example-image-item">
              <span class="example-image-label">xlsx 格式示例</span>
              <img
                src="https://minimax-algeng-chat-tts.oss-cn-wulanchabu.aliyuncs.com/ccv2%2F2026-04-26%2FMiniMax-M2.7%2F2035402385077047958%2F19dc31f5b60a75cfc31c26d9326c4ae3e983b11abf35e53eec1b66827cb9f233..png?Expires=1777278678&OSSAccessKeyId=LTAI5tGLnRTkBjLuYPjNcKQ8&Signature=s73CeKoLgCEsSamacaRtMvhFGa0%3D"
                alt="xlsx 格式示例"
              />
            </div>
            <div class="example-image-item">
              <span class="example-image-label">csv/txt 格式示例</span>
              <img
                src="https://minimax-algeng-chat-tts.oss-cn-wulanchabu.aliyuncs.com/ccv2%2F2026-04-26%2FMiniMax-M2.7%2F2035402385077047958%2F8eed82b4e7e942cbb4a0f56e3ba15ac98e97fca84dcba98f7cbb41bc6a448ccd..png?Expires=1777278679&OSSAccessKeyId=LTAI5tGLnRTkBjLuYPjNcKQ8&Signature=YYfccG4eUdYEQyiFclPCu7VrXCQ%3D"
                alt="csv/txt 格式示例"
              />
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
@import '../assets/styles/admin-view.css';

.add-user-layout {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 40px;
  align-items: flex-start;
  z-index: 1011;
}

.add-user-modal {
  position: relative !important;
  top: auto !important;
  left: auto !important;
  transform: none !important;
  width: 480px;
}

.add-user-example-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 280px;
  padding-top: 4px;
}

.example-image-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.example-image-label {
  font-size: 12px;
  color: var(--text-sub);
  font-weight: 600;
}

.example-image-item img {
  width: 100%;
  border-radius: 8px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
}

.class-select-hint {
  font-size: 12px;
  color: var(--text-sub);
  margin-bottom: 8px;
}

.assigned-classes-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.assigned-class-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: var(--bg-secondary);
  border-radius: 4px;
  border: 1px solid var(--line);
  font-size: 13px;
}

.remove-class-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-sub);
  padding: 0 2px;
  line-height: 1;
}

.remove-class-btn:hover {
  color: var(--danger);
}

.add-class-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.class-year-input {
  width: 90px;
}

.class-category-select {
  width: 100px;
}

.class-major-select {
  flex: 1;
  min-width: 160px;
}

.class-no-input {
  width: 70px;
}

.add-class-btn {
  padding: 6px 12px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.add-class-btn:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .add-user-layout {
    width: calc(100vw - 32px);
    max-width: 480px;
  }
  .add-user-example-panel {
    display: none;
  }
  .add-user-modal {
    width: 100% !important;
  }
}
</style>
