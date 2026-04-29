function text(value, fallback = "-") {
  const normalized = String(value ?? "").trim();
  return normalized || fallback;
}

function joinText(values = [], separator = " · ", fallback = "-") {
  const normalized = values
    .map((value) => String(value ?? "").trim())
    .filter(Boolean);
  return normalized.length ? normalized.join(separator) : fallback;
}

export function formatContestDate(fields = {}) {
  return text(fields.awardDate);
}

export function formatContestAwardPill(fields = {}) {
  return joinText([fields.awardCategory, fields.awardLevel], " ", "");
}

export function formatContestCategoryLine(fields = {}) {
  return `类别：${joinText([fields.contestCategory, fields.contestType])}`;
}

export function formatContestMembers(fields = {}) {
  const people = joinText([fields.studentName, fields.teamMembers], "、");
  const teachers = text(fields.instructors, "");
  return teachers ? `成员：${people} | 指导老师：${teachers}` : `成员：${people}`;
}

export function formatContestAwardLine(fields = {}) {
  const count = text(fields.awardCount);
  const remark = text(fields.remark, "");
  return remark ? `获奖人数：${count} · ${remark}` : `获奖人数：${count}`;
}

export function formatContestOrganizer(fields = {}) {
  return `主办单位：${text(fields.organizer)}`;
}

export function formatPaperAuthors(fields = {}) {
  const name = text(fields.studentName);
  return fields.authorOrder ? `${name}（${fields.authorOrder}）` : name;
}

export function formatPatentInventor(fields = {}) {
  const name = text(fields.studentName);
  return fields.firstInventor ? `${name}（${fields.firstInventor}）` : name;
}

export function formatWorksCategory(fields = {}) {
  return text(fields.workCategory);
}

export function formatWorksOccasion(fields = {}) {
  return text(fields.publishOccasion);
}

export function formatWorksDate(fields = {}) {
  return text(fields.publishDate);
}

export function formatWorksTag(fields = {}) {
  return text(fields.workCategory, "作品");
}

export { joinText, text };
