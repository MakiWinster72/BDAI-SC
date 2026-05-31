/** 学生是否须先完成强制改密（仅看账号标记，不看登录历史） */
export function checkStudentMustChangePassword(user) {
  return user?.role === "STUDENT" && Boolean(user?.mustChangePassword);
}
