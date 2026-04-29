import * as XLSX from "xlsx";
import { listAchievements } from "../api/achievement";

export const exportGroups = [
  {
    id: "base",
    label: "基础信息",
    fields: [
      { key: "name", label: "姓名", defaultSelected: true },
      { key: "className", label: "班级", defaultSelected: true },
      { key: "studentNo", label: "学号", defaultSelected: true },
    ],
  },
  {
    id: "school",
    label: "学籍信息",
    fields: [
      { key: "enrollmentDate", label: "入学时间" },
      { key: "studentCategory", label: "学生类别" },
      { key: "classTeacher", label: "班主任" },
      { key: "counselor", label: "辅导员" },
    ],
  },
  {
    id: "identity",
    label: "个人证件与联系方式",
    fields: [
      { key: "ethnicity", label: "民族" },
      { key: "politicalStatus", label: "政治面貌" },
      { key: "phone", label: "手机号码" },
      { key: "backupContact", label: "备用联系方式（QQ/邮箱）" },
      { key: "idNo", label: "身份证号" },
      { key: "nativePlace", label: "籍贯" },
      { key: "address", label: "住址" },
    ],
  },
  {
    id: "dorm",
    label: "住宿信息",
    fields: [
      { key: "offCampusLiving", label: "是否在外居住" },
      { key: "offCampusAddress", label: "外居住地址" },
      { key: "dormCampus", label: "住宿校区" },
      { key: "dormBuilding", label: "住宿楼栋" },
      { key: "dormRoom", label: "住宿房间" },
    ],
  },
  {
    id: "family",
    label: "家庭信息",
    fields: [
      { key: "fatherName", label: "父亲姓名" },
      { key: "fatherPhone", label: "父亲电话" },
      { key: "fatherWorkUnit", label: "父亲工作单位" },
      { key: "fatherTitle", label: "父亲职务" },
      { key: "motherName", label: "母亲姓名" },
      { key: "motherPhone", label: "母亲电话" },
      { key: "motherWorkUnit", label: "母亲工作单位" },
      { key: "motherTitle", label: "母亲职务" },
      { key: "isHk", label: "香港" },
      { key: "isMo", label: "澳门" },
      { key: "isTw", label: "台湾" },
      { key: "specialStudent", label: "特殊学生" },
    ],
  },
  {
    id: "emergency",
    label: "紧急联系人",
    fields: [
      { key: "emergencyPhone", label: "紧急联系人电话" },
      { key: "emergencyRelation", label: "紧急联系人关系" },
    ],
  },
  {
    id: "education",
    label: "教育经历",
    fields: [
      { key: "eduPeriod", label: "时间段" },
      { key: "eduSchoolName", label: "学校名称" },
      { key: "eduEducationLevel", label: "学历" },
      { key: "eduWitness", label: "证明人" },
    ],
  },
  {
    id: "party",
    label: "团组织与入党信息",
    fields: [
      { key: "leagueJoined", label: "是否入团" },
      { key: "leagueApplicationDate", label: "提交入团申请书时间" },
      { key: "leagueJoinDate", label: "入团时间" },
      { key: "leagueNo", label: "团号" },
      { key: "partyApplied", label: "是否申请入党" },
      { key: "applicationDate", label: "提交入党申请书时间" },
      { key: "activistDate", label: "确定积极分子时间" },
      { key: "partyTrainingDate", label: "上党课时间" },
      { key: "developmentTargetDate", label: "确定发展对象时间" },
      { key: "probationaryMemberDate", label: "接收为预备党员时间" },
      { key: "fullMemberDate", label: "转为正式党员时间" },
    ],
  },
  {
    id: "achievement",
    label: "个人成就",
    fields: [
      { key: "ach_contest", label: "学科竞赛、文体艺术" },
      { key: "ach_paper", label: "发表学术论文" },
      { key: "ach_journal", label: "发表期刊作品" },
      { key: "ach_patent", label: "专利（著作权）授权数（项）" },
      { key: "ach_certificate", label: "职业资格证书" },
      { key: "ach_research", label: "学生参与教师科研项目情况" },
      { key: "ach_works", label: "创作、表演的代表性作品" },
    ],
  },
];

