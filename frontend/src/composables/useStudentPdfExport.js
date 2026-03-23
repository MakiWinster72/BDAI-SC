import { ref } from "vue";
import { jsPDF } from "jspdf";
import autoTable from "jspdf-autotable";
import harmonyFontUrl from "../assets/fonts/HarmonyOS_Sans_SC_Regular.ttf?url";
import harmonyFontBlackUrl from "../assets/fonts/HarmonyOS_Sans_SC_Black.ttf?url";
import { listAchievements } from "../api/achievement";

const PDF_FONT_NAME = "HarmonyOSSansSC";
const PDF_FONT_BLACK = "HarmonyOSSansSCBlack";
let pdfFontBase64 = null;
let pdfFontBlackBase64 = null;

const ACHIEVEMENT_CATEGORIES = [
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利（著作权）授权数（项）" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
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
  const yyyy = now.getFullYear();
  const mm = pad(now.getMonth() + 1);
  const dd = pad(now.getDate());
  const hh = pad(now.getHours());
  const min = pad(now.getMinutes());
  const ss = pad(now.getSeconds());
  return `${yyyy}${mm}${dd}_${hh}${min}${ss}`;
}

async function loadPdfFontBase64(url, cacheKey) {
  if (cacheKey === "regular" && pdfFontBase64) {
    return pdfFontBase64;
  }
  if (cacheKey === "black" && pdfFontBlackBase64) {
    return pdfFontBlackBase64;
  }
  const response = await fetch(url);
  const buffer = await response.arrayBuffer();
  const bytes = new Uint8Array(buffer);
  let binary = "";
  const chunkSize = 0x8000;
  for (let index = 0; index < bytes.length; index += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(index, index + chunkSize));
  }
  const base64 = btoa(binary);
  if (cacheKey === "regular") {
    pdfFontBase64 = base64;
  } else if (cacheKey === "black") {
    pdfFontBlackBase64 = base64;
  }
  return base64;
}

async function ensurePdfFonts(doc) {
  const base64 = await loadPdfFontBase64(harmonyFontUrl, "regular");
  const blackBase64 = await loadPdfFontBase64(harmonyFontBlackUrl, "black");
  doc.addFileToVFS("HarmonyOS_Sans_SC_Regular.ttf", base64);
  doc.addFont("HarmonyOS_Sans_SC_Regular.ttf", PDF_FONT_NAME, "normal");
  doc.addFileToVFS("HarmonyOS_Sans_SC_Black.ttf", blackBase64);
  doc.addFont("HarmonyOS_Sans_SC_Black.ttf", PDF_FONT_BLACK, "normal");
  doc.setFont(PDF_FONT_NAME, "normal");
}

function buildDefaultClassName(student) {
  if (!student) {
    return "";
  }
  if (student.className) {
    return student.className;
  }
  const safeYear = student.classYear ? `${student.classYear}级` : "";
  const safeMajor = student.classMajor || "";
  const safeNo = student.classNo ? `${student.classNo}班` : "";
  return `${safeYear}${safeMajor}${safeNo}`.trim();
}

