export const MENU_ITEMS = [
  { key: "my-info", label: "我的信息" },
  { key: "achievements", label: "个人成就" },
  { key: "student-info", label: "学生信息" },
  { key: "notifications", label: "通知" },
  { key: "admin", label: "后台管理" },
];

export const MENU_ROUTE_MAP = {
  notifications: { path: "/notifications" },
  achievements: { path: "/achievements", query: { category: "all" } },
  "my-info": { path: "/myinfos" },
  "student-info": { path: "/student-info" },
  admin: { path: "/admin" },
};

const ENABLED_MENU_KEYS = new Set([
  "notifications",
  "achievements",
  "my-info",
  "student-info",
  "admin",
]);

const ROLE_MENU_VISIBILITY = {
  "student-info": new Set(["TEACHER", "ADMIN"]),
  "admin": new Set(["ADMIN"]),
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

export function getMenuLocation(key) {
  return MENU_ROUTE_MAP[key] || MENU_ROUTE_MAP["my-info"];
}

export function getActiveMenuFromRoute(route) {
  const routeName = route?.name;
  if (routeName === "notifications") {
    return "notifications";
  }
  if (routeName === "achievements") {
    return "achievements";
  }
  if (routeName === "student-info") {
    return "student-info";
  }
  if (routeName === "admin") {
    return "admin";
  }
  if (routeName === "settings") {
    return "";
  }
  return "my-info";
}
