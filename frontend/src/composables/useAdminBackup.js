import { reactive, shallowRef } from "vue";
import {
  deleteUserStorage,
  downloadBackupAttachments,
  downloadBackupDb,
  getStorageAnalysis,
  restoreBackupAttachments,
  restoreBackupDb,
} from "@/api/admin";
import { useToast } from "@/composables/useToast";

export function useAdminBackup() {
  const { success, error } = useToast();

  const backupForm = reactive({
    sqlFile: null,
    zipFile: null,
  });
  const backupLoading = shallowRef(false);
  const restoreLoading = shallowRef(false);
  const storageData = shallowRef(null);
  const storageLoading = shallowRef(false);
  const storageError = shallowRef("");
  const storageDeleting = shallowRef(new Set());

  function storageLabel(item) {
    if (item.userExists) {
      return item.displayName || item.username;
    }
    return `(已删除) #${item.folderName}`;
  }

  function barWidth(size, max) {
    if (!max || max === 0) {
      return "0%";
    }
    return `${Math.max(1, (size / max) * 100)}%`;
  }

  async function fetchStorageAnalysis() {
    storageLoading.value = true;
    storageError.value = "";
    await getStorageAnalysis()
      .then((res) => {
        storageData.value = res.data;
      })
      .catch(() => {
        storageError.value = "存储分析加载失败";
      })
      .finally(() => {
        storageLoading.value = false;
      });
  }

  async function handleDeleteStorage(item) {
    const label = storageLabel(item);
    if (
      !confirm(
        `确定要删除「${label}」的全部附件文件吗？\n\n此操作不可恢复，将删除磁盘上的 ${item.sizeFormatted} 文件。`,
      )
    ) {
      return;
    }
    storageDeleting.value = new Set([...storageDeleting.value, item.userId]);
    await deleteUserStorage(item.userId)
      .then(async () => {
        success(`已删除「${label}」的附件`);
        await fetchStorageAnalysis();
      })
      .catch(() => {
        error("删除失败");
      })
      .finally(() => {
        const next = new Set(storageDeleting.value);
        next.delete(item.userId);
        storageDeleting.value = next;
      });
  }

  async function handleBackupDb() {
    backupLoading.value = true;
    const res = await downloadBackupDb();
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      error(errData.message || "备份失败");
      backupLoading.value = false;
      return;
    }
    const blob = await res.blob();
    const filename =
      res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] ||
      "bdai_sc_backup.sql";
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
    success("SQL 文件已下载");
    backupLoading.value = false;
  }

  async function handleBackupZip() {
    backupLoading.value = true;
    const res = await downloadBackupAttachments();
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      error(errData.message || "打包失败");
      backupLoading.value = false;
      return;
    }
    const blob = await res.blob();
    const filename =
      res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] ||
      "bdai_sc_attachments.zip";
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
    success("ZIP 文件已下载");
    backupLoading.value = false;
  }

  async function handleRestore() {
    if (!backupForm.sqlFile) {
      error("请先选择 SQL 备份文件");
      return;
    }
    restoreLoading.value = true;
    const res = await restoreBackupDb(backupForm.sqlFile);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      error(data.message || "恢复失败");
      restoreLoading.value = false;
      return;
    }
    success("数据库恢复成功");
    backupForm.sqlFile = null;
    const input = document.getElementById("sql-file");
    if (input) {
      input.value = "";
    }
    restoreLoading.value = false;
  }

  async function handleRestoreAttachments() {
    if (!backupForm.zipFile) {
      error("请先选择附件压缩包");
      return;
    }
    restoreLoading.value = true;
    const res = await restoreBackupAttachments(backupForm.zipFile);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      error(data.message || "恢复失败");
      restoreLoading.value = false;
      return;
    }
    success("附件恢复成功");
    backupForm.zipFile = null;
    const input = document.getElementById("zip-file");
    if (input) {
      input.value = "";
    }
    restoreLoading.value = false;
  }

  function onSqlFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.name.endsWith(".sql")) {
      error("请选择 .sql 格式的备份文件");
      backupForm.sqlFile = null;
      return;
    }
    backupForm.sqlFile = file;
  }

  function onZipFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.name.toLowerCase().endsWith(".zip")) {
      error("请选择 .zip 格式的压缩文件");
      backupForm.zipFile = null;
      return;
    }
    backupForm.zipFile = file;
  }

  return {
    backupForm,
    backupLoading,
    restoreLoading,
    storageData,
    storageLoading,
    storageError,
    storageDeleting,
    storageLabel,
    barWidth,
    fetchStorageAnalysis,
    handleDeleteStorage,
    handleBackupDb,
    handleBackupZip,
    handleRestore,
    handleRestoreAttachments,
    onSqlFileChange,
    onZipFileChange,
  };
}
