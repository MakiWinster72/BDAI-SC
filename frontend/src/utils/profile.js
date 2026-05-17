/**
 * profile - 学生档案相关转换工具
 *
 * 从 MyInfosView 和 StudentProfileEditor 中提取的可复用函数。
 */
import { regionData, codeToText } from "element-china-area-data";

// ── 地址解析 ────────────────────────────────────────────

/**
 * 解析地址字符串为省市区+详细地址
 * @param {string} rawAddress
 * @returns {{ province: string, city: string, county: string, detail: string }}
 */
export function parseAddressToRegion(rawAddress) {
  const address = String(rawAddress || "").trim();
  if (!address) {
    return { province: "", city: "", county: "", detail: "" };
  }
  const province = regionData.find((item) => address.startsWith(item.label));
  if (!province) {
    return { province: "", city: "", county: "", detail: address };
  }
  let remaining = address.slice(province.label.length);
  let city = province.children?.find((item) => remaining.startsWith(item.label));
  let county = null;

  if (city) {
    remaining = remaining.slice(city.label.length);
    county = city.children?.find((item) => remaining.startsWith(item.label));
    if (county) {
      remaining = remaining.slice(county.label.length);
    }
  } else {
    for (const candidate of province.children || []) {
      for (const item of candidate.children || []) {
        const prefix = `${candidate.label}${item.label}`;
        if (remaining.startsWith(prefix)) {
          city = candidate;
          county = item;
          remaining = remaining.slice(prefix.length);
          break;
        }
      }
      if (city) {
        break;
      }
    }
  }

  return {
    province: province.value,
    city: city?.value || "",
    county: county?.value || "",
    detail: remaining.trim(),
  };
}

/**
 * 构建完整地址字符串
 * @param {string} province
 * @param {string} city
 * @param {string} county
 * @param {string} detail
 * @param {string} [fallback]
 * @returns {string}
 */
export function buildAddress(province, city, county, detail, fallback) {
  const parts = [
    codeToText[province],
    codeToText[city],
    codeToText[county],
  ].filter(Boolean);
  const safeDetail = String(detail || "").trim();
  const combined = [...parts, safeDetail].filter(Boolean).join("");
  if (combined) {
    return combined;
  }
  return String(fallback || "").trim();
}

// ── 宿舍解析 ────────────────────────────────────────────

/**
 * 解析宿舍字符串为楼层和房间号
 * @param {string} rawValue
 * @returns {{ floor: string, roomNo: string }}
 */
export function parseDormRoom(rawValue) {
  const raw = String(rawValue || "").trim();
  if (!raw) {
    return { floor: "", roomNo: "" };
  }
  const floorMatch = raw.match(/^(\d+)层/);
  const roomMatch = raw.match(/(\d+)号$/);
  return {
    floor: floorMatch ? floorMatch[1] : "",
    roomNo: roomMatch ? roomMatch[1] : "",
  };
}

/**
 * 构建宿舍字符串
 * @param {string} floor
 * @param {string} roomNo
 * @param {string} [fallback]
 * @returns {string}
 */
export function buildDormRoom(floor, roomNo, fallback) {
  const safeFloor = String(floor || "").trim();
  const safeRoomNo = String(roomNo || "").trim();
  if (safeFloor || safeRoomNo) {
    return `${safeFloor}层${safeRoomNo}号`.trim();
  }
  return fallback || "";
}

// ── 班级解析 ────────────────────────────────────────────

/**
 * 构建班级名称字符串
 * @param {string} year
 * @param {string} major
 * @param {string} no
 * @param {string} [fallback]
 * @returns {string}
 */
export function buildClassName(year, major, no, fallback) {
  const hasParts = Boolean(year || major || no);
  if (!hasParts && fallback) {
    return fallback;
  }
  const safeYear = year ? `${year}级` : "";
  const safeMajor = major || "";
  const safeNo = no ? `${no}班` : "";
  return `${safeYear}${safeMajor}${safeNo}`.trim();
}