export function useStudentPdfExport() {
  const pdfExporting = ref(false);

  async function exportResumePdf(options) {
    const {
      student,
      resolveMediaUrl,
      getStudentName,
      getStudentNo,
      getClassName,
      getCollege,
      getAddress,
      getOffCampusAddress,
      getEducationExperiences,
      getAchievements,
    } = options || {};

    if (pdfExporting.value || !student) {
      return;
    }

    pdfExporting.value = true;
    try {
      const studentName =
        (getStudentName && getStudentName(student)) ||
        student.fullName ||
        student.name ||
        "";
      const studentNo =
        (getStudentNo && getStudentNo(student)) || student.studentNo || "";
      const className =
        (getClassName && getClassName(student)) ||
        buildDefaultClassName(student);
      const college =
        (getCollege && getCollege(student)) || student.college || "";
      const addressText =
        (getAddress && getAddress(student)) || student.address || "";
      const offCampusAddress =
        (getOffCampusAddress && getOffCampusAddress(student)) ||
        student.offCampusAddress ||
        "";
      const educationExperiences =
        (getEducationExperiences && getEducationExperiences(student)) ||
        student.educationExperiences ||
        [];
      const achievements =
        (getAchievements && (await getAchievements(student))) ||
        (studentNo || studentName
          ? await listAchievements({ studentNo, studentName })
              .then(({ data }) => (Array.isArray(data) ? data : []))
              .catch(() => [])
          : []);

      const doc = new jsPDF({
        orientation: "portrait",
        unit: "pt",
        format: "a4",
      });
      await ensurePdfFonts(doc);

      const marginX = 40;
      const pageHeight = doc.internal.pageSize.getHeight();
      const maxY = pageHeight - 40;
      let currentY = 40;

      const ensureSpace = (heightNeeded) => {
        if (currentY + heightNeeded > maxY) {
          doc.addPage();
          doc.setFont(PDF_FONT_NAME, "normal");
          currentY = 40;
        }
      };

      const addSectionTitle = (title, opts = {}) => {
        const topGap = 30;
        const bottomGap = 10;
        const minContentHeight = opts.minContentHeight ?? 60;
        if (currentY + topGap + bottomGap + minContentHeight > maxY) {
          doc.addPage();
          doc.setFont(PDF_FONT_NAME, "normal");
          currentY = 40;
        }
        currentY += topGap;
        doc.setFont(PDF_FONT_BLACK, "normal");
        doc.setFontSize(opts.size || 13);
        doc.text(title, marginX, currentY);
        currentY += bottomGap;
        doc.setFont(PDF_FONT_NAME, "normal");
      };

      const addKeyValueTable = (rows, columns = 2) => {
        const rowPairs = [];
        for (let i = 0; i < rows.length; i += columns) {
          const slice = rows.slice(i, i + columns);
          const flat = [];
          slice.forEach(([label, value]) => {
            flat.push(label, value);
          });
          if (slice.length < columns) {
            flat.push("");
            flat.push("");
          }
          rowPairs.push(flat);
        }
        autoTable(doc, {
          body: rowPairs,
          startY: currentY + 6,
          styles: { fontSize: 9, cellPadding: 4, font: PDF_FONT_NAME },
          columnStyles: Object.fromEntries(
            Array.from({ length: columns }).map((_, index) => [
              index * 2,
              { font: PDF_FONT_BLACK },
            ]),
          ),
          theme: "grid",
        });
        currentY = doc.lastAutoTable.finalY + 14;
      };

      const avatarSize = 88;
      const avatarUrlRaw = student.avatarUrl || "";
      if (avatarUrlRaw) {
        const avatarUrl = resolveMediaUrl
          ? resolveMediaUrl(avatarUrlRaw)
          : avatarUrlRaw;
        try {
          const response = await fetch(avatarUrl);
          const blob = await response.blob();
          const dataUrl = await new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.readAsDataURL(blob);
          });
          const format = blob.type.includes("png") ? "PNG" : "JPEG";
          doc.addImage(
            dataUrl,
            format,
            marginX,
            currentY,
            avatarSize,
            avatarSize,
          );
        } catch {
          // ignore avatar failures
        }
      }

      const infoX = marginX + avatarSize + 20;
      const lineHeight = 18;
      doc.setFont(PDF_FONT_BLACK, "normal");
      doc.setFontSize(18);
      doc.text(studentName || "未命名", infoX, currentY + 18);
      doc.setFont(PDF_FONT_NAME, "normal");
      doc.setFontSize(11);
      const infoLines = [
        `学号：${studentNo || "-"}`,
        `班级：${className || "-"}`,
        `学院：${college || "-"}`,
      ];
      infoLines.forEach((line, index) => {
        doc.text(line, infoX, currentY + 40 + index * lineHeight);
      });
      currentY += Math.max(avatarSize, 40 + infoLines.length * lineHeight) + 20;

      addSectionTitle("学籍信息");
      addKeyValueTable([
        ["姓名", studentName || ""],
        ["学号", studentNo || ""],
        ["年级", student.classYear ? `${student.classYear}级` : ""],
        ["学院", college || ""],
        ["专业", student.classMajor || ""],
        ["班级", className || ""],
        ["入学时间", student.enrollmentDate || ""],
        ["学生类别", student.studentCategory || ""],
        ["班主任", student.classTeacher || ""],
        ["辅导员", student.counselor || ""],
      ]);

      addSectionTitle("个人证件与联系方式");
      addKeyValueTable([
        ["民族", student.ethnicity || ""],
        ["政治面貌", student.politicalStatus || ""],
        ["手机号码", student.phone || ""],
        ["备用联系方式（QQ/邮箱）", student.backupContact || ""],
        ["身份证号", student.idNo || ""],
        ["籍贯", student.nativePlace || ""],
        ["住址", addressText || ""],
      ]);

      addSectionTitle("住宿信息");
      if (student.offCampusLiving) {
        addKeyValueTable([
          ["是否在外居住", "是"],
          ["外居住地址", offCampusAddress || ""],
        ]);
      } else {
        addKeyValueTable([
          ["是否在外居住", "否"],
          ["住宿校区", student.dormCampus || ""],
          ["住宿楼栋", student.dormBuilding || ""],
          ["住宿房间", student.dormRoom || ""],
        ]);
      }

      addSectionTitle("家庭信息");
      const familyRows = [
        ["父亲姓名", student.fatherName || ""],
        ["父亲电话", student.fatherPhone || ""],
        ["父亲工作单位", student.fatherWorkUnit || ""],
        ["父亲职务", student.fatherTitle || ""],
        ["母亲姓名", student.motherName || ""],
        ["母亲电话", student.motherPhone || ""],
        ["母亲工作单位", student.motherWorkUnit || ""],
        ["母亲职务", student.motherTitle || ""],
      ];
      if (student.hkMoTw !== undefined) {
        familyRows.push(["港澳台", formatYesNo(student.hkMoTw)]);
      }
      if (student.specialStudent !== undefined) {
        familyRows.push(["特殊学生", formatYesNo(student.specialStudent)]);
      }
      addKeyValueTable(familyRows);

      addSectionTitle("紧急联系人");
      addKeyValueTable([
        ["紧急联系人电话", student.emergencyPhone || ""],
        ["紧急联系人关系", student.emergencyRelation || ""],
      ]);

      const drawNode = (label, x, y, w, h, muted) => {
        doc.setDrawColor(3, 107, 114);
        doc.setFillColor(255, 255, 255);
        doc.roundedRect(x, y, w, h, 8, 8, "FD");
        doc.setFont(PDF_FONT_NAME, "normal");
        doc.setFontSize(10);
        doc.setTextColor(muted ? 160 : 20);
        doc.text(label, x + 10, y + h / 2 + 3);
        doc.setTextColor(20);
      };
      const drawArrow = (fromX, fromY, toX, toY, markPending) => {
        doc.setDrawColor(3, 107, 114);
        doc.line(fromX, fromY, toX, toY);
        doc.triangle(toX - 4, toY - 2, toX, toY, toX - 4, toY + 2, "F");
        if (markPending) {
          const midY = (fromY + toY) / 2;
          doc.setFont(PDF_FONT_BLACK, "normal");
          doc.setFontSize(10);
          doc.text("×", toX + 6, midY + 3);
        }
      };
      const nodeHeight = 26;
      const nodeGap = 14;
      const nodes = [];
      const leagueJoined = student.leagueJoined === true;
      const partyApplied = student.partyApplied === true;
      if (!leagueJoined) {
        nodes.push("入团情况：未申请入团");
      } else {
        nodes.push("已申请入团");
        nodes.push(
          `提交入团申请书时间：${student.leagueApplicationDate || "—"}`,
        );
        nodes.push(
          `入团时间：${
            formatDateOrEmpty(
              student.leagueJoinDate,
              student.leagueDeveloping,
              "正在发展",
            ) || "—"
          }`,
        );
        nodes.push(`团号：${student.leagueNo || "—"}`);
      }
      if (!partyApplied) {
        nodes.push("入党情况：未申请入党");
      } else {
        nodes.push("已申请入党");
        nodes.push(`提交入党申请书时间：${student.applicationDate || "—"}`);
        nodes.push(
          `确定积极分子时间：${
            formatDateOrEmpty(
              student.activistDate,
              student.activistDeveloping,
              "正在发展",
            ) || "—"
          }`,
        );
        nodes.push(
          `上党课时间：${
            formatDateOrEmpty(
              student.partyTrainingDate,
              student.partyTrainingPending,
              "暂未报名",
            ) || "—"
          }`,
        );
        nodes.push(
          `确定发展对象时间：${
            formatDateOrEmpty(
              student.developmentTargetDate,
              student.developmentTargetDeveloping,
              "正在发展",
            ) || "—"
          }`,
        );
        nodes.push(
          `接收为预备党员时间：${
            formatDateOrEmpty(
              student.probationaryMemberDate,
              student.probationaryDeveloping,
              "正在发展",
            ) || "—"
          }`,
        );
        nodes.push(
          `转为正式党员时间：${
            formatDateOrEmpty(
              student.fullMemberDate,
              student.fullMemberDeveloping,
              "正在发展",
            ) || "—"
          }`,
        );
      }
      const pendingFlags = nodes.map(
        (label) => label.includes("正在发展") || label.includes("暂未报名"),
      );
      const pendingIndex = pendingFlags.findIndex((flag) => flag);
      const maxTextWidth = nodes.reduce((max, label) => {
        const width = doc.getTextWidth(label);
        return Math.max(max, width);
      }, 0);
      const flowWidth = Math.min(
        doc.internal.pageSize.getWidth() - marginX * 2,
        Math.max(240, maxTextWidth + 28),
      );
      const flowX =
        marginX +
        (doc.internal.pageSize.getWidth() - marginX * 2 - flowWidth) / 2;
      const flowHeight =
        nodes.length * nodeHeight + Math.max(0, nodes.length - 1) * nodeGap;
      addSectionTitle("团组织与入党信息", {
        minContentHeight: flowHeight + 10,
      });
      ensureSpace(flowHeight + 10);
      let nodeY = currentY + 6;
      nodes.forEach((label, index) => {
        const muted = pendingIndex >= 0 && index > pendingIndex;
        drawNode(label, flowX, nodeY, flowWidth, nodeHeight, muted);
        if (index < nodes.length - 1) {
          const markPending = pendingFlags[index + 1];
          drawArrow(
            flowX + flowWidth / 2,
            nodeY + nodeHeight,
            flowX + flowWidth / 2,
            nodeY + nodeHeight + nodeGap - 4,
            markPending,
          );
        }
        nodeY += nodeHeight + nodeGap;
      });
      currentY = nodeY + 6;

      addSectionTitle("教育经历");
      if (Array.isArray(educationExperiences) && educationExperiences.length) {
        autoTable(doc, {
          head: [["起始时间", "结束时间", "学校名", "学位", "证明人"]],
          body: educationExperiences.map((edu) => {
            const start = edu.startDate || "";
            const end = edu.isCurrent ? "至今" : edu.endDate || "";
            return [
              start,
              end,
              edu.schoolName || "",
              edu.educationLevel || "",
              edu.witness || "",
            ];
          }),
          startY: currentY + 6,
          styles: { fontSize: 9, cellPadding: 4, font: PDF_FONT_NAME },
          headStyles: {
            font: PDF_FONT_BLACK,
            fillColor: [31, 79, 87],
            textColor: 255,
          },
          theme: "grid",
        });
        currentY = doc.lastAutoTable.finalY + 14;
      } else {
        doc.setFontSize(10);
        doc.text("暂无教育经历", marginX, currentY + 12);
        currentY += 20;
      }

      if (achievements.length) {
        doc.addPage();
        doc.setFont(PDF_FONT_NAME, "normal");
        currentY = 40;
        addSectionTitle("个人成就", { size: 16 });
        const grouped = achievements.reduce((acc, record) => {
          const category = record.category || "other";
          acc[category] = acc[category] || [];
          acc[category].push(record);
          return acc;
        }, {});
        for (const category of ACHIEVEMENT_CATEGORIES) {
          const records = grouped[category.key] || [];
          if (!records.length) {
            continue;
          }
          addSectionTitle(category.label, { size: 13 });
          for (const record of records) {
            const fields = ACHIEVEMENT_FIELDS[category.key] || [];
            const rowValues = fields.map((field) => [
              field.label,
              record.fields?.[field.key] || "",
            ]);
            addKeyValueTable(rowValues, 1);
          }
        }
      }

      doc.save(`student_resume_${formatTimestamp()}.pdf`);
    } finally {
      pdfExporting.value = false;
    }
  }

  return { exportResumePdf, pdfExporting };
}
