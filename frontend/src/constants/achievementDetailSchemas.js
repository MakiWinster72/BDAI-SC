import {
  formatPaperAuthors,
  formatPatentInventor,
  text,
} from "@/utils/achievementFormatters";

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
  sanSanXiang: {
    tag: "三下乡社会实践",
    groups: [
      {
        label: "团队信息",
        rows: [
          row("所在学院", (fields) => text(fields.college)),
          row("团队名称", (fields) => text(fields.teamName)),
          row("团队负责人", (fields) => text(fields.teamLeader)),
          row("团队人数", (fields) => text(fields.teamSize)),
        ],
      },
      {
        label: "项目信息",
        rows: [
          row("项目名称", (fields) => text(fields.projectName)),
          row("主要服务类别", (fields) => text(fields.serviceCategory)),
          row("项目类型", (fields) => text(fields.projectType)),
          row("立项等级", (fields) => text(fields.projectLevel)),
        ],
      },
      {
        label: "实施信息",
        rows: [
          row("实践天数", (fields) => text(fields.practiceDays)),
          row("指导老师", (fields) => text(fields.instructor)),
          row("百千万校地通结对", (fields) => text(fields.isPaired)),
          row("单位初评结项等级", (fields) => text(fields.finalLevel)),
        ],
      },
    ],
  },
};
