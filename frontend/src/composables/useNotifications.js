import { computed, reactive } from "vue";

const STORAGE_KEY = "gcsc_notification_center";
const REVIEWER_ROLES = ["TEACHER", "ADMIN"];
const CATEGORY_LABELS = {
  contest: "学科竞赛、文体艺术",
  paper: "发表学术论文",
  journal: "发表期刊作品",
  patent: "专利(著作权)授权数(项)",
  certificate: "职业资格证书",
  research: "学生参与教师科研项目情况",
  works: "创作、表演的代表性作品",
  doubleHundred: "双百工程",
  ieerTraining: "大学生创新创业训练计划项目",
};

const store = reactive({
  loaded: false,
  reviewRequests: [],
  notifications: [],
});

function ensureLoaded() {
  if (store.loaded || typeof window === "undefined") {
    return;
  }
  store.loaded = true;
  try {
    const raw = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
    store.reviewRequests = Array.isArray(raw.reviewRequests)
      ? raw.reviewRequests
      : [];
    store.notifications = Array.isArray(raw.notifications)
      ? raw.notifications
      : [];
  } catch {
    store.reviewRequests = [];
    store.notifications = [];
  }
}

function persistStore() {
  if (typeof window === "undefined") {
    return;
  }
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      reviewRequests: store.reviewRequests,
      notifications: store.notifications,
    }),
  );
}

function generateId(prefix) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

function toDisplayName(actor) {
  return actor?.displayName || actor?.username || "当前用户";
}

function resolveAchievementCategoryLabel(category) {
  return CATEGORY_LABELS[category] || category || "成就";
}

