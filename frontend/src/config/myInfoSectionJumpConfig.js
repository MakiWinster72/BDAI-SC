/** @typedef {{ id: string, label: string }} MyInfoSectionJumpItem */

/** @type {MyInfoSectionJumpItem[]} */
const MY_INFO_SECTION_JUMP_ITEMS = [
  { id: "profile-section-hero", label: "基础信息" },
  { id: "profile-section-school", label: "学籍信息" },
  { id: "profile-section-identity", label: "个人证件与联系方式" },
  { id: "profile-section-dorm", label: "住宿信息" },
  { id: "profile-section-party", label: "团组织与入党信息" },
  { id: "profile-section-education", label: "教育经历" },
  { id: "profile-section-cadre", label: "学生干部经历" },
  { id: "profile-section-family", label: "家庭信息" },
  { id: "profile-section-emergency", label: "紧急联系人（除亲戚外）" },
];

/** @returns {MyInfoSectionJumpItem[]} */
export function getMyInfoSectionJumpItems() {
  return MY_INFO_SECTION_JUMP_ITEMS;
}
