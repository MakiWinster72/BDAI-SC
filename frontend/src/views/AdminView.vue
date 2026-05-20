<script setup>
import { computed, onMounted, reactive, ref, shallowRef, watch } from "vue";
import MobileCapsule from "@/components/MobileCapsule.vue";
import AdminBackupTab from "@/components/admin/AdminBackupTab.vue";
import AdminUploadSettingsTab from "@/components/admin/AdminUploadSettingsTab.vue";
import AdminReviewSettingsTab from "@/components/admin/AdminReviewSettingsTab.vue";
import AdminSystemSettingsTab from "@/components/admin/AdminSystemSettingsTab.vue";
import AdminUsersTab from "@/components/admin/AdminUsersTab.vue";
import { useAchievementUploadSettings } from "@/composables/useAchievementUploadSettings";
import { useReviewSettings } from "@/composables/useReviewSettings";
import { getUserList, updateUser, deleteUser, createUser, getAllUserIds, getSystemSettings, updateSystemSettings, downloadBackupDb, restoreBackupDb, downloadBackupAttachments, restoreBackupAttachments, updateTeacherAssignedClasses, getStorageAnalysis, deleteUserStorage } from "@/api/admin";
import { useToast } from "@/composables/useToast";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { loadUser } from "@/utils/userStorage";
import { buildClassName } from "@/utils/profile";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const activeSection = shallowRef("upload");

