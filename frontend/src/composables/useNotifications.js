import { computed, reactive } from "vue";
import {
  approveAchievementReviewRequest,
  cancelAchievementReviewRequest,
  listAchievementReviewRequests,
  rejectAchievementReviewRequest,
  submitAchievementReviewRequestApi,
} from "../api/achievementReviewRequests";
import {
  approveProfileReviewRequest,
  cancelProfileReviewRequest,
  listProfileReviewRequests,
  rejectProfileReviewRequest,
  submitProfileReviewRequestApi,
} from "../api/profileReviewRequests";

const STORAGE_KEY = "gcsc_notification_center";
const REVIEWER_ROLES = ["TEACHER", "ADMIN"];
const DELAYED_THRESHOLD_MS = 3 * 24 * 60 * 60 * 1000;
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
  achievementReviewRequests: [],
  achievementReviewFetched: false,
  achievementReviewLoading: false,
  profileReviewRequests: [],
  profileReviewFetched: false,
  profileReviewLoading: false,
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

function isRemoteAchievementReview(request) {
  return request?.resourceType === "achievement";
}

function upsertAchievementReviewRequest(nextRequest) {
  const index = store.achievementReviewRequests.findIndex(
    (item) => String(item.id) === String(nextRequest.id),
  );
  if (index === -1) {
    store.achievementReviewRequests.unshift(nextRequest);
    return;
  }
  store.achievementReviewRequests.splice(index, 1, nextRequest);
}

function upsertProfileReviewRequest(nextRequest) {
  const index = store.profileReviewRequests.findIndex(
    (item) => String(item.id) === String(nextRequest.id),
  );
  if (index === -1) {
    store.profileReviewRequests.unshift(nextRequest);
    return;
  }
  store.profileReviewRequests.splice(index, 1, nextRequest);
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

  const categoryKey = classifyNotificationCategory({
    status: request.status,
    createdAt: request.createdAt,
    source: "review-request",
  });

  const createdTime = new Date(request.createdAt).getTime();
  const delayedDays = !Number.isNaN(createdTime)
    ? Math.floor((Date.now() - createdTime) / (24 * 60 * 60 * 1000))
    : 0;

  const badgeText =
    request.status === "approved"
      ? "已通过"
      : request.status === "rejected"
        ? "已驳回"
        : categoryKey === "delayed"
          ? `已滞后${delayedDays}天`
          : "待处理";
  const badgeClass =
    request.status === "approved"
      ? "is-approved"
      : request.status === "rejected"
        ? "is-rejected"
        : categoryKey === "delayed"
          ? "is-delayed"
          : "is-pending";

  return {
    id: request.id,
    sourceId: request.id,
    source: "review-request",
    title: `${toDisplayName(request.requester)} ${requestTypeLabel}修改`,
    content:
      request.status === "approved"
        ? approvedContent
        : request.status === "rejected"
          ? rejectedContent
          : pendingContent,
    reason: request.rejectionReason || "",
    badgeText,
    badgeClass,
    meta: isOwner
      ? `${requestTypeLabel}审核`
      : `${requestTypeLabel}请求 · ${toDisplayName(request.requester)}`,
    summary: request.summary || "",
    status: request.status,
    requester: request.requester || null,
    reviewer: request.reviewer || null,
    resourceType: request.resourceType,
    action: request.action,
    category: request.category || "",
    categoryLabel: request.categoryLabel || "",
    recordId: request.recordId || null,
    payloadSnapshot: request.payloadSnapshot || null,
    changes: Array.isArray(request.changes) ? request.changes : [],
    categoryKey,
    timeText: formatRelativeTime(request.updatedAt || request.createdAt),
    createdAt: request.updatedAt || request.createdAt,
  };
}

function buildNotificationEntry(notification) {
  const categoryKey = classifyNotificationCategory({
    status: notification.status || "processed",
    createdAt: notification.createdAt,
    source: "notification",
  });
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
    categoryKey,
    timeText: formatRelativeTime(notification.createdAt),
    createdAt: notification.createdAt,
  };
}

