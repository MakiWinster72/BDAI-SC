export const gridDefaultColDef = {
  sortable: true,
  filter: true,
  resizable: true,
  minWidth: 90,
  flex: 1,
};

const gridLocaleText = {
  page: "页",
  more: "更多",
  to: "至",
  of: "共",
  next: "下一页",
  last: "末页",
  first: "首页",
  previous: "上一页",
  loadingOoo: "加载中...",
  selectAll: "全选",
  searchOoo: "搜索...",
  blank: "空值",
  notBlank: "非空",
  filterOoo: "筛选...",
  applyFilter: "应用筛选",
  equals: "等于",
  notEqual: "不等于",
  contains: "包含",
  notContains: "不包含",
  startsWith: "以...开头",
  endsWith: "以...结尾",
  lessThan: "小于",
  greaterThan: "大于",
  lessThanOrEqual: "小于等于",
  greaterThanOrEqual: "大于等于",
  inRange: "范围",
  setFilter: "集合筛选",
  columns: "列",
  filters: "筛选",
  reset: "重置",
  group: "分组",
  rowGroupColumnsEmptyMessage: "拖拽列到这里进行分组",
  pivotColumnsEmptyMessage: "拖拽列到这里进行透视",
  noRowsToShow: "暂无数据",
  sum: "求和",
  min: "最小值",
  max: "最大值",
  none: "无",
  count: "计数",
  avg: "平均值",
  copy: "复制",
  copyWithHeaders: "复制（含表头）",
  paste: "粘贴",
  export: "导出",
  csvExport: "导出 CSV",
  excelExport: "导出 Excel",
};

export function gridLocaleTextFunc(key, defaultValue) {
  if (key in gridLocaleText) {
    return gridLocaleText[key];
  }
  return defaultValue;
}

export const gridLocaleTextMap = gridLocaleText;
