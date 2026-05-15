import request from "./request";

export function markNotificationRead(data) {
  return request.post("/api/notification-read-states/read", data);
}

export function markNotificationUnread(data) {
  return request.post("/api/notification-read-states/unread", data);
}

export function markAllNotificationsRead() {
  return request.post("/api/notification-read-states/read-all");
}
