import request from "./request";

export function listAchievementReviewRequests() {
  return request.get("/api/achievement-review-requests");
}

export function submitAchievementReviewRequestApi(data) {
  return request.post("/api/achievement-review-requests", data);
}

export function approveAchievementReviewRequest(id) {
  return request.post(`/api/achievement-review-requests/${id}/approve`);
}

export function rejectAchievementReviewRequest(id, data) {
  return request.post(`/api/achievement-review-requests/${id}/reject`, data);
}