export const familyRows = (() => {
  const group = exportGroups.find((item) => item.id === "family");
  if (!group) {
    return [];
  }
  const byKey = Object.fromEntries(group.fields.map((field) => [field.key, field]));
  return [
    ["fatherName", "fatherPhone", "fatherWorkUnit", "fatherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["motherName", "motherPhone", "motherWorkUnit", "motherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["isHk", "isMo", "isTw", "specialStudent"].map((key) => byKey[key]).filter(Boolean),
  ];
})();

const IDENTITY_FIELDS = [
  {
    key: "name",
    label: "姓名",
    getter: (item) => item.fullName || item.name || "",
  },
  {
    key: "className",
    label: "班级",
    getter: (item) => buildClassName(item) || "",
  },
  { key: "studentNo", label: "学号", getter: (item) => item.studentNo || "" },
];

const IDENTITY_KEYS = IDENTITY_FIELDS.map((field) => field.key);

const MAIN_FIELD_ORDER = [
  "name",
  "className",
  "studentNo",
  "enrollmentDate",
  "studentCategory",
  "classTeacher",
  "counselor",
  "ethnicity",
  "politicalStatus",
  "phone",
  "backupContact",
  "idNo",
  "nativePlace",
  "address",
  "offCampusLiving",
  "offCampusAddress",
  "dormCampus",
  "dormBuilding",
  "dormRoom",
  "fatherName",
  "fatherPhone",
  "fatherWorkUnit",
  "fatherTitle",
  "motherName",
  "motherPhone",
  "motherWorkUnit",
  "motherTitle",
  "isHk",
  "isMo",
  "isTw",
  "specialStudent",
  "emergencyPhone",
  "emergencyRelation",
];

const MAIN_FIELD_META = {
  name: { label: "姓名", getter: (item) => item.fullName || item.name || "" },
  className: { label: "班级", getter: (item) => buildClassName(item) || "" },
  studentNo: { label: "学号", getter: (item) => item.studentNo || "" },
  enrollmentDate: {
    label: "入学时间",
    getter: (item) => item.enrollmentDate || "",
  },
  studentCategory: {
    label: "学生类别",
    getter: (item) => item.studentCategory || "",
  },
  classTeacher: { label: "班主任", getter: (item) => item.classTeacher || "" },
  counselor: { label: "辅导员", getter: (item) => item.counselor || "" },
  ethnicity: { label: "民族", getter: (item) => item.ethnicity || "" },
  politicalStatus: {
    label: "政治面貌",
    getter: (item) => item.politicalStatus || "未填写",
  },
  phone: { label: "手机号码", getter: (item) => item.phone || "" },
  backupContact: {
    label: "备用联系方式（QQ/邮箱）",
    getter: (item) => item.backupContact || "",
  },
  idNo: { label: "身份证号", getter: (item) => item.idNo || "" },
  nativePlace: { label: "籍贯", getter: (item) => item.nativePlace || "" },
  address: { label: "住址", getter: (item) => item.address || "" },
  offCampusLiving: {
    label: "是否在外居住",
    getter: (item) => formatYesNo(item.offCampusLiving),
  },
  offCampusAddress: {
    label: "外居住地址",
    getter: (item) => item.offCampusAddress || "",
  },
  dormCampus: { label: "住宿校区", getter: (item) => item.dormCampus || "" },
  dormBuilding: {
    label: "住宿楼栋",
    getter: (item) => item.dormBuilding || "",
  },
  dormRoom: { label: "住宿房间", getter: (item) => item.dormRoom || "" },
  isHk: { label: "香港", getter: (item) => (item.isHk ? "是" : "否") },
  isMo: { label: "澳门", getter: (item) => (item.isMo ? "是" : "否") },
  isTw: { label: "台湾", getter: (item) => (item.isTw ? "是" : "否") },
  specialStudent: {
    label: "特殊学生",
    getter: (item) => (item.specialStudent ? "是" : "否"),
  },
  fatherName: { label: "父亲姓名", getter: (item) => item.fatherName || "" },
  fatherPhone: { label: "父亲电话", getter: (item) => item.fatherPhone || "" },
  fatherWorkUnit: {
    label: "父亲工作单位",
    getter: (item) => item.fatherWorkUnit || "",
  },
  fatherTitle: { label: "父亲职务", getter: (item) => item.fatherTitle || "" },
  motherName: { label: "母亲姓名", getter: (item) => item.motherName || "" },
  motherPhone: { label: "母亲电话", getter: (item) => item.motherPhone || "" },
  motherWorkUnit: {
    label: "母亲工作单位",
    getter: (item) => item.motherWorkUnit || "",
  },
  motherTitle: { label: "母亲职务", getter: (item) => item.motherTitle || "" },
  emergencyPhone: {
    label: "紧急联系人电话",
    getter: (item) => item.emergencyPhone || "",
  },
  emergencyRelation: {
    label: "紧急联系人关系",
    getter: (item) => item.emergencyRelation || "",
  },
};

const EDUCATION_FIELD_ORDER = [
  "eduPeriod",
  "eduSchoolName",
  "eduEducationLevel",
  "eduWitness",
];

const EDUCATION_FIELD_META = {
  eduPeriod: {
    label: "时间段",
    getter: (item, edu) => {
      const start = edu?.startDate || "";
      const end = edu?.isCurrent ? "至今" : edu?.endDate || "";
      return [start, end].filter(Boolean).join("~");
    },
  },
  eduSchoolName: {
    label: "学校名称",
    getter: (item, edu) => edu?.schoolName || "",
  },
  eduEducationLevel: {
    label: "学历",
    getter: (item, edu) => edu?.educationLevel || "",
  },
  eduWitness: {
    label: "证明人",
    getter: (item, edu) => edu?.witness || "",
  },
};

const PARTY_FIELD_ORDER = [
  "leagueJoined",
  "leagueApplicationDate",
  "leagueJoinDate",
  "leagueNo",
  "partyApplied",
  "applicationDate",
  "activistDate",
  "partyTrainingDate",
  "developmentTargetDate",
  "probationaryMemberDate",
  "fullMemberDate",
];

const PARTY_FIELD_META = {
  leagueJoined: {
    label: "是否入团",
    getter: (item) => formatYesNo(item.leagueJoined),
  },
  leagueApplicationDate: {
    label: "提交入团申请书时间",
    getter: (item) => item.leagueApplicationDate || "",
  },
  leagueJoinDate: {
    label: "入团时间",
    getter: (item) =>
      formatDateOrEmpty(item.leagueJoinDate, item.leagueDeveloping, "正在发展"),
  },
  leagueNo: { label: "团号", getter: (item) => item.leagueNo || "" },
  partyApplied: {
    label: "是否申请入党",
    getter: (item) => formatYesNo(item.partyApplied),
  },
  applicationDate: {
    label: "提交入党申请书时间",
    getter: (item) => item.applicationDate || "",
  },
  activistDate: {
    label: "确定积极分子时间",
    getter: (item) =>
      formatDateOrEmpty(item.activistDate, item.activistDeveloping, "正在发展"),
  },
  partyTrainingDate: {
    label: "上党课时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.partyTrainingDate,
        item.partyTrainingPending,
        "暂未报名",
      ),
  },
  developmentTargetDate: {
    label: "确定发展对象时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.developmentTargetDate,
        item.developmentTargetDeveloping,
        "正在发展",
      ),
  },
  probationaryMemberDate: {
    label: "接收为预备党员时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.probationaryMemberDate,
        item.probationaryDeveloping,
        "正在发展",
      ),
  },
  fullMemberDate: {
    label: "转为正式党员时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.fullMemberDate,
        item.fullMemberDeveloping,
        "正在发展",
      ),
  },
};

