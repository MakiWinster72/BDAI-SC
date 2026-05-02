import { computed, reactive } from "vue";
import { getSystemSettings } from "../api/admin";
import {
  approveAchievementReviewRequest,
  cancelAchievementReviewRequest,
  listAchievementReviewRequests,
  rejectAchievementReviewRequest,
  setAchievementReviewRequestDocuments,
  submitAchievementReviewRequestApi,
} from "../api/achievementReviewRequests";
import {
  approveProfileReviewRequest,
  cancelProfileReviewRequest,
  listProfileReviewRequests,
  rejectProfileReviewRequest,
  setProfileReviewRequestDocuments,
  submitProfileReviewRequestApi,
} from "../api/profileReviewRequests";

const STORAGE_BASE = "bdai_sc_notification_center";

function getStorageKey() {
  try {
    const token = localStorage.getItem("bdai_sc_token");
    if (token) {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const user = payload.sub || payload.username || "unknown";
      return `${STORAGE_BASE}_${user}`;
    }
  } catch { /* fallback */ }
  return STORAGE_BASE;
}
const DEFAULT_DELAYED_THRESHOLD_MS = 2 * 24 * 60 * 60 * 1000;
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
  notifications: [],
  achievementReviewRequests: [],
  achievementReviewFetched: false,
  achievementReviewLoading: false,
  profileReviewRequests: [],
  profileReviewFetched: false,
  profileReviewLoading: false,
  processedReadIds: new Set(),
  readIds: new Set(),
  delayedThresholdMs: DEFAULT_DELAYED_THRESHOLD_MS,
});

function ensureLoaded() {
  if (store.loaded || typeof window === "undefined") {
    return;
  }
  store.loaded = true;
  const key = getStorageKey();
  try {
    const raw = JSON.parse(localStorage.getItem(key) || "{}");
    store.notifications = Array.isArray(raw.notifications)
      ? raw.notifications
      : [];
    store.processedReadIds = new Set(Array.isArray(raw.processedReadIds) ? raw.processedReadIds : []);
    store.readIds = new Set(Array.isArray(raw.readIds) ? raw.readIds : []);
  } catch {
    store.notifications = [];
    store.processedReadIds = new Set();
  }
}

function persistStore() {
  if (typeof window === "undefined") {
    return;
  }
  const key = getStorageKey();
  localStorage.setItem(
    key,
    JSON.stringify({
      notifications: store.notifications,
      processedReadIds: [...store.processedReadIds],
      readIds: [...store.readIds],
    }),
  );
}

function syncStoredIds() {
  const allRequestIds = new Set([
    ...store.achievementReviewRequests.map((r) => String(r.id)),
    ...store.profileReviewRequests.map((r) => String(r.id)),
  ]);
  let changed = false;
  for (const id of store.processedReadIds) {
    if (!allRequestIds.has(id)) {
      store.processedReadIds.delete(id);
      changed = true;
    }
  }
  for (const id of store.readIds) {
    if (!allRequestIds.has(id)) {
      store.readIds.delete(id);
      changed = true;
    }
  }
  if (changed) {
    persistStore();
  }
}