export function classifyNotificationCategory({ status, createdAt, source }) {
  if (source === "notification") {
    return "system";
  }
  if (status === "approved") {
    return "approved";
  }
  if (status === "rejected") {
    return "rejected";
  }
  const createdTime = new Date(createdAt).getTime();
  if (!Number.isNaN(createdTime) && Date.now() - createdTime >= DELAYED_THRESHOLD_MS) {
    return "delayed";
  }
  return "pending";
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

async function fetchAchievementReviewRequests(force = false) {
  if (typeof window === "undefined") {
    return store.achievementReviewRequests;
  }
  const token = localStorage.getItem("gcsc_token");
  if (!token) {
    store.achievementReviewRequests = [];
    store.achievementReviewFetched = true;
    return store.achievementReviewRequests;
  }
  if (store.achievementReviewLoading) {
    return store.achievementReviewRequests;
  }
  if (!force && store.achievementReviewFetched) {
    return store.achievementReviewRequests;
  }
  store.achievementReviewLoading = true;
  try {
    const { data } = await listAchievementReviewRequests();
    store.achievementReviewRequests = Array.isArray(data) ? data : [];
    store.achievementReviewFetched = true;
    return store.achievementReviewRequests;
  } finally {
    store.achievementReviewLoading = false;
  }
}

async function fetchProfileReviewRequests(force = false) {
  if (typeof window === "undefined") {
    return store.profileReviewRequests;
  }
  const token = localStorage.getItem("gcsc_token");
  if (!token) {
    store.profileReviewRequests = [];
    store.profileReviewFetched = true;
    return store.profileReviewRequests;
  }
  if (store.profileReviewLoading) {
    return store.profileReviewRequests;
  }
  if (!force && store.profileReviewFetched) {
    return store.profileReviewRequests;
  }
  store.profileReviewLoading = true;
  try {
    const { data } = await listProfileReviewRequests();
    store.profileReviewRequests = Array.isArray(data) ? data : [];
    store.profileReviewFetched = true;
    return store.profileReviewRequests;
  } finally {
    store.profileReviewLoading = false;
  }
}

function createReviewRequest({
  actor,
  resourceType,
  action,
  title,
  summary,
  payloadSnapshot = null,
  changes = [],
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
    changes,
    ...extra,
  };
  store.reviewRequests.unshift(request);
  persistStore();
  return request;
}

async function submitAchievementReviewRequest({
  actor,
  action,
  category,
  title,
  payload = null,
  payloadSnapshot = null,
  recordId = null,
  changes = [],
}) {
  const categoryLabel = resolveAchievementCategoryLabel(category);
  const { data } = await submitAchievementReviewRequestApi({
    category,
    action,
    recordId,
    title: action === "update" ? `成就修改待审核` : `成就新增待审核`,
    summary: `${toDisplayName(actor)}${action === "update" ? "修改" : "新增"}了「${title || categoryLabel}」`,
    payload,
    payloadSnapshot,
    changes,
  });
  upsertAchievementReviewRequest(data);
  return data;
}

async function submitProfileReviewRequest({
  actor,
  payloadSnapshot = null,
  changes = [],
}) {
  const { data } = await submitProfileReviewRequestApi({
    title: "个人信息修改待审核",
    summary: `${toDisplayName(actor)} 提交了个人信息修改申请`,
    payloadSnapshot,
    changes,
  });
  upsertProfileReviewRequest(data);
  return data;
}

async function updateReviewRequestStatus({ requestId, status, reviewer, reason = "" }) {
  ensureLoaded();

  // Check by resourceType explicitly to avoid routing to wrong API
  const requestType = store.achievementReviewRequests.find(
    (item) => String(item.id) === String(requestId) && item.resourceType === "achievement",
  ) ? "achievement" : null;

  const profileType = store.profileReviewRequests.find(
    (item) => String(item.id) === String(requestId) && item.resourceType === "profile",
  ) ? "profile" : null;

  if (requestType === "achievement") {
    if (status === "rejected" && !String(reason || "").trim()) {
      throw new Error("驳回时必须填写理由");
    }
    const response =
      status === "approved"
        ? await approveAchievementReviewRequest(requestId)
        : await rejectAchievementReviewRequest(requestId, {
            reason: String(reason || "").trim(),
          });
    upsertAchievementReviewRequest(response.data);
    return response.data;
  }

  if (profileType === "profile") {
    if (status === "rejected" && !String(reason || "").trim()) {
      throw new Error("驳回时必须填写理由");
    }
    const response =
      status === "approved"
        ? await approveProfileReviewRequest(requestId)
        : await rejectProfileReviewRequest(requestId, {
            reason: String(reason || "").trim(),
          });
    upsertProfileReviewRequest(response.data);
    return response.data;
  }

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

async function cancelReviewRequest({ requestId }) {
  ensureLoaded();

  const requestType = store.achievementReviewRequests.find(
    (item) => String(item.id) === String(requestId) && item.resourceType === "achievement",
  ) ? "achievement" : null;

  const profileType = store.profileReviewRequests.find(
    (item) => String(item.id) === String(requestId) && item.resourceType === "profile",
  ) ? "profile" : null;

  if (requestType === "achievement") {
    await cancelAchievementReviewRequest(requestId);
    store.achievementReviewRequests = store.achievementReviewRequests.filter(
      (item) => String(item.id) !== String(requestId),
    );
    return;
  }

  if (profileType === "profile") {
    await cancelProfileReviewRequest(requestId);
    store.profileReviewRequests = store.profileReviewRequests.filter(
      (item) => String(item.id) !== String(requestId),
    );
    return;
  }

  // Local request (fallback for backwards compatibility)
  const request = store.reviewRequests.find((item) => item.id === requestId);
  if (!request) {
    throw new Error("审核请求不存在");
  }
  if (request.requester?.username !== userSource?.username) {
    throw new Error("只能取消自己的申请");
  }
  if (request.status !== "pending") {
    throw new Error("只能取消待审核的申请");
  }
  store.reviewRequests = store.reviewRequests.filter((item) => item.id !== requestId);
  persistStore();
}

export function useNotifications(userSource) {
  ensureLoaded();
  fetchAchievementReviewRequests().catch(() => {
    store.achievementReviewFetched = true;
  });
  fetchProfileReviewRequests().catch(() => {
    store.profileReviewFetched = true;
  });

  const currentUser = computed(() => userSource || {});
  const localProfileReviewRequests = computed(() =>
    store.reviewRequests.filter(
      (item) => item.resourceType === "profile" && isReviewVisibleForUser(item, currentUser.value),
    ),
  );
  const remoteProfileReviewRequests = computed(() =>
    store.profileReviewRequests.filter((item) =>
      isReviewVisibleForUser(item, currentUser.value),
    ),
  );
  const visibleAchievementReviewRequests = computed(() =>
    store.achievementReviewRequests.filter((item) =>
      isReviewVisibleForUser(item, currentUser.value),
    ),
  );
  const visibleReviewRequests = computed(() =>
    [
      ...visibleAchievementReviewRequests.value,
      ...localProfileReviewRequests.value,
      ...remoteProfileReviewRequests.value,
    ],
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
  const categoryCounts = computed(() => {
    const counts = {
      pending: 0,
      delayed: 0,
      approved: 0,
      rejected: 0,
    };
    inboxEntries.value.forEach((entry) => {
      const key = entry.categoryKey || "pending";
      if (counts[key] !== undefined) {
        counts[key] += 1;
      }
    });
    return counts;
  });

  const hasPendingProfileReviewRequest = computed(() =>
    visibleReviewRequests.value.some(
      (item) => item.resourceType === "profile" && item.status === "pending",
    ),
  );

  function findPendingAchievementReview(recordId, category) {
    if (!recordId) return null;
    return store.achievementReviewRequests.find(
      (item) =>
        item.resourceType === "achievement" &&
        String(item.recordId) === String(recordId) &&
        item.category === category &&
        item.status === "pending",
    ) || null;
  }

  return {
    inboxEntries,
    pendingCount,
    categoryCounts,
    reviewRequests: visibleReviewRequests,
    notifications: visibleNotifications,
    hasPendingProfileReviewRequest,
    findPendingAchievementReview,
    fetchAchievementReviewRequests,
    fetchProfileReviewRequests,
    submitAchievementReviewRequest,
    submitProfileReviewRequest,
    addNotification,
    updateReviewRequestStatus,
    cancelReviewRequest,
  };
}
