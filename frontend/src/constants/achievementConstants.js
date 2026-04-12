/**
 * achievementConstants - 成就相关常量
 *
 * categoryFieldMap、achievementEntries、categoryHints、ATTACHMENT_TYPE_META、
 * attachmentIconMap、IMAGE_URLS_FIELD、ATTACHMENTS_FIELD
 */

export const IMAGE_URLS_FIELD = "_imageUrls";
export const ATTACHMENTS_FIELD = "_attachments";

export const ATTACHMENT_TYPE_META = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

export const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
  { key: "doubleHundred", label: "双百工程" },
  { key: "ieerTraining", label: "大学生创新创业训练计划项目" },
];

export const categoryHints = {
  contest: [
    '学科竞赛获奖指本科生校级及以上竞赛类活动获奖。其中省级以上统计范围为：全国大学生电子设计竞赛、全国大学生电子设计竞赛嵌入式专题竞赛、全国大学生数学建模竞赛、全国大学生广告艺术设计大赛、全国大学生英语竞赛、全国大学生英语演讲竞赛、全国大学生化学实验竞赛、全国大学生电子商务竞赛、全国大学生机械创新设计大赛、全国周培源大学生力学竞赛、全国大学生结构设计竞赛、"挑战杯"全国大学生科技作品竞赛、"挑战杯"全国大学生创业计划大赛、美国数学模型竞赛（MCM）、美国大学生程序设计竞赛（ACM）、国际大学生机械设计竞赛、全国临床技能大赛及其他具有全球影响和全国影响的比赛等。',
    "文体艺术获奖指本科生在国内外及省、部级等文艺、体育竞赛中获得的奖项数。切勿与前项学科竞赛获奖重复。",
    '"竞赛名称"填写须完整、规范，如：第十三届"挑战杯"中国大学生创业计划竞赛，第十五届大学生科技学术季活动之广东大学生社会治理调研大赛。',
    '主办单位：以活动通知和荣誉证书上的记载为准。填写单位规范全称，多个单位联合主办的，填写"第一主办单位名称+等"，如"共青团广东省委员会等"。',
    '获奖时间具体到月，如"2024年10月"。',
    "获奖类别：限选国家级、省部级、校级。国际级竞赛等同于国家级，全国性行业协会主办赛事等同省部级。",
    "获奖等级：指特等奖、一等奖、二等奖、三等奖；冠军、亚军、季军；金奖、银奖、铜奖。",
    '竞赛类型："互联网+"创新创业大赛、挑战杯、创青春中国青年创新创业大赛、其他。',
    "指导老师：以正式获奖通知文件或荣誉证书记载为准，指导老师多人的，全部填写。比赛未设指导老师或无指导老师的，可不填。",
    "备注：团体、个人。",
  ],
  paper: [
    "发表时间具体到月，如 2024年10月 。",
    "发表期刊如是论文集，需在刊物名称中括号备注。",
    "佐证材料需提供：(1)期刊封面、目录、论文全文或见刊证明、论文全文；(2)已被权威数据库收录的需提供检索证明。",
  ],
  journal: [
    "发表时间具体到月，如 2024年10月 。",
    "作品指在校本科生在国内外正式出版刊物或重大活动上以第一作者发表作品的数量（例如：诗歌、散文、小说等）。",
  ],
  patent: [
    "发表时间具体到月，如“2024年10月”。",
    "专利类别指发明专利、实用新型专利、外观设计专利、软件著作权。",
  ],
  certificate: [
    "发表时间具体到月，如“2024年10月”",
    "证书类别指包括专业技术人员职业资格、技能人员职业资格。",
    "证书指在人力资源社会保障部公布的《国家职业资格目录（2021年版）》内的职业资格证。",
  ],
  research: [
    "发表时间具体到月，如“2024年10月”。",
    "参与科研项目指本科生参加的各类教师主持的国家、省部纵向项目，以及学校科技管理部门科研考核统计的横向项目（自然年内在研项目）。",
  ],
  works: [
    "发表时间具体到月，如“2024年10月”。",
    "类别：理论类、创作类、表演类；",
    "类型:指大型作品、中型作品、小型作品。其中大型作品、中型作品、小型作品的划分，依据音乐、戏剧、影视类作品的规模（包括作品时长、技术含量、参与程度等）。",
    "影响范围：指全国（含国际）、区域、省内。",
  ],
  doubleHundred: ["请填写双百工程的具体参与内容"],
  ieerTraining: ["请填写项目名称和项目级别", "请说明在项目中的具体工作"],
};

