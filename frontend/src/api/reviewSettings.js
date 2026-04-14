import request from "./request";

export function getReviewSettings() {
  return request.get("/api/settings/review");
}

export function updateReviewSettings(data) {
  return request.put("/api/settings/review", data);
}
