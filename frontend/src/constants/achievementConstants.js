/**
 * achievementConstants - 成就相关常量
 *
 * categoryFieldMap、achievementEntries、categoryHints、ATTACHMENT_TYPE_META、
 * attachmentIconMap、IMAGE_URLS_FIELD、ATTACHMENTS_FIELD
 */

export const IMAGE_URLS_FIELD = '_imageUrls';
export const ATTACHMENTS_FIELD = '_attachments';

export const ATTACHMENT_TYPE_META = [
  { key: 'document', label: '文档', icon: '/assets/icons/doc.svg' },
  { key: 'image', label: '图片', icon: '/assets/icons/image.svg' },
  { key: 'video', label: '视频', icon: '/assets/icons/video.svg' },
  { key: 'archive', label: '压缩包', icon: '/assets/icons/zip.svg' },
];

export const achievementEntries = [
  { key: 'all', label: '全部' },
  { key: 'contest', label: '学科竞赛、文体艺术' },
  { key: 'paper', label: '发表学术论文' },
  { key: 'journal', label: '发表期刊作品' },
  { key: 'patent', label: '专利(著作权)授权数(项)' },
  { key: 'certificate', label: '职业资格证书' },
  { key: 'research', label: '学生参与教师科研项目情况' },
  { key: 'works', label: '创作、表演的代表性作品' },
  { key: 'doubleHundred', label: '双百工程' },
  { key: 'ieerTraining', label: '大学生创新创业训练计划项目' },
];

export const categoryHints = {
  contest: [
    '请填写具体的竞赛名称（如：第十二届全国大学生数学竞赛）',
    '团队成员请按证书顺序填写',
    '获奖等级填写如：国家级一等奖、省级二等奖',
  ],
  paper: [
    '请填写论文的全称',
    '作者顺序请与论文署名一致',
  ],
  journal: [
    '请填写期刊的全称',
  ],
  patent: [
    '请填写专利名称',
    '发明人顺序请与专利证书一致',
  ],
  certificate: [
    '请填写证书全称',
    '证书编号请与证书一致',
  ],
  research: [
    '请填写项目名称',
    '请填写在项目中的具体职责',
  ],
  works: [
    '请填写作品名称',
    '请说明作品的创作过程和意义',
  ],
  doubleHundred: [
    '请填写双百工程的具体参与内容',
  ],
  ieerTraining: [
    '请填写项目名称和项目级别',
    '请说明在项目中的具体工作',
  ],
};

