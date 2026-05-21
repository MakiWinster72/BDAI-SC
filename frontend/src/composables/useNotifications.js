import { computed, reactive, watch } from "vue";
import { getSystemSettings } from "@/api/admin";
import {
  approveAchievementReviewRequest,
  cancelAchievementReviewRequest,
  rejectAchievementReviewRequest,
  setAchievementReviewRequestDocuments,
  submitAchievementReviewRequestApi,
} from "@/api/achievementReviewRequests";
import {
  approveProfileReviewRequest,
  cancelProfileReviewRequest,
  rejectProfileReviewRequest,
  setProfileReviewRequestDocuments,
  submitProfileReviewRequestApi,
} from "@/api/profileReviewRequests";
import {
  findPendingAchievementReview as findPendingAchievementReviewApi,
  getReviewInboxDetail,
  getReviewInboxStats,
  listReviewInbox,
} from "@/api/reviewInbox";
import {
  markAllNotificationsRead,
  markNotificationRead,
  markNotificationUnread,
} from "@/api/notificationReadStates";

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
const INBOX_PAGE_SIZE = 20;
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
  inboxSummaries: [],
  inboxPage: 0,
  inboxPages: 0,
  inboxTotal: 0,
  inboxLoading: false,
  inboxCategory: "pending",
  inboxSearch: "",
  inboxCategoryCounts: {},
  inboxStats: null,
  classReviewSummaries: [],
  classReviewPage: 0,
  classReviewPages: 0,
  classReviewTotal: 0,
  classReviewLoading: false,
  classReviewCategory: "pending",
  classReviewCategoryCounts: {},
  detailCache: {},
  detailLoadingKey: "",
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
    store.readIds = new Set(Array.isArray(raw.readIds) ? raw.readIds.map((id) => String(id)) : []);
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

function getEntryReadKey(entryOrId, resourceType = "") {
  if (entryOrId && typeof entryOrId === "object") {
    const type = entryOrId.resourceType || entryOrId.source || resourceType || "notification";
    const id = entryOrId.sourceId || entryOrId.id;
    return `${type}:${id}`;
  }
  return resourceType ? `${resourceType}:${entryOrId}` : String(entryOrId);
}

function syncReadIdsFromServer(entries) {
  let changed = false;
  entries.forEach((entry) => {
    if (entry.source !== "review-request") {
      return;
    }
    const key = getEntryReadKey(entry);
    if (entry.read) {
      if (!store.readIds.has(key)) {
        store.readIds.add(key);
        changed = true;
      }
    } else if (store.readIds.has(key)) {
      store.readIds.delete(key);
      changed = true;
    }
  });
  if (changed) {
    persistStore();
  }
}

function syncStoredIds() {
  const allReadKeys = new Set([
    ...store.inboxSummaries.map((r) => getEntryReadKey(r.id, r.resourceType)),
    ...store.classReviewSummaries.map((r) => getEntryReadKey(r.id, r.resourceType)),
    ...store.notifications.map((n) => getEntryReadKey(n)),
  ]);
  let changed = false;
  for (const id of store.processedReadIds) {
    const stillExists = store.inboxSummaries.some((r) => String(r.id) === String(id))
      || store.classReviewSummaries.some((r) => String(r.id) === String(id));
    if (!stillExists) {
      store.processedReadIds.delete(id);
      changed = true;
    }
  }
  for (const id of store.readIds) {
    if (!allReadKeys.has(String(id))) {
      store.readIds.delete(id);
      changed = true;
    }
  }
  if (changed) {
    persistStore();
  }
}

function getDetailCacheKey(resourceType, id) {
  return `${resourceType}:${id}`;
}

