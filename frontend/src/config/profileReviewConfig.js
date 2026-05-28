/**
 * 个人信息保存策略：仅由后台「审核策略设置」中的开关决定，无「首次免审」例外。
 * 与后端 StudentProfileService.ensureDirectProfileWriteAllowed 一致。
 *
 * - 开启审核：学生/干部 → 提交审核申请（按钮「请求审核」）
 * - 关闭审核：学生/干部 → 直接 PUT 保存（按钮「保存」）
 * - 教师/管理员：始终可直接保存
 */
export function checkProfileSaveRequiresReview(profileReviewEnabled, role) {
  if (!profileReviewEnabled) {
    return false;
  }
  return role !== "ADMIN" && role !== "TEACHER";
}

/** 我的信息页保存按钮文案 */
export function getProfileSaveActionLabel(profileReviewEnabled, role) {
  return checkProfileSaveRequiresReview(profileReviewEnabled, role)
    ? "请求审核"
    : "保存";
}