const ACHIEVEMENT_CATEGORIES = [
  { key: "contest", label: "学科竞赛、文体艺术", selectKey: "ach_contest" },
  { key: "paper", label: "发表学术论文", selectKey: "ach_paper" },
  { key: "journal", label: "发表期刊作品", selectKey: "ach_journal" },
  {
    key: "patent",
    label: "专利（著作权）授权数（项）",
    selectKey: "ach_patent",
  },
  { key: "certificate", label: "职业资格证书", selectKey: "ach_certificate" },
  {
    key: "research",
    label: "学生参与教师科研项目情况",
    selectKey: "ach_research",
  },
  { key: "works", label: "创作、表演的代表性作品", selectKey: "ach_works" },
];

const ACHIEVEMENT_FIELDS = {
  certificate: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "certificateType", label: "证书类型" },
    { key: "certificateName", label: "证书名称" },
    { key: "obtainDate", label: "获得日期" },
  ],
  contest: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "contestName", label: "竞赛名称" },
    { key: "organizer", label: "主办方" },
    { key: "contestCategory", label: "竞赛类别" },
    { key: "awardCategory", label: "奖项类别" },
    { key: "awardLevel", label: "奖项等级" },
    { key: "contestType", label: "竞赛类型" },
    { key: "awardDate", label: "获奖日期" },
    { key: "awardCount", label: "获奖人数/数量" },
    { key: "teamMembers", label: "团队成员" },
    { key: "instructors", label: "指导教师" },
    { key: "remark", label: "备注" },
  ],
  journal: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "workTitle", label: "作品题目" },
    { key: "publicationName", label: "刊物名称" },
    { key: "publishDate", label: "发表日期" },
  ],
  paper: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "paperTitle", label: "论文题目" },
    { key: "journalName", label: "期刊名称" },
    { key: "publishDate", label: "发表日期" },
    { key: "authorOrder", label: "作者排序" },
    { key: "indexed", label: "收录情况" },
  ],
  patent: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "patentName", label: "专利名称" },
    { key: "patentType", label: "专利类型" },
    { key: "grantNo", label: "授权号" },
    { key: "grantDate", label: "授权日期" },
    { key: "firstInventor", label: "第一发明人" },
  ],
  research: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "projectName", label: "项目名称" },
    { key: "teacherNo", label: "教师工号" },
    { key: "projectLeader", label: "项目负责人" },
  ],
  works: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "workName", label: "作品名称" },
    { key: "workCategory", label: "作品类别" },
    { key: "workType", label: "作品类型" },
    { key: "publishDate", label: "发布日期" },
    { key: "publishOccasion", label: "发布场合" },
    { key: "organizer", label: "主办方" },
    { key: "impactScope", label: "影响范围" },
    { key: "note", label: "备注说明" },
  ],
};

