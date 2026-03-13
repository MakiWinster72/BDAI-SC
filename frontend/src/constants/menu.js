export const MENU_ITEMS = [
  { key: "campus", label: "校园生活" },
  { key: "good-news", label: "喜报🎉" },
  { key: "records", label: "记录" },
  { key: "achievements", label: "个人成就" },
  { key: "my-info", label: "我的信息" },
  { key: "contacts", label: "教师/部门联系方式" },
  { key: "student-info", label: "学生信息" },
  { key: "admin", label: "后台管理" },
];

const ENABLED_MENU_KEYS = new Set([
  "campus",
  "good-news",
  "records",
  "achievements",
  "my-info",
  "student-info",
  "contacts",
]);

const ROLE_MENU_VISIBILITY = {
  "student-info": new Set(["TEACHER", "ADMIN"]),
};

export function isMenuEnabled(key) {
  return ENABLED_MENU_KEYS.has(key);
}

export function filterMenuItemsByRole(role) {
  const normalizedRole = role || "STUDENT";
  return MENU_ITEMS.filter((item) => {
    const allowedRoles = ROLE_MENU_VISIBILITY[item.key];
    if (!allowedRoles) {
      return true;
    }
    return allowedRoles.has(normalizedRole);
  });
}
