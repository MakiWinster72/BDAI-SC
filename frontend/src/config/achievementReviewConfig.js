/**
 * 个人成果保存策略：仅由后台「审核策略设置」决定，与后端 AchievementService 一致。
 *
 * - 开启审核：学生/干部 → 提交审核申请（按钮「请求审核」）
 * - 关闭审核：学生/干部 → 直接保存（按钮「保存」）
 * - 教师/管理员：始终可直接保存
 */
export function checkAchievementSaveRequiresReview(
  achievementReviewEnabled,
  role,
) {
  if (!achievementReviewEnabled) {
    return false;
  }
  return role !== "ADMIN" && role !== "TEACHER";
}

/** 成果编辑页保存按钮文案 */
export function getAchievementSaveActionLabel(
  achievementReviewEnabled,
  role,
) {
  return checkAchievementSaveRequiresReview(achievementReviewEnabled, role)
    ? "请求审核"
    : "保存";
}
