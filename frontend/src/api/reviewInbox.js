import request from "./request";

export function listReviewInbox(params = {}) {
  return request.get("/api/review-inbox", { params });
}

export function getReviewInboxStats(params = {}) {
  return request.get("/api/review-inbox/stats", { params });
}

export function getReviewInboxDetail(resourceType, id) {
  return request.get(`/api/review-inbox/${resourceType}/${id}`);
}

export function findPendingAchievementReview(recordId, category) {
  return request.get("/api/review-inbox/pending-achievement", {
    params: { recordId, category },
  });
}