function formatRelativeTime(timestamp) {
  if (!timestamp) {
    return "";
  }
  const date = new Date(timestamp);
  const diff = Date.now() - date.getTime();
  if (Number.isNaN(diff)) {
    return "";
  }
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  if (diff < minute) {
    return "刚刚";
  }
  if (diff < hour) {
    return `${Math.max(1, Math.floor(diff / minute))}分钟前`;
  }
  if (diff < day) {
    return `${Math.max(1, Math.floor(diff / hour))}小时前`;
  }
  return date.toLocaleString("zh-CN", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function matchesNotificationAudience(notification, user) {
  const usernames = Array.isArray(notification?.usernames)
    ? notification.usernames
    : [];
  const roles = Array.isArray(notification?.roles) ? notification.roles : [];
  return usernames.includes(user.username) || roles.includes(user.role);
}

function isReviewVisibleForUser(request, user) {
  if (!request || !user?.username) {
    return false;
  }
  if (request.requester?.username === user.username) {
    return true;
  }
  return Array.isArray(request.targetRoles) && request.targetRoles.includes(user.role);
}

function buildReviewEntry(request, user) {
  const isOwner = request.requester?.username === user.username;
  const requestTypeLabel = request.resourceType === "profile" ? "信息" : "成就";
  const pendingContent = isOwner
    ? "已提交，等待老师或管理员处理。"
    : `${toDisplayName(request.requester)} 提交了新的审核请求，等待你处理。`;
  const approvedContent = isOwner
    ? "你的请求已通过审核。"
    : `${toDisplayName(request.requester)} 的请求已通过审核。`;
  const rejectedContent = isOwner
    ? "你的请求已被驳回。"
    : `${toDisplayName(request.requester)} 的请求已被驳回。`;
  return {
    id: request.id,
    sourceId: request.id,
    source: "review-request",
    title: request.title,
    content:
      request.status === "approved"
        ? approvedContent
        : request.status === "rejected"
          ? rejectedContent
          : pendingContent,
    reason: request.rejectionReason || "",
    badgeText:
      request.status === "approved"
        ? "已通过"
        : request.status === "rejected"
          ? "已驳回"
          : "待处理",
    badgeClass:
      request.status === "approved"
        ? "is-approved"
        : request.status === "rejected"
          ? "is-rejected"
          : "is-pending",
    meta: isOwner
      ? `${requestTypeLabel}审核`
      : `${requestTypeLabel}请求 · ${toDisplayName(request.requester)}`,
    summary: request.summary || "",
    status: request.status,
    requester: request.requester || null,
    reviewer: request.reviewer || null,
    resourceType: request.resourceType,
    action: request.action,
    payloadSnapshot: request.payloadSnapshot || null,
    timeText: formatRelativeTime(request.updatedAt || request.createdAt),
    createdAt: request.updatedAt || request.createdAt,
  };
}

function buildNotificationEntry(notification) {
  return {
    id: notification.id,
    sourceId: notification.id,
    source: "notification",
    title: notification.title,
    content: notification.content,
    reason: notification.reason || "",
    badgeText: notification.badgeText || "通知",
    badgeClass: notification.badgeClass || "is-system",
    meta: notification.meta || "系统消息",
    timeText: formatRelativeTime(notification.createdAt),
    createdAt: notification.createdAt,
  };
}

function addNotification({
  title,
  content,
  usernames = [],
  roles = [],
  badgeText = "通知",
  badgeClass = "is-system",
  meta = "系统消息",
  reason = "",
}) {
  ensureLoaded();
  store.notifications.unshift({
    id: generateId("notification"),
    title,
    content,
    usernames,
    roles,
    badgeText,
    badgeClass,
    meta,
    reason,
    createdAt: new Date().toISOString(),
  });
  persistStore();
}

function createReviewRequest({
  actor,
  resourceType,
  action,
  title,
  summary,
  payloadSnapshot = null,
  extra = {},
}) {
  ensureLoaded();
  const now = new Date().toISOString();
  const request = {
    id: generateId("review"),
    resourceType,
    action,
    title,
    summary,
    status: "pending",
    requester: {
      username: actor?.username || "",
      displayName: actor?.displayName || "",
      role: actor?.role || "STUDENT",
      studentNo: actor?.studentNo || "",
    },
    targetRoles: [...REVIEWER_ROLES],
    createdAt: now,
    updatedAt: now,
    rejectionReason: "",
    payloadSnapshot,
    ...extra,
  };
  store.reviewRequests.unshift(request);
  persistStore();
  return request;
}

function submitAchievementReviewRequest({
  actor,
  action,
  category,
  title,
  payloadSnapshot = null,
  recordId = null,
}) {
  const categoryLabel = resolveAchievementCategoryLabel(category);
  return createReviewRequest({
    actor,
    resourceType: "achievement",
    action,
    title: action === "update" ? `成就修改待审核` : `成就新增待审核`,
    summary: `${toDisplayName(actor)}${action === "update" ? "修改" : "新增"}了「${title || categoryLabel}」`,
    payloadSnapshot,
    extra: {
      category,
      categoryLabel,
      recordId,
    },
  });
}

function submitProfileReviewRequest({ actor, payloadSnapshot = null }) {
  return createReviewRequest({
    actor,
    resourceType: "profile",
    action: "update",
    title: "个人信息修改待审核",
    summary: `${toDisplayName(actor)} 提交了个人信息修改申请`,
    payloadSnapshot,
  });
}

function updateReviewRequestStatus({ requestId, status, reviewer, reason = "" }) {
  ensureLoaded();
  const request = store.reviewRequests.find((item) => item.id === requestId);
  if (!request) {
    throw new Error("审核请求不存在");
  }
  if (status === "rejected" && !String(reason || "").trim()) {
    throw new Error("驳回时必须填写理由");
  }
  request.status = status;
  request.rejectionReason = status === "rejected" ? String(reason).trim() : "";
  request.reviewer = reviewer
    ? {
        username: reviewer.username || "",
        displayName: reviewer.displayName || "",
        role: reviewer.role || "",
      }
    : null;
  request.updatedAt = new Date().toISOString();
  persistStore();

  if (request.requester?.username) {
    addNotification({
      usernames: [request.requester.username],
      title:
        status === "approved"
          ? `${request.title}已通过`
          : `${request.title}已驳回`,
      content:
        status === "approved"
          ? `${request.title}已处理完成。`
          : `${request.title}未通过，请根据驳回理由修改后重新提交。`,
      badgeText: status === "approved" ? "已通过" : "已驳回",
      badgeClass: status === "approved" ? "is-approved" : "is-rejected",
      meta: "审核结果",
      reason: status === "rejected" ? String(reason).trim() : "",
    });
  }

  return request;
}

export function useNotifications(userSource) {
  ensureLoaded();

  const currentUser = computed(() => userSource || {});
  const visibleReviewRequests = computed(() =>
    store.reviewRequests.filter((item) =>
      isReviewVisibleForUser(item, currentUser.value),
    ),
  );
  const visibleNotifications = computed(() =>
    store.notifications.filter((item) =>
      matchesNotificationAudience(item, currentUser.value),
    ),
  );
  const inboxEntries = computed(() =>
    [
      ...visibleReviewRequests.value.map((item) =>
        buildReviewEntry(item, currentUser.value),
      ),
      ...visibleNotifications.value.map((item) => buildNotificationEntry(item)),
    ].sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime(),
    ),
  );
  const pendingCount = computed(() =>
    visibleReviewRequests.value.filter((item) => item.status === "pending").length,
  );

  return {
    inboxEntries,
    pendingCount,
    reviewRequests: visibleReviewRequests,
    notifications: visibleNotifications,
    submitAchievementReviewRequest,
    submitProfileReviewRequest,
    addNotification,
    updateReviewRequestStatus,
  };
}
