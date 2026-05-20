export function buildStudentClassName(item) {
  if (!item) {
    return "";
  }
  if (item.className) {
    return item.className;
  }
  const safeYear = item.classYear ? `${item.classYear}级` : "";
  const safeMajor = item.classMajor || "";
  const safeNo = item.classNo ? `${item.classNo}班` : "";
  return `${safeYear}${safeMajor}${safeNo}`.trim();
}

export function mapStudentListItem(item) {
  return {
    id: item.id,
    name: item.fullName || "未命名",
    avatarUrl: item.avatarUrl || "",
    className: buildStudentClassName(item),
    gradeYear: item.classYear || "",
    college: item.college || "",
    major: item.classMajor || "",
    classNo: item.classNo || "",
    studentNo: item.studentNo || "",
    isHk: item.isHk || false,
    isMo: item.isMo || false,
    isTw: item.isTw || false,
    specialStudent: item.specialStudent || false,
    specialStudentType: item.specialStudentType || "",
  };
}

export function getHkMoTwLabel(item) {
  const parts = [];
  if (item.isHk) {
    parts.push("香港");
  }
  if (item.isMo) {
    parts.push("澳门");
  }
  if (item.isTw) {
    parts.push("台湾");
  }
  return parts.join(" / ");
}