export const categoryFieldMap = {
  contest: {
    titleKey: 'contestName',
    dateKey: 'awardDate',
    noteKey: 'remark',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'contestName', label: '竞赛名称', kind: 'input', placeholder: '请输入竞赛名称' },
      { key: 'organizer', label: '主办单位', kind: 'input', placeholder: '请输入主办单位' },
      { key: 'contestCategory', label: '竞赛类别', kind: 'input', placeholder: '国家级/省部级/校级' },
      { key: 'awardCategory', label: '获奖类别', kind: 'input', placeholder: '国家级/省部级/校级' },
      { key: 'awardLevel', label: '获奖等级', kind: 'input', placeholder: '特等奖/一等奖/二等奖/三等奖' },
      { key: 'contestType', label: '竞赛类型', kind: 'input', placeholder: '互联网+/挑战杯/创青春/其他' },
      { key: 'awardDate', label: '获奖时间', kind: 'input', type: 'date' },
      { key: 'awardCount', label: '获奖人数', kind: 'input', placeholder: '请输入获奖人数' },
      { key: 'teamMembers', label: '团队其他成员', kind: 'input', placeholder: '按证书顺序填写' },
      { key: 'instructors', label: '指导老师', kind: 'input', placeholder: '可填写多名老师' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '团体/个人' },
    ],
  },
  paper: {
    titleKey: 'paperTitle',
    dateKey: 'publishDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'paperTitle', label: '论文名称', kind: 'input', placeholder: '请输入论文名称' },
      { key: 'journalName', label: '刊物名称', kind: 'input', placeholder: '请输入刊物名称' },
      { key: 'authorOrder', label: '作者顺序', kind: 'input', placeholder: '如：1/2/3…' },
      { key: 'publishDate', label: '发表时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  journal: {
    titleKey: 'publicationName',
    dateKey: 'publishDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'publicationName', label: '作品名称', kind: 'input', placeholder: '请输入作品名称' },
      { key: 'publishDate', label: '发表时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  patent: {
    titleKey: 'patentName',
    dateKey: 'grantDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'patentName', label: '专利名称', kind: 'input', placeholder: '请输入专利名称' },
      { key: 'patentType', label: '专利类型', kind: 'input', placeholder: '发明专利/实用新型/外观设计' },
      { key: 'grantNo', label: '授权号', kind: 'input', placeholder: '请输入授权号' },
      { key: 'firstInventor', label: '第一发明人', kind: 'input', placeholder: '请输入第一发明人' },
      { key: 'grantDate', label: '获批时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  certificate: {
    titleKey: 'certificateName',
    dateKey: 'obtainDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'certificateName', label: '证书名称', kind: 'input', placeholder: '请输入证书名称' },
      { key: 'certificateType', label: '证书类别', kind: 'input', placeholder: '请输入证书类别' },
      { key: 'obtainDate', label: '获得时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  research: {
    titleKey: 'projectName',
    dateKey: 'projectStartDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'projectName', label: '项目名称', kind: 'input', placeholder: '请输入项目名称' },
      { key: 'projectLevel', label: '项目级别', kind: 'input', placeholder: '国家级/省级/校级' },
      { key: 'projectRole', label: '项目角色', kind: 'input', placeholder: '项目负责人/核心成员' },
      { key: 'projectLeader', label: '项目负责人', kind: 'input', placeholder: '请输入项目负责人' },
      { key: 'teacherNo', label: '指导教师工号', kind: 'input', placeholder: '请输入指导教师工号' },
      { key: 'projectStartDate', label: '开始时间', kind: 'input', type: 'date' },
      { key: 'projectEndDate', label: '结束时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  works: {
    titleKey: 'workTitle',
    dateKey: 'publishDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'workTitle', label: '作品名称', kind: 'input', placeholder: '请输入作品名称' },
      { key: 'workCategory', label: '作品类别', kind: 'input', placeholder: '文学/艺术/科技…' },
      { key: 'publishDate', label: '完成时间', kind: 'input', type: 'date' },
      { key: 'publishOccasion', label: '发表/展出平台', kind: 'input', placeholder: '请输入发表或展出平台' },
      { key: 'organizer', label: '主办单位', kind: 'input', placeholder: '请输入主办单位' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  doubleHundred: {
    titleKey: 'activityName',
    dateKey: 'activityDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'activityName', label: '活动名称', kind: 'input', placeholder: '请输入活动名称' },
      { key: 'activityDate', label: '参与时间', kind: 'input', type: 'date' },
      { key: 'organizer', label: '主办单位', kind: 'input', placeholder: '请输入主办单位' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
  ieerTraining: {
    titleKey: 'projectName',
    dateKey: 'projectStartDate',
    fields: [
      { key: 'studentNo', label: '学号', kind: 'input', placeholder: '请输入学号' },
      { key: 'studentName', label: '学生姓名', kind: 'input', placeholder: '请输入学生姓名' },
      { key: 'projectName', label: '项目名称', kind: 'input', placeholder: '请输入项目名称' },
      { key: 'projectLevel', label: '项目级别', kind: 'input', placeholder: '国家级/省级/校级' },
      { key: 'projectRole', label: '项目角色', kind: 'input', placeholder: '项目负责人/核心成员' },
      { key: 'projectStartDate', label: '开始时间', kind: 'input', type: 'date' },
      { key: 'projectEndDate', label: '结束时间', kind: 'input', type: 'date' },
      { key: 'remark', label: '备注', kind: 'input', placeholder: '请输入备注' },
    ],
  },
};

export const attachmentIconMap = {
  doc: '/assets/icons/doc.svg',
  docx: '/assets/icons/doc.svg',
  pdf: '/assets/icons/pdf.svg',
  xls: '/assets/icons/excel.svg',
  xlsx: '/assets/icons/excel.svg',
  ppt: '/assets/icons/ppt.svg',
  pptx: '/assets/icons/ppt.svg',
  zip: '/assets/icons/zip.svg',
  rar: '/assets/icons/zip.svg',
  '7z': '/assets/icons/zip.svg',
  mp4: '/assets/icons/video.svg',
  mov: '/assets/icons/video.svg',
  jpeg: '/assets/icons/image.svg',
  jpg: '/assets/icons/image.svg',
  png: '/assets/icons/image.svg',
  heif: '/assets/icons/image.svg',
};