export function createExportSelections() {
  const selections = {};
  exportGroups.forEach((group) => {
    group.fields.forEach((field) => {
      selections[field.key] = true;
    });
  });
  return selections;
}

export function getSelectedExportKeys(selections) {
  return new Set(
    Object.entries(selections)
      .filter(([, value]) => Boolean(value))
      .map(([key]) => key),
  );
}

export async function fetchAchievementsForStudents(rows) {
  const tasks = rows.map((item) => {
    const studentNo = item.studentNo || "";
    const studentName = item.fullName || item.name || "";
    if (!studentNo && !studentName) {
      return Promise.resolve({ studentNo, studentName, records: [] });
    }
    return listAchievements({ studentNo, studentName })
      .then(({ data }) => ({
        studentNo,
        studentName,
        records: Array.isArray(data) ? data : [],
      }))
      .catch(() => ({ studentNo, studentName, records: [] }));
  });
  return Promise.all(tasks);
}

export function buildPreviewSheets(rows, selectedKeys, achievementData = []) {
  const sheets = [];
  if (shouldIncludeMainSheet(selectedKeys)) {
    const table = buildStudentTable(rows, selectedKeys);
    if (table) {
      sheets.push({ id: "main", label: "全部", table });
    }
  }
  const educationTable = buildEducationTable(rows, selectedKeys);
  if (educationTable) {
    sheets.push({ id: "education", label: "教育经历", table: educationTable });
  }
  const partyTable = buildPartyTable(rows, selectedKeys);
  if (partyTable) {
    sheets.push({ id: "party", label: "团组织与入党信息", table: partyTable });
  }
  const activeAchievementCategories = ACHIEVEMENT_CATEGORIES.filter((item) =>
    selectedKeys.has(item.selectKey),
  );
  if (activeAchievementCategories.length) {
    const overview = buildAchievementOverview(rows, selectedKeys, achievementData);
    sheets.push({
      id: "achievement-overview",
      label: "成就总览",
      table: overview,
    });
    activeAchievementCategories.forEach((category) => {
      sheets.push({
        id: `achievement-${category.key}`,
        label: category.label,
        table: buildAchievementDetailTable(category.key, achievementData),
      });
    });
  }
  return sheets;
}