async function fetchDelayedThreshold() {
  if (typeof window === "undefined") return;
  try {
    const res = await getSystemSettings();
    const days = res.data?.delayedThresholdDays;
    if (days && days >= 1) {
      store.delayedThresholdMs = days * 24 * 60 * 60 * 1000;
    }
  } catch { /* ignore */ }
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
    supportingDocuments: Array.isArray(request.supportingDocuments) ? request.supportingDocuments : [],
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
  /// Handle LocalDateTime strings without timezone (e.g., "2026-04-26T14:27:30.527344")
  // JavaScript parses these as UTC, but Java LocalDateTime is local time (CST/UTC+8)
  // Subtract 8 hours to convert local-time string to proper UTC milliseconds
  let createdTime;
  if (typeof createdAt === "string" && createdAt.includes("T") && !createdAt.endsWith("Z") && !createdAt.includes("+")) {
    createdTime = new Date(createdAt).getTime() - 8 * 60 * 60 * 1000;
  } else {
    createdTime = new Date(createdAt).getTime();
  }
  if (!Number.isNaN(createdTime) && Date.now() - createdTime >= store.delayedThresholdMs) {
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
  const token = localStorage.getItem("bdai_sc_token");
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
  const token = localStorage.getItem("bdai_sc_token");
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

async function updateReviewRequestStatus({ requestId, status, reviewer, reason = "", resourceType }) {
  const isAch = resourceType === "achievement";
  if (!isAch && resourceType !== "profile") {
    throw new Error("审核请求类型无效");
  }
  if (status === "rejected" && !String(reason || "").trim()) {
    throw new Error("驳回时必须填写理由");
  }
  const response = isAch
    ? (status === "approved"
        ? await approveAchievementReviewRequest(requestId)
        : await rejectAchievementReviewRequest(requestId, { reason: String(reason || "").trim() }))
    : (status === "approved"
        ? await approveProfileReviewRequest(requestId)
        : await rejectProfileReviewRequest(requestId, { reason: String(reason || "").trim() }));
  if (isAch) {
    upsertAchievementReviewRequest(response.data);
  } else {
    upsertProfileReviewRequest(response.data);
  }
  return response.data;
}

async function cancelReviewRequest({ requestId, resourceType }) {
  const isAch = resourceType === "achievement";
  if (!isAch && resourceType !== "profile") {
    throw new Error("审核请求类型无效");
  }

  if (isAch) {
    await cancelAchievementReviewRequest(requestId);
    store.achievementReviewRequests = store.achievementReviewRequests.filter(
      (item) => String(item.id) !== String(requestId),
    );
    return;
  }

  await cancelProfileReviewRequest(requestId);
  store.profileReviewRequests = store.profileReviewRequests.filter(
    (item) => String(item.id) !== String(requestId),
  );
}

async function setSupportingDocuments({ requestId, documents, resourceType }) {
  const isAch = resourceType === "achievement";
  if (!isAch && resourceType !== "profile") {
    throw new Error("审核请求类型无效");
  }
  const response = isAch
    ? await setAchievementReviewRequestDocuments(requestId, documents)
    : await setProfileReviewRequestDocuments(requestId, documents);
  if (isAch) {
    upsertAchievementReviewRequest(response.data);
  } else {
    upsertProfileReviewRequest(response.data);
  }
  return response.data;
}

export function useNotifications(userSource) {
  ensureLoaded();
  fetchDelayedThreshold();
  fetchAchievementReviewRequests().catch(() => {
    store.achievementReviewFetched = true;
  });
  fetchProfileReviewRequests().catch(() => {
    store.profileReviewFetched = true;
  });

  const currentUser = computed(() => userSource || {});
  const visibleReviewRequests = computed(() =>
    [
      ...store.achievementReviewRequests,
      ...store.profileReviewRequests,
    ].filter((item) => isReviewVisibleForUser(item, currentUser.value)),
  );
  const visibleNotifications = computed(() =>
    store.notifications.filter((item) =>
      matchesNotificationAudience(item, currentUser.value),
    ),
  );
  const inboxEntries = computed(() => {
    const requests = [
      ...store.achievementReviewRequests,
      ...store.profileReviewRequests,
    ];

    const visible = requests.filter((item) => isReviewVisibleForUser(item, currentUser.value));

    const entries = [
      ...visibleReviewRequests.value.map((item) =>
        buildReviewEntry(item, currentUser.value),
      ),
      ...visibleNotifications.value.map((item) => buildNotificationEntry(item)),
    ];
    // Defensive dedup by sourceId+resourceType to prevent duplicate-key Vue warnings
    const seen = new Set();
    return entries
      .filter((entry) => {
        const key = `${entry.sourceId}-${entry.resourceType}`;
        if (seen.has(key)) return false;
        seen.add(key);
        return true;
      })
      .sort(
        (left, right) =>
          new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime(),
      );
  });
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
      (item) => item.resourceType === "profile" && item.status === "pending" && item.requester?.username === currentUser.value?.username,
    ),
  );

  const processedUnreadCount = computed(() =>
    inboxEntries.value.filter(
      (entry) =>
        (entry.categoryKey === "approved" || entry.categoryKey === "rejected") &&
        !store.processedReadIds.has(String(entry.id)),
    ).length,
  );

  // Class review entries for CADRE role - filtered by requester's class matching current user's class
  const classReviewEntries = computed(() => {
    if (currentUser.value.role !== "CADRE") return [];
    const myClass = currentUser.value.className;
    if (!myClass) return [];

    const allRequests = [
      ...store.achievementReviewRequests,
      ...store.profileReviewRequests,
    ].filter((r) => {
      // Exclude own requests
      if (r.requester?.username === currentUser.value.username) return false;
      // Exclude other CADRE requests - only regular students need review
      if (r.requester?.role === "CADRE") return false;
      const requesterClass = r.requester?.className;
      if (!requesterClass) return false;
      return requesterClass.trim() === myClass.trim();
    });

    return allRequests.map((r) => buildReviewEntry(r, currentUser.value));
  });

  function markProcessedEntryRead(entryId) {
    store.processedReadIds.add(String(entryId));
    persistStore();
  }

  function markEntryRead(entryId) {
    store.readIds.add(String(entryId));
    persistStore();
  }

  function markEntryUnread(entryId) {
    store.readIds.delete(String(entryId));
    persistStore();
  }

  function markAllRead() {
    inboxEntries.value.forEach((e) => store.readIds.add(String(e.id)));
    persistStore();
  }

  const totalUnreadCount = computed(() =>
    inboxEntries.value.filter((e) => !store.readIds.has(String(e.id))).length,
  );

  const unreadEntries = computed(() =>
    inboxEntries.value.filter((e) => !store.readIds.has(String(e.id))),
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

  async function refreshNotifications() {
    store.achievementReviewFetched = false;
    store.profileReviewFetched = false;
    await Promise.all([
      fetchAchievementReviewRequests(true).catch(() => {}),
      fetchProfileReviewRequests(true).catch(() => {}),
    ]);
  }

  return {
    inboxEntries,
    pendingCount,
    categoryCounts,
    processedUnreadCount,
    totalUnreadCount,
    unreadEntries,
    processedReadIds: store.processedReadIds,
    readIds: store.readIds,
    reviewRequests: visibleReviewRequests,
    notifications: visibleNotifications,
    hasPendingProfileReviewRequest,
    findPendingAchievementReview,
    fetchAchievementReviewRequests,
    fetchProfileReviewRequests,
    refreshNotifications,
    submitAchievementReviewRequest,
    submitProfileReviewRequest,
    addNotification,
    updateReviewRequestStatus,
    cancelReviewRequest,
    setSupportingDocuments,
    markProcessedEntryRead,
    markEntryRead,
    markEntryUnread,
    markAllRead,
    classReviewEntries,
  };
}
