export const FIXED_COLLEGE = "大数据与人工智能学院";

export const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);

export const majorOptionsByCategory = {
  本科生: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程",
    "人工智能",
    "电子商务",
    "电子商务（大数据决策分析）",
    "大数据管理与应用",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
  研究生: [
    "管理科学与工程",
    "技术经济及管理",
    "智能科学与技术",
    "计算机技术",
    "图书情报",
  ],
};

export const studentCategoryOptions = ["本科生", "研究生"];

export const politicalStatusOptions = ["群众", "共青团员", "中共预备党员", "中共党员"];

export const idTypeOptions = [
  "居民身份证",
  "台湾居民来往大陆通行证",
  "港澳居民来往内地通行证",
  "普通护照",
  "台湾居民居住证",
  "港澳居民居住证",
  "外国人永久居留身份证",
  "外国护照",
];

export const dormCampusOptions = ["佛山校区", "广州校区"];

export const specialStudentTypeOptions = [
  { value: "", label: "无" },
  { value: "HIGH_CARE", label: "高关怀" },
  { value: "ECONOMIC_SPECIAL", label: "经济困难>特殊困难" },
  { value: "ECONOMIC_DIFFICULT", label: "经济困难>困难" },
  { value: "ECONOMIC_GENERAL", label: "经济困难>一般困难" },
  { value: "DISABILITY", label: "残疾" },
  { value: "ORPHAN", label: "孤儿" },
  { value: "ACADEMIC_DIFFICULTY", label: "学业困难" },
];