export async function exportStudentRowsToExcel(
  rows,
  selectedKeys,
  options = {},
) {
  const { filenamePrefix = "students_export" } = options;
  const workbook = XLSX.utils.book_new();
  const activeAchievementCategories = ACHIEVEMENT_CATEGORIES.filter((item) =>
    selectedKeys.has(item.selectKey),
  );
  const achievementData = activeAchievementCategories.length
    ? await fetchAchievementsForStudents(rows)
    : [];

  if (shouldIncludeMainSheet(selectedKeys)) {
    const table = buildStudentTable(rows, selectedKeys);
    if (table) {
      const worksheet = XLSX.utils.aoa_to_sheet(table);
      worksheet["!cols"] = computeColumnWidths(table);
      XLSX.utils.book_append_sheet(workbook, worksheet, "学生");
    }
  }

  const educationTable = buildEducationTable(rows, selectedKeys);
  if (educationTable) {
    const worksheet = XLSX.utils.aoa_to_sheet(educationTable);
    worksheet["!cols"] = computeColumnWidths(educationTable);
    XLSX.utils.book_append_sheet(workbook, worksheet, "教育经历");
  }

  const partyTable = buildPartyTable(rows, selectedKeys);
  if (partyTable) {
    const worksheet = XLSX.utils.aoa_to_sheet(partyTable);
    worksheet["!cols"] = computeColumnWidths(partyTable);
    XLSX.utils.book_append_sheet(workbook, worksheet, "团组织与入党信息");
  }

  if (activeAchievementCategories.length) {
    const overview = buildAchievementOverview(rows, selectedKeys, achievementData);
    const overviewSheet = XLSX.utils.aoa_to_sheet(overview);
    const baseFieldCount = IDENTITY_FIELDS.filter((field) =>
      selectedKeys.has(field.key),
    ).length;
    activeAchievementCategories.forEach((category, index) => {
      const cell = XLSX.utils.encode_cell({
        r: 0,
        c: baseFieldCount + index,
      });
      if (overviewSheet[cell]) {
        overviewSheet[cell].l = {
          Target: `#'${category.label}'!A1`,
        };
      }
    });
    overviewSheet["!cols"] = computeColumnWidths(overview);
    XLSX.utils.book_append_sheet(workbook, overviewSheet, "成就总览");

    activeAchievementCategories.forEach((category) => {
      const detailTable = buildAchievementDetailTable(category.key, achievementData);
      const detailSheet = XLSX.utils.aoa_to_sheet(detailTable);
      detailSheet["!cols"] = computeColumnWidths(detailTable);
      XLSX.utils.book_append_sheet(workbook, detailSheet, category.label);
    });
  }

  XLSX.writeFile(workbook, `${filenamePrefix}_${formatTimestamp()}.xlsx`, {
    compression: true,
  });
}

function shouldIncludeMainSheet(selectedKeys) {
  const hasEdu = EDUCATION_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasParty = PARTY_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasAchievement = ACHIEVEMENT_CATEGORIES.some((item) =>
    selectedKeys.has(item.selectKey),
  );
  const hasNonBaseMain = MAIN_FIELD_ORDER.some(
    (key) => !IDENTITY_KEYS.includes(key) && selectedKeys.has(key),
  );
  const hasAnyMain = MAIN_FIELD_ORDER.some((key) => selectedKeys.has(key));
  if (hasEdu || hasParty || hasAchievement) {
    return hasNonBaseMain;
  }
  return hasAnyMain;
}

