import request from "./request";

export function listProfileReviewRequests() {
  return request.get("/api/profile-review-requests");
}

export function submitProfileReviewRequestApi(data) {
  return request.post("/api/profile-review-requests", data);
}

export function approveProfileReviewRequest(id) {
  return request.post(`/api/profile-review-requests/${id}/approve`);
}

export function rejectProfileReviewRequest(id, data) {
  return request.post(`/api/profile-review-requests/${id}/reject`, data);
}

export function cancelProfileReviewRequest(id) {
  return request.delete(`/api/profile-review-requests/${id}`);
}

export function setProfileReviewRequestDocuments(id, documents) {
  return request.put(`/api/profile-review-requests/${id}/supporting-documents`, { documents });
}