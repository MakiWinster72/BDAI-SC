import request from "./request";

export function getAchievementUploadSettings() {
  return request.get("/api/settings/achievement-upload");
}

export function updateAchievementUploadSettings(data) {
  return request.put("/api/settings/achievement-upload", data);
}