function buildStudentTable(rows, selectedKeys) {
  const activeFields = MAIN_FIELD_ORDER.filter((key) => selectedKeys.has(key));
  if (!activeFields.length) {
    return null;
  }
  const header = activeFields.map((key) => MAIN_FIELD_META[key].label);
  const body = rows.map((item) =>
    activeFields.map((key) => MAIN_FIELD_META[key].getter(item)),
  );
  return [header, ...body];
}

function buildEducationTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = EDUCATION_FIELD_ORDER.filter((key) =>
    selectedKeys.has(key),
  );
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => EDUCATION_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    const identityValues = activeIdentity.map((field) => field.getter(item));
    const experiences = Array.isArray(item.educationExperiences)
      ? item.educationExperiences.filter(Boolean)
      : [];
    if (!experiences.length) {
      lines.push([...identityValues, ...activeFields.map(() => "")]);
      return;
    }
    experiences.forEach((edu) => {
      lines.push([
        ...identityValues,
        ...activeFields.map((key) => EDUCATION_FIELD_META[key].getter(item, edu)),
      ]);
    });
  });
  return lines;
}

function buildPartyTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = PARTY_FIELD_ORDER.filter((key) => selectedKeys.has(key));
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => PARTY_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    lines.push([
      ...activeIdentity.map((field) => field.getter(item)),
      ...activeFields.map((key) => PARTY_FIELD_META[key].getter(item)),
    ]);
  });
  return lines;
}

function buildAchievementOverview(rows, selectedKeys, achievementData) {
  const baseFields = IDENTITY_FIELDS.filter((field) => selectedKeys.has(field.key));
  const activeCategories = ACHIEVEMENT_CATEGORIES.filter((item) =>
    selectedKeys.has(item.selectKey),
  );
  const header = [
    ...baseFields.map((field) => field.label),
    ...activeCategories.map((item) => item.label),
  ];
  const body = rows.map((item) => {
    const baseValues = baseFields.map((field) => field.getter(item));
    const studentNo = item.studentNo || "";
    const studentName = item.fullName || item.name || "";
    const recordEntry = achievementData.find(
      (entry) =>
        (studentNo && entry.studentNo === studentNo) ||
        (studentName && entry.studentName === studentName),
    );
    const records = recordEntry?.records || [];
    const categoryFlags = activeCategories.map((category) => {
      const count = records.filter((record) => record.category === category.key).length;
      return count > 0 ? String(count) : "";
    });
    return [...baseValues, ...categoryFlags];
  });
  return [header, ...body];
}

function buildAchievementDetailTable(categoryKey, achievementData) {
  const fields = ACHIEVEMENT_FIELDS[categoryKey] || [];
  const header = fields.map((field) => field.label);
  const rows = [];
  achievementData.forEach((entry) => {
    entry.records
      .filter((record) => record.category === categoryKey)
      .forEach((record) => {
        rows.push(fields.map((field) => record.fields?.[field.key] || ""));
      });
  });
  return [header, ...rows];
}

function computeColumnWidths(table) {
  const widths = [];
  table.forEach((row) => {
    row.forEach((cell, index) => {
      const text = cell == null ? "" : String(cell);
      const width = Math.min(Math.max(text.length + 2, 8), 40);
      if (!widths[index] || width > widths[index].wch) {
        widths[index] = { wch: width };
      }
    });
  });
  return widths;
}

function buildClassName(item) {
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

function formatDateOrEmpty(dateValue, statusFlag, statusText) {
  if (statusFlag) {
    return statusText;
  }
  return dateValue || "";
}

function formatYesNo(value) {
  if (value === true) {
    return "是";
  }
  if (value === false) {
    return "否";
  }
  return "";
}

function formatTimestamp() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  return [
    now.getFullYear(),
    pad(now.getMonth() + 1),
    pad(now.getDate()),
  ].join("") + `_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
}
