export const ADMIN_TABS = [
  {
    key: "upload",
    label: "上传限制",
    shortLabel: "媒体",
    icon: "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z",
  },
  {
    key: "review",
    label: "审核策略",
    shortLabel: "审核",
    icon: "M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z",
  },
  {
    key: "users",
    label: "用户管理",
    shortLabel: "用户",
    icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z",
  },
  {
    key: "backup",
    label: "备份与恢复",
    shortLabel: "数据",
    icon: "M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4",
  },
  {
    key: "other",
    label: "其他设置",
    shortLabel: "其他",
    icon: "M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4",
  },
];

export const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

export const ROLE_LABELS = {
  ADMIN: "管理员",
  TEACHER: "教师",
  STUDENT: "学生",
  CADRE: "班干部",
};

export const ROLE_OPTIONS = [
  { value: "STUDENT", label: "学生" },
  { value: "CADRE", label: "班干部" },
  { value: "TEACHER", label: "教师" },
  { value: "ADMIN", label: "管理员" },
];

export const STUDENT_CATEGORY_OPTIONS = ["本科生", "研究生"];

export function getAdminRoleLabel(role) {
  return ROLE_LABELS[role] || role;
}
