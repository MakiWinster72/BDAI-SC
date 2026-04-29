import {
  formatPaperAuthors,
  formatPatentInventor,
  text,
} from "../utils/achievementFormatters";

function row(label, value, options = {}) {
  return {
    label,
    value,
    hidden: options.hidden || null,
  };
}

export const achievementDetailSchemas = {
  paper: {
    tag: "学术论文",
    groups: [
      {
        label: "期刊信息",
        rows: [
          row("发表期刊", (fields) => text(fields.journalName)),
          row("收录情况", (fields) => text(fields.indexed), {
            hidden: (fields) => !fields.indexed,
          }),
        ],
      },
      {
        label: "作者信息",
        rows: [row("作者", (fields) => formatPaperAuthors(fields))],
      },
      {
        rows: [row("发表时间", (fields) => text(fields.publishDate))],
      },
    ],
  },
  journal: {
    tag: "期刊作品",
    groups: [
      {
        label: "刊物信息",
        rows: [row("刊物名称", (fields) => text(fields.publicationName))],
      },
      {
        label: "作者信息",
        rows: [row("作者", (fields) => text(fields.studentName))],
      },
      {
        rows: [row("发表时间", (fields) => text(fields.publishDate))],
      },
    ],
  },
  patent: {
    tag: (fields) => text(fields.patentType, "专利"),
    groups: [
      {
        label: "专利信息",
        rows: [row("授权号", (fields) => text(fields.grantNo))],
      },
      {
        label: "发明人信息",
        rows: [row("发明人", (fields) => formatPatentInventor(fields))],
      },
      {
        rows: [row("获批时间", (fields) => text(fields.grantDate))],
      },
    ],
  },
  certificate: {
    tag: "职业资格证书",
    groups: [
      {
        label: "证书信息",
        rows: [row("证书类别", (fields) => text(fields.certificateType))],
      },
      {
        label: "持证人",
        rows: [row("姓名", (fields) => text(fields.studentName))],
      },
      {
        rows: [row("获得时间", (fields) => text(fields.obtainDate))],
      },
    ],
  },
  research: {
    tag: "科研项目",
    groups: [
      {
        label: "项目信息",
        rows: [
          row("项目负责人", (fields) => text(fields.projectLeader)),
          row("教师工号", (fields) => text(fields.teacherNo)),
        ],
      },
    ],
  },
};