function summaryToReviewRequest(summary) {
  if (!summary) {
    return null;
  }
  return {
    id: summary.id,
    resourceType: summary.resourceType,
    status: summary.status,
    action: summary.action,
    category: summary.category,
    categoryLabel: summary.categoryLabel,
    recordId: summary.recordId,
    title: summary.title,
    summary: summary.summary,
    rejectionReason: summary.rejectionReason,
    requester: summary.requester,
    reviewer: summary.reviewer,
    targetRoles: summary.targetRoles,
    createdAt: summary.createdAt,
    updatedAt: summary.updatedAt,
    read: summary.read,
  };
}

function mergeRequestWithDetail(request) {
  if (!request?.resourceType || request.id === undefined || request.id === null) {
    return request;
  }
  const cached = store.detailCache[getDetailCacheKey(request.resourceType, request.id)];
  if (!cached) {
    return request;
  }
  return {
    ...request,
    payloadSnapshot: cached.payloadSnapshot ?? null,
    changes: Array.isArray(cached.changes) ? cached.changes : [],
    supportingDocuments: Array.isArray(cached.supportingDocuments) ? cached.supportingDocuments : [],
    rejectionReason: cached.rejectionReason ?? request.rejectionReason,
    status: cached.status ?? request.status,
    reviewer: cached.reviewer ?? request.reviewer,
    read: cached.read ?? request.read,
  };
}

function fullResponseToSummary(request) {
  if (!request) {
    return null;
  }
  return {
    id: request.id,
    resourceType: request.resourceType,
    status: request.status,
    action: request.action,
    category: request.category,
    categoryLabel: request.categoryLabel,
    recordId: request.recordId,
    title: request.title,
    summary: request.summary,
    rejectionReason: request.rejectionReason,
    requester: request.requester,
    reviewer: request.reviewer,
    targetRoles: request.targetRoles,
    createdAt: request.createdAt,
    updatedAt: request.updatedAt,
    read: request.read,
  };
}

function upsertSummaryInList(list, summary) {
  const index = list.findIndex(
    (item) => String(item.id) === String(summary.id) && item.resourceType === summary.resourceType,
  );
  if (index === -1) {
    list.unshift(summary);
    return;
  }
  list.splice(index, 1, summary);
}

function removeSummaryFromLists(requestId, resourceType) {
  store.inboxSummaries = store.inboxSummaries.filter(
    (item) => !(String(item.id) === String(requestId) && item.resourceType === resourceType),
  );
  store.classReviewSummaries = store.classReviewSummaries.filter(
    (item) => !(String(item.id) === String(requestId) && item.resourceType === resourceType),
  );
}

