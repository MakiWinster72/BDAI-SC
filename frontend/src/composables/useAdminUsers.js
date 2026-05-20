import { computed, reactive, ref, shallowRef, watch } from "vue";
import {
  createUser,
  deleteUser,
  getAllUserIds,
  getUserList,
  updateUser,
} from "@/api/admin";
import { majorOptionsByCategory } from "@/constants/profileOptions";
import { STUDENT_CATEGORY_OPTIONS } from "@/config/adminConfig";
import { useToast } from "@/composables/useToast";
import { buildClassName } from "@/utils/profile";

export function useAdminUsers(activeSection) {
  const { success, error } = useToast();

  const users = shallowRef([]);
  const usersLoading = shallowRef(false);
  const usersError = shallowRef("");
  const userSearch = shallowRef("");
  const userRoleFilter = shallowRef("");
  const userCurrentPage = shallowRef(1);
  const userPageSize = shallowRef(20);
  const userTotal = shallowRef(0);
  const userPages = computed(() =>
    Math.ceil(userTotal.value / userPageSize.value),
  );
  const selectedUserIds = shallowRef(new Set());
  const allFilteredSelected = shallowRef(false);
  const someSelected = computed(() => selectedUserIds.value.size > 0);
  const allPageSelected = computed(
    () =>
      users.value.length > 0 &&
      selectedUserIds.value.size === users.value.length,
  );

  const editModal = reactive({
    visible: false,
    user: null,
    saving: false,
    error: "",
    form: {
      username: "",
      password: "",
      role: "",
      assignedClasses: [],
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

  const currentMajorOptions = computed(
    () => majorOptionsByCategory[editModal.form.newClassCategory] || [],
  );

  async function loadUsers(page = userCurrentPage.value) {
    usersLoading.value = true;
    usersError.value = "";
    selectedUserIds.value = new Set();
    allFilteredSelected.value = false;
    await getUserList({
      page,
      size: userPageSize.value,
      search: userSearch.value.trim() || undefined,
      role: userRoleFilter.value || undefined,
    })
      .then((res) => {
        const payload = res.data;
        users.value = payload.data || [];
        userTotal.value = payload.total || 0;
        userCurrentPage.value = payload.page || page;
      })
      .catch(() => {
        usersError.value = "加载用户列表失败";
      })
      .finally(() => {
        usersLoading.value = false;
      });
  }

  function toggleUserSelect(id) {
    const next = new Set(selectedUserIds.value);
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    selectedUserIds.value = next;
    allFilteredSelected.value = false;
  }

  function selectAllPage() {
    selectedUserIds.value = new Set(users.value.map((user) => user.id));
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
    await getAllUserIds({
      search: userSearch.value.trim() || undefined,
      role: userRoleFilter.value || undefined,
    })
      .then((res) => {
        selectedUserIds.value = new Set(res.data || []);
        allFilteredSelected.value = true;
      })
      .catch(() => {
        error("获取用户列表失败");
      });
  }

  async function handleDeleteSelectedUsers() {
    if (selectedUserIds.value.size === 0) {
      return;
    }
    if (
      !confirm(
        `确定要删除选中的 ${selectedUserIds.value.size} 个用户吗？此操作不可恢复。`,
      )
    ) {
      return;
    }
    for (const id of selectedUserIds.value) {
      await deleteUser(id);
    }
    selectedUserIds.value = new Set();
    allFilteredSelected.value = false;
    await loadUsers(userCurrentPage.value);
    success("已删除选中的用户");
  }

  function openAddUserModal() {
    addUserModal.textarea = "";
    addUserModal.error = "";
    addUserModal.visible = true;
  }

  function closeAddUserModal() {
    addUserModal.visible = false;
  }

  async function handleCreateUser() {
    const lines = addUserModal.textarea
      .trim()
      .split("\n")
      .filter((line) => line.trim());
    if (lines.length === 0) {
      addUserModal.error = "请输入用户信息";
      return;
    }

    const parsedUsers = [];
    const errors = [];
    const seenUsernames = new Set();

    lines.forEach((line, index) => {
      const parts = line.split(",").map((part) => part.trim());
      if (parts.length < 3) {
        errors.push(
          `第 ${index + 1} 行：${parts[0] || "空"}，${parts[1] || "空"}，字段不足`,
        );
        return;
      }
      const [displayName, username, password] = parts;
      if (seenUsernames.has(username)) {
        errors.push(`第 ${index + 1} 行：${displayName}，${username}，重复`);
        return;
      }
      seenUsernames.add(username);
      parsedUsers.push({ displayName, username, password, line: index + 1 });
    });

    if (errors.length > 0) {
      addUserModal.error =
        errors.slice(0, 5).join("；") +
        (errors.length > 5 ? `…还有 ${errors.length - 5} 条` : "");
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

    for (const user of parsedUsers) {
      await createUser(user)
        .then(() => {
          created.push(user.username);
        })
        .catch((err) => {
          const msg = err?.response?.data?.message || "失败";
          failed.push(`${user.displayName}，${user.username}，${msg}`);
        });
    }

    await loadUsers(1);
    closeAddUserModal();
    if (failed.length > 0) {
      addUserModal.error = `已创建 ${created.length} 个用户，失败 ${failed.length} 个：${failed.slice(0, 3).join("；")}${failed.length > 3 ? "…" : ""}`;
    } else {
      success(`已创建 ${created.length} 个用户`);
    }
    addUserModal.saving = false;
  }

  async function handleImportFile(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    const name = file.name.toLowerCase();
    let rawLines = [];

    if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
      const XLSX = await import("xlsx");
      const arrayBuffer = await file.arrayBuffer();
      const workbook = XLSX.read(arrayBuffer, { type: "array" });
      const sheet = workbook.Sheets[workbook.SheetNames[0]];
      const data = XLSX.utils.sheet_to_json(sheet, { header: 1 });
      rawLines = data.filter((row) => row && row.length >= 3);
    } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
      const text = await file.text();
      rawLines = text.trim().split("\n").filter((line) => line.trim());
    } else {
      addUserModal.error = "不支持的文件格式，请上传 xlsx、csv 或 txt 文件";
      event.target.value = "";
      return;
    }

    const errors = [];
    const seenUsernames = new Set();
    rawLines.forEach((row, index) => {
      const parts = Array.isArray(row)
        ? row.map((part) => String(part).trim())
        : String(row).split(",").map((part) => part.trim());
      const displayName = parts[0] || "空";
      const username = parts[1] || "空";
      if (parts.length < 3) {
        errors.push(`第 ${index + 1} 行：${displayName}，${username}，字段不足`);
        return;
      }
      if (seenUsernames.has(username)) {
        errors.push(`第 ${index + 1} 行：${displayName}，${username}，重复`);
      }
      seenUsernames.add(username);
    });

    const validRows = rawLines.map((row) => {
      const parts = Array.isArray(row)
        ? row.map((part) => String(part).trim())
        : String(row).split(",").map((part) => part.trim());
      return parts.slice(0, 3).join(",");
    });
    addUserModal.textarea = validRows.join("\n");
    addUserModal.error =
      errors.length > 0
        ? errors.slice(0, 5).join("；") +
          (errors.length > 5 ? `…还有 ${errors.length - 5} 条` : "")
        : "";
    event.target.value = "";
  }

  function openEditModal(user) {
    const freshUser = users.value.find((entry) => entry.id === user.id) || user;
    editModal.user = freshUser;
    editModal.form.username = freshUser.username;
    editModal.form.password = "";
    editModal.form.role = freshUser.role;
    editModal.form.remark = freshUser.remark || "";
    editModal.form.assignedClasses = freshUser.assignedClasses
      ? freshUser.assignedClasses.split(",").map((item) => item.trim()).filter(Boolean)
      : [];
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
    editModal.form.assignedClasses = editModal.form.assignedClasses.filter(
      (item) => item !== className,
    );
  }

  async function handleUpdateUser() {
    editModal.saving = true;
    editModal.error = "";
    const data = {};
    if (
      editModal.form.username &&
      editModal.form.username !== editModal.user.username
    ) {
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
    editModal.saving = false;
  }

  async function handleDeleteUser(user) {
    if (!confirm(`确定要删除用户「${user.displayName}」吗？此操作不可恢复。`)) {
      return;
    }
    await deleteUser(user.id)
      .then(async () => {
        await loadUsers();
        success("用户已删除");
        closeEditModal();
      })
      .catch((err) => {
        error(err?.response?.data?.message || "删除失败");
      });
  }

  watch([userSearch, userRoleFilter], () => {
    if (activeSection.value === "users") {
      userCurrentPage.value = 1;
      loadUsers(1);
    }
  });

  return {
    users,
    usersLoading,
    usersError,
    userSearch,
    userRoleFilter,
    userCurrentPage,
    userTotal,
    userPages,
    selectedUserIds,
    someSelected,
    allPageSelected,
    editModal,
    addUserModal,
    importFileRef,
    currentMajorOptions,
    studentCategoryOptions: STUDENT_CATEGORY_OPTIONS,
    loadUsers,
    toggleUserSelect,
    selectAllPage,
    toggleSelectAllPage,
    selectAllFiltered,
    handleDeleteSelectedUsers,
    openAddUserModal,
    closeAddUserModal,
    handleCreateUser,
    handleImportFile,
    openEditModal,
    closeEditModal,
    addTeacherAssignedClass,
    removeTeacherAssignedClass,
    handleUpdateUser,
    handleDeleteUser,
  };
}