export const categoryFieldMap = {
  contest: {
    titleKey: "contestName",
    dateKey: "awardDate",
    noteKey: "remark",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "contestName",
        label: "竞赛名称",
        kind: "input",
        placeholder: "请输入竞赛名称",
      },
      {
        key: "organizer",
        label: "主办单位",
        kind: "input",
        placeholder: "请输入主办单位",
      },
      {
        key: "contestCategory",
        label: "竞赛类别",
        kind: "input",
        placeholder: "国家级/省部级/校级",
      },
      {
        key: "awardCategory",
        label: "获奖类别",
        kind: "input",
        placeholder: "国家级/省部级/校级",
      },
      {
        key: "awardLevel",
        label: "获奖等级",
        kind: "input",
        placeholder: "特等奖/一等奖/二等奖/三等奖",
      },
      {
        key: "contestType",
        label: "竞赛类型",
        kind: "input",
        placeholder: "互联网+/挑战杯/创青春/其他",
      },
      { key: "awardDate", label: "获奖时间", kind: "input", type: "date" },
      {
        key: "awardCount",
        label: "获奖人数",
        kind: "input",
        placeholder: "请输入获奖人数",
      },
      {
        key: "teamMembers",
        label: "团队其他成员",
        kind: "input",
        placeholder: "按证书顺序填写",
      },
      {
        key: "instructors",
        label: "指导老师",
        kind: "input",
        placeholder: "可填写多名老师",
      },
      { key: "remark", label: "备注", kind: "input", placeholder: "团体/个人" },
    ],
  },
  paper: {
    titleKey: "paperTitle",
    dateKey: "publishDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "paperTitle",
        label: "论文名称",
        kind: "input",
        placeholder: "请输入论文名称",
      },
      {
        key: "journalName",
        label: "刊物名称",
        kind: "input",
        placeholder: "请输入刊物名称",
      },
      {
        key: "authorOrder",
        label: "作者顺序",
        kind: "input",
        placeholder: "如：1/2/3…",
      },
      { key: "publishDate", label: "发表时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  journal: {
    titleKey: "publicationName",
    dateKey: "publishDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "publicationName",
        label: "作品名称",
        kind: "input",
        placeholder: "请输入作品名称",
      },
      { key: "publishDate", label: "发表时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  patent: {
    titleKey: "patentName",
    dateKey: "grantDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "patentName",
        label: "专利名称",
        kind: "input",
        placeholder: "请输入专利名称",
      },
      {
        key: "patentType",
        label: "专利类型",
        kind: "input",
        placeholder: "发明专利/实用新型/外观设计",
      },
      {
        key: "grantNo",
        label: "授权号",
        kind: "input",
        placeholder: "请输入授权号",
      },
      {
        key: "firstInventor",
        label: "第一发明人",
        kind: "input",
        placeholder: "请输入第一发明人",
      },
      { key: "grantDate", label: "获批时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  certificate: {
    titleKey: "certificateName",
    dateKey: "obtainDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "certificateName",
        label: "证书名称",
        kind: "input",
        placeholder: "请输入证书名称",
      },
      {
        key: "certificateType",
        label: "证书类别",
        kind: "input",
        placeholder: "请输入证书类别",
      },
      { key: "obtainDate", label: "获得时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  research: {
    titleKey: "projectName",
    dateKey: "projectStartDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "projectName",
        label: "项目名称",
        kind: "input",
        placeholder: "请输入项目名称",
      },
      {
        key: "projectLevel",
        label: "项目级别",
        kind: "input",
        placeholder: "国家级/省级/校级",
      },
      {
        key: "projectRole",
        label: "项目角色",
        kind: "input",
        placeholder: "项目负责人/核心成员",
      },
      {
        key: "projectLeader",
        label: "项目负责人",
        kind: "input",
        placeholder: "请输入项目负责人",
      },
      {
        key: "teacherNo",
        label: "指导教师工号",
        kind: "input",
        placeholder: "请输入指导教师工号",
      },
      {
        key: "projectStartDate",
        label: "开始时间",
        kind: "input",
        type: "date",
      },
      { key: "projectEndDate", label: "结束时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  works: {
    titleKey: "workTitle",
    dateKey: "publishDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "workTitle",
        label: "作品名称",
        kind: "input",
        placeholder: "请输入作品名称",
      },
      {
        key: "workCategory",
        label: "作品类别",
        kind: "input",
        placeholder: "文学/艺术/科技…",
      },
      { key: "publishDate", label: "完成时间", kind: "input", type: "date" },
      {
        key: "publishOccasion",
        label: "发表/展出平台",
        kind: "input",
        placeholder: "请输入发表或展出平台",
      },
      {
        key: "organizer",
        label: "主办单位",
        kind: "input",
        placeholder: "请输入主办单位",
      },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  doubleHundred: {
    titleKey: "activityName",
    dateKey: "activityDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "activityName",
        label: "活动名称",
        kind: "input",
        placeholder: "请输入活动名称",
      },
      { key: "activityDate", label: "参与时间", kind: "input", type: "date" },
      {
        key: "organizer",
        label: "主办单位",
        kind: "input",
        placeholder: "请输入主办单位",
      },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
  ieerTraining: {
    titleKey: "projectName",
    dateKey: "projectStartDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "projectName",
        label: "项目名称",
        kind: "input",
        placeholder: "请输入项目名称",
      },
      {
        key: "projectLevel",
        label: "项目级别",
        kind: "input",
        placeholder: "国家级/省级/校级",
      },
      {
        key: "projectRole",
        label: "项目角色",
        kind: "input",
        placeholder: "项目负责人/核心成员",
      },
      {
        key: "projectStartDate",
        label: "开始时间",
        kind: "input",
        type: "date",
      },
      { key: "projectEndDate", label: "结束时间", kind: "input", type: "date" },
      {
        key: "remark",
        label: "备注",
        kind: "input",
        placeholder: "请输入备注",
      },
    ],
  },
};

export const attachmentIconMap = {
  doc: "/assets/icons/doc.svg",
  docx: "/assets/icons/doc.svg",
  pdf: "/assets/icons/pdf.svg",
  xls: "/assets/icons/excel.svg",
  xlsx: "/assets/icons/excel.svg",
  ppt: "/assets/icons/ppt.svg",
  pptx: "/assets/icons/ppt.svg",
  zip: "/assets/icons/zip.svg",
  rar: "/assets/icons/zip.svg",
  "7z": "/assets/icons/zip.svg",
  mp4: "/assets/icons/video.svg",
  mov: "/assets/icons/video.svg",
  jpeg: "/assets/icons/image.svg",
  jpg: "/assets/icons/image.svg",
  png: "/assets/icons/image.svg",
  heif: "/assets/icons/image.svg",
};