const ADMIN_TABS = [
  { key: "upload", label: "上传限制", shortLabel: "媒体", icon: "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" },
  { key: "review", label: "审核策略", shortLabel: "审核", icon: "M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" },
  { key: "users", label: "用户管理", shortLabel: "用户", icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" },
  { key: "backup", label: "备份与恢复", shortLabel: "数据", icon: "M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" },
  { key: "other", label: "其他设置", shortLabel: "其他", icon: "M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" },
];

// Backup & Restore
const backupForm = reactive({
  sqlFile: null,
  zipFile: null,
});
const backupLoading = shallowRef(false);
const restoreLoading = shallowRef(false);

// Storage analysis
const storageData = shallowRef(null);
const storageLoading = shallowRef(false);
const storageError = shallowRef("");

async function fetchStorageAnalysis() {
  storageLoading.value = true;
  storageError.value = "";
  try {
    const res = await getStorageAnalysis();
    storageData.value = res.data;
  } catch (e) {
    storageError.value = "存储分析加载失败";
  } finally {
    storageLoading.value = false;
  }
}

const storageDeleting = shallowRef(new Set());

function storageLabel(item) {
  if (item.userExists) {
    return item.displayName || item.username;
  }
  return "(已删除) #" + item.folderName;
}

async function handleDeleteStorage(item) {
  const label = storageLabel(item);
  if (!confirm("确定要删除「" + label + "」的全部附件文件吗？\n\n此操作不可恢复，将删除磁盘上的 " + item.sizeFormatted + " 文件。")) {
    return;
  }
  storageDeleting.value = new Set([...storageDeleting.value, item.userId]);
  try {
    await deleteUserStorage(item.userId);
    success("已删除「" + label + "」的附件");
    await fetchStorageAnalysis();
  } catch (e) {
    error("删除失败");
  } finally {
    const s = new Set(storageDeleting.value);
    s.delete(item.userId);
    storageDeleting.value = s;
  }
}

function barWidth(size, max) {
  if (!max || max === 0) return "0%";
  return Math.max(1, (size / max) * 100) + "%";
}

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

function stepThreshold(delta) {
  const next = (systemSettings.delayedThresholdDays || 2) + delta;
  if (next >= 1 && next <= 30) {
    systemSettings.delayedThresholdDays = next;
    handleSaveSystemSettings();
  }
}

function updateSystemSetting({ key, value }) {
  systemSettings[key] = value;
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

function toggleSelectAllPage() {
  if (allPageSelected.value) {
    selectedUserIds.value = new Set();
  } else {
    selectAllPage();
  }
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

const activeSaving = computed(() =>
  activeSection.value === "upload" ? uploadSaving.value : reviewSaving.value,
);
const activeErrorMessage = computed(() =>
  activeSection.value === "upload"
    ? uploadErrorMessage.value
    : reviewErrorMessage.value,
);

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

function updateUploadFormField({ key, value }) {
  form[key] = value;
}

function syncReviewFormFromSettings() {
  reviewForm.profileReviewEnabled = reviewSettings.profileReviewEnabled;
  reviewForm.profileReviewAutoApprove = reviewSettings.profileReviewAutoApprove;
  reviewForm.achievementReviewEnabled = reviewSettings.achievementReviewEnabled;
  reviewForm.achievementReviewAutoApprove =
    reviewSettings.achievementReviewAutoApprove;
}

function updateReviewFormField({ key, value }) {
  reviewForm[key] = value;
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

    <!-- Section Content -->
    <Transition name="section-fade" mode="out-in">
      <div :key="activeSection" class="admin-content">

        <AdminUploadSettingsTab
          v-if="activeSection === 'upload'"
          :form="form"
          :attachment-type-options="ATTACHMENT_TYPE_OPTIONS"
          :error-message="uploadErrorMessage"
          :saving="uploadSaving"
          @update-form-field="updateUploadFormField"
          @reset="syncFormFromSettings"
          @save="handleSubmit"
        />

        <AdminReviewSettingsTab
          v-else-if="activeSection === 'review'"
          :review-form="reviewForm"
          :error-message="activeErrorMessage"
          :saving="activeSaving"
          @update-review-field="updateReviewFormField"
          @reset="syncReviewFormFromSettings"
          @save="handleReviewSubmit"
        />

        <AdminUsersTab
          v-else-if="activeSection === 'users'"
          v-model:user-search="userSearch"
          v-model:user-role-filter="userRoleFilter"
          :users="users"
          :users-loading="usersLoading"
          :users-error="usersError"
          :user-total="userTotal"
          :role-options="ROLE_OPTIONS"
          :selected-user-ids="selectedUserIds"
          :some-selected="someSelected"
          :all-page-selected="allPageSelected"
          :user-current-page="userCurrentPage"
          :user-pages="userPages"
          :get-role-label="getRoleLabel"
          @open-add-user="openAddUserModal"
          @select-all-page="selectAllPage"
          @select-all-filtered="selectAllFiltered"
          @clear-selection="selectedUserIds = new Set()"
          @delete-selected="handleDeleteSelectedUsers"
          @toggle-select-all-page="toggleSelectAllPage"
          @toggle-user-select="toggleUserSelect"
          @edit-user="openEditModal"
          @update:user-current-page="loadUsers"
        />

        <AdminBackupTab
          v-else-if="activeSection === 'backup'"
          :backup-form="backupForm"
          :backup-loading="backupLoading"
          :restore-loading="restoreLoading"
          :storage-data="storageData"
          :storage-loading="storageLoading"
          :storage-error="storageError"
          :storage-deleting="storageDeleting"
          :storage-label="storageLabel"
          :bar-width="barWidth"
          @backup-db="handleBackupDb"
          @backup-zip="handleBackupZip"
          @sql-file-change="onSqlFileChange"
          @zip-file-change="onZipFileChange"
          @restore-db="handleRestore"
          @restore-attachments="handleRestoreAttachments"
          @refresh-storage="fetchStorageAnalysis"
          @delete-storage="handleDeleteStorage"
        />

        <AdminSystemSettingsTab
          v-else-if="activeSection === 'other'"
          :settings="systemSettings"
          :message="systemSettingsMsg"
          @update-setting="updateSystemSetting"
          @save="handleSaveSystemSettings"
          @step-threshold="stepThreshold"
        />

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
                src="/assets/images/xlsx格式示例.png"
                alt="xlsx 格式示例"
              />
            </div>
            <div class="example-image-item">
              <span class="example-image-label">csv/txt 格式示例</span>
              <img
                src="/assets/images/csv-txt格式示例.png"
                alt="csv/txt 格式示例"
              />
            </div>
          </div>
        </div>
      </div>
    </Teleport>

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
@import '@/assets/styles/admin-view.css';
</style>

<style scoped>
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
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 16px;
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
  border: 1px solid rgba(100, 12, 114, 0.1);
  box-shadow: 0 4px 16px rgba(100, 12, 114, 0.12);
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

/* ── Admin Capsule Theme Overrides ────────────────────── */
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