function dedupeSortEntries(entries) {
  const seen = new Set();
  return entries
    .filter((entry) => {
      const key = getEntryReadKey(entry);
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    })
    .sort(
      (left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime(),
    );
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

function upsertReviewRequest(nextRequest) {
  if (!nextRequest) {
    return;
  }
  const summary = fullResponseToSummary(nextRequest);
  store.detailCache[getDetailCacheKey(summary.resourceType, summary.id)] = nextRequest;
  upsertSummaryInList(store.inboxSummaries, summary);
  upsertSummaryInList(store.classReviewSummaries, summary);
}

function setReviewRequestReadState(resourceType, resourceId, read) {
  const updateList = (list) => {
    const request = list.find(
      (item) => String(item.id) === String(resourceId) && item.resourceType === resourceType,
    );
    if (request) {
      request.read = read;
    }
  };
  updateList(store.inboxSummaries);
  updateList(store.classReviewSummaries);
  const cacheKey = getDetailCacheKey(resourceType, resourceId);
  if (store.detailCache[cacheKey]) {
    store.detailCache[cacheKey] = { ...store.detailCache[cacheKey], read };
  }
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
    read: Boolean(request.read),
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
    read: store.readIds.has(getEntryReadKey(notification)),
    timeText: formatRelativeTime(notification.createdAt),
    createdAt: notification.createdAt,
  };
}

export function formatNotificationEntryNumber(entry) {
  if (!entry || entry.source !== "review-request") {
    return "";
  }
  const id = entry.sourceId ?? entry.id;
  if (id === null || id === undefined || id === "") {
    return "";
  }
  return `#${id}`;
}

export function entryMatchesNotificationSearch(entry, rawQuery) {
  const query = String(rawQuery || "").trim();
  if (!query) {
    return true;
  }
  const normalized = query.toLowerCase();
  const textFields = [entry.title, entry.content, entry.summary, entry.meta, entry.reason]
    .filter((value) => value !== null && value !== undefined && value !== "")
    .map((value) => String(value).toLowerCase());
  if (textFields.some((text) => text.includes(normalized))) {
    return true;
  }
  if (entry.source !== "review-request") {
    return false;
  }
  const idStr = String(entry.sourceId ?? entry.id ?? "");
  const numQuery = query.replace(/^#/, "").trim();
  if (!numQuery || !idStr) {
    return false;
  }
  return idStr.includes(numQuery) || `#${idStr}`.toLowerCase().includes(normalized);
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

async function loadInboxPage({
  page = 1,
  category = store.inboxCategory,
  search = store.inboxSearch,
  scope = "inbox",
  append = false,
} = {}) {
  if (typeof window === "undefined") {
    return [];
  }
  const token = localStorage.getItem("bdai_sc_token");
  if (!token) {
    if (scope === "class-reviews") {
      store.classReviewSummaries = [];
    } else {
      store.inboxSummaries = [];
    }
    return [];
  }

  const isClassScope = scope === "class-reviews";
  if (isClassScope) {
    if (store.classReviewLoading) {
      return store.classReviewSummaries;
    }
    store.classReviewLoading = true;
    store.classReviewCategory = category;
  } else {
    if (store.inboxLoading) {
      return store.inboxSummaries;
    }
    store.inboxLoading = true;
    store.inboxCategory = category;
    store.inboxSearch = search;
  }

  try {
    const { data } = await listReviewInbox({
      page,
      size: INBOX_PAGE_SIZE,
      category,
      search: search?.trim() || undefined,
      scope,
    });
    const items = Array.isArray(data?.items) ? data.items : [];
    if (isClassScope) {
      store.classReviewSummaries = append ? [...store.classReviewSummaries, ...items] : items;
      store.classReviewPage = data?.page || page;
      store.classReviewPages = data?.pages || 0;
      store.classReviewTotal = data?.total || 0;
      store.classReviewCategoryCounts = data?.categoryCounts || {};
    } else {
      store.inboxSummaries = append ? [...store.inboxSummaries, ...items] : items;
      store.inboxPage = data?.page || page;
      store.inboxPages = data?.pages || 0;
      store.inboxTotal = data?.total || 0;
      store.inboxCategoryCounts = data?.categoryCounts || {};
    }
    return items;
  } finally {
    if (isClassScope) {
      store.classReviewLoading = false;
    } else {
      store.inboxLoading = false;
    }
  }
}

async function loadInboxStats(scope = "inbox") {
  if (typeof window === "undefined") {
    return null;
  }
  const token = localStorage.getItem("bdai_sc_token");
  if (!token) {
    store.inboxStats = null;
    return null;
  }
  const { data } = await getReviewInboxStats({ scope }).catch(() => ({ data: null }));
  if (scope === "inbox") {
    store.inboxStats = data;
  }
  return data;
}

async function fetchReviewRequestDetail(resourceType, id) {
  const cacheKey = getDetailCacheKey(resourceType, id);
  if (store.detailCache[cacheKey]) {
    return store.detailCache[cacheKey];
  }
  store.detailLoadingKey = cacheKey;
  const { data } = await getReviewInboxDetail(resourceType, id);
  store.detailCache[cacheKey] = data;
  upsertReviewRequest(data);
  store.detailLoadingKey = "";
  return data;
}

async function fetchProfileReviewRequests(force = false) {
  await Promise.all([
    loadInboxPage({ page: 1, category: store.inboxCategory, append: false }),
    loadInboxStats("inbox"),
  ]);
  if (force) {
    return loadInboxPage({ page: 1, category: store.inboxCategory, append: false });
  }
  return store.inboxSummaries;
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
  upsertReviewRequest(data);
  await loadInboxStats("inbox");
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
  upsertReviewRequest(data);
  await loadInboxStats("inbox");
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
  upsertReviewRequest(response.data);
  await Promise.all([
    loadInboxPage({ page: 1, category: store.inboxCategory, search: store.inboxSearch, append: false }),
    loadInboxStats("inbox"),
  ]);
  return response.data;
}

async function cancelReviewRequest({ requestId, resourceType }) {
  const isAch = resourceType === "achievement";
  if (!isAch && resourceType !== "profile") {
    throw new Error("审核请求类型无效");
  }

  if (isAch) {
    await cancelAchievementReviewRequest(requestId);
  } else {
    await cancelProfileReviewRequest(requestId);
  }
  removeSummaryFromLists(requestId, resourceType);
  delete store.detailCache[getDetailCacheKey(resourceType, requestId)];
  await Promise.all([
    loadInboxPage({ page: 1, category: store.inboxCategory, search: store.inboxSearch, append: false }),
    loadInboxStats("inbox"),
  ]);
}

async function setSupportingDocuments({ requestId, documents, resourceType }) {
  const isAch = resourceType === "achievement";
  if (!isAch && resourceType !== "profile") {
    throw new Error("审核请求类型无效");
  }
  const response = isAch
    ? await setAchievementReviewRequestDocuments(requestId, documents)
    : await setProfileReviewRequestDocuments(requestId, documents);
  upsertReviewRequest(response.data);
  return response.data;
}

function buildEntriesFromSummaries(summaries, user) {
  return summaries.map((summary) =>
    buildReviewEntry(mergeRequestWithDetail(summaryToReviewRequest(summary)), user),
  );
}

export function useNotifications(userSource) {
  ensureLoaded();
  fetchDelayedThreshold();
  loadInboxPage({ page: 1, category: "pending" }).catch(() => {});
  loadInboxStats("inbox").catch(() => {});

  const currentUser = computed(() => userSource || {});
  const visibleNotifications = computed(() =>
    store.notifications.filter((item) =>
      matchesNotificationAudience(item, currentUser.value),
    ),
  );
  const inboxEntries = computed(() => {
    const reviewEntries = buildEntriesFromSummaries(store.inboxSummaries, currentUser.value);
    const localEntries = visibleNotifications.value.map((item) =>
      buildNotificationEntry(item),
    );
    return dedupeSortEntries([...localEntries, ...reviewEntries]);
  });
  const pendingCount = computed(() => Number(store.inboxStats?.pending ?? 0));
  const categoryCounts = computed(() => ({
    pending: Number(store.inboxCategoryCounts?.pending ?? 0),
    delayed: Number(store.inboxCategoryCounts?.delayed ?? 0),
    approved: Number(store.inboxCategoryCounts?.approved ?? 0),
    rejected: Number(store.inboxCategoryCounts?.rejected ?? 0),
    unread: Number(store.inboxCategoryCounts?.unread ?? 0),
  }));

  const hasPendingProfileReviewRequest = computed(() => Boolean(store.inboxStats?.hasPendingProfile));
  const inboxHasMore = computed(() => store.inboxPage < store.inboxPages);
  const classReviewHasMore = computed(() => store.classReviewPage < store.classReviewPages);

  const processedUnreadCount = computed(() =>
    inboxEntries.value.filter(
      (entry) =>
        (entry.categoryKey === "approved" || entry.categoryKey === "rejected") &&
        !store.processedReadIds.has(String(entry.id)),
    ).length,
  );

  const classReviewEntries = computed(() => {
    if (currentUser.value.role !== "CADRE") {
      return [];
    }
    return buildEntriesFromSummaries(store.classReviewSummaries, currentUser.value);
  });

  watch(
    inboxEntries,
    (entries) => {
      syncReadIdsFromServer(entries);
      syncStoredIds();
    },
    { immediate: true },
  );

  function markProcessedEntryRead(entryId) {
    store.processedReadIds.add(String(entryId));
    persistStore();
  }

  async function markEntryRead(entryOrId, resourceType = "") {
    const key = getEntryReadKey(entryOrId, resourceType);
    store.readIds.add(key);
    if (entryOrId && typeof entryOrId === "object") {
      setReviewRequestReadState(entryOrId.resourceType, entryOrId.sourceId || entryOrId.id, true);
    }
    persistStore();
    if (entryOrId && typeof entryOrId === "object" && entryOrId.source === "review-request") {
      await markNotificationRead({
        resourceType: entryOrId.resourceType,
        resourceId: entryOrId.sourceId || entryOrId.id,
      }).catch(() => {
        refreshNotifications();
      });
    }
  }

  async function markEntryUnread(entryOrId, resourceType = "") {
    const key = getEntryReadKey(entryOrId, resourceType);
    store.readIds.delete(key);
    if (entryOrId && typeof entryOrId === "object") {
      setReviewRequestReadState(entryOrId.resourceType, entryOrId.sourceId || entryOrId.id, false);
    }
    persistStore();
    if (entryOrId && typeof entryOrId === "object" && entryOrId.source === "review-request") {
      await markNotificationUnread({
        resourceType: entryOrId.resourceType,
        resourceId: entryOrId.sourceId || entryOrId.id,
      }).catch(() => {
        refreshNotifications();
      });
    }
  }

  async function markAllRead() {
    inboxEntries.value.forEach((e) => {
      store.readIds.add(getEntryReadKey(e));
      if (e.source === "review-request") {
        setReviewRequestReadState(e.resourceType, e.sourceId || e.id, true);
      }
    });
    persistStore();
    await markAllNotificationsRead().catch(() => {
      refreshNotifications();
    });
    await loadInboxPage({ page: 1, category: store.inboxCategory, search: store.inboxSearch, append: false });
  }

  const totalUnreadCount = computed(() => {
    const serverUnread = Number(store.inboxCategoryCounts?.unread ?? store.inboxStats?.unread ?? 0);
    const localUnread = visibleNotifications.value.filter(
      (item) => !store.readIds.has(getEntryReadKey(item)),
    ).length;
    return serverUnread + localUnread;
  });

  const unreadEntries = computed(() =>
    inboxEntries.value.filter((e) => !store.readIds.has(getEntryReadKey(e))),
  );

  async function findPendingAchievementReview(recordId, category) {
    if (!recordId) {
      return null;
    }
    const response = await findPendingAchievementReviewApi(recordId, category).catch((error) => error?.response);
    if (!response || response.status === 204 || !response.data) {
      return null;
    }
    return mergeRequestWithDetail(summaryToReviewRequest(response.data));
  }

  async function refreshNotifications() {
    store.detailCache = {};
    await Promise.all([
      loadInboxPage({ page: 1, category: store.inboxCategory, search: store.inboxSearch, append: false }),
      loadInboxStats("inbox"),
      loadInboxPage({ page: 1, category: store.classReviewCategory, scope: "class-reviews", append: false }),
    ]).catch(() => {});
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
    notifications: visibleNotifications,
    hasPendingProfileReviewRequest,
    findPendingAchievementReview,
    fetchReviewRequestDetail,
    detailLoadingKey: computed(() => store.detailLoadingKey),
    loadInboxPage,
    loadInboxStats,
    inboxPage: computed(() => store.inboxPage),
    inboxLoading: computed(() => store.inboxLoading),
    inboxHasMore,
    classReviewPage: computed(() => store.classReviewPage),
    classReviewLoading: computed(() => store.classReviewLoading),
    classReviewHasMore,
    classReviewCategoryCounts: computed(() => store.classReviewCategoryCounts),
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
    getEntryReadKey,
    classReviewEntries,
  };
}
