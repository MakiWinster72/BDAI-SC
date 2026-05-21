const ICON_BASE = "/assets/icons";

const BROWSER_ICON_MAP = {
  Chrome: "Chrome",
  Safari: "Safari",
  Firefox: "Firefox",
  Edge: "Edge",
  Opera: "opera",
  Brave: "brave",
};

const OS_ICON_MAP = {
  Windows: "Windows",
  macOS: "macos",
  Linux: "linux",
  Android: "android",
  iOS: "macos",
};

export function browserIconSrc(browser) {
  const file = BROWSER_ICON_MAP[browser];
  return file ? `${ICON_BASE}/${file}.svg` : "";
}

export function osIconSrc(os) {
  const file = OS_ICON_MAP[os];
  return file ? `${ICON_BASE}/${file}.svg` : "";
}

/** 从 deviceName（如 "Chrome / Windows"）解析浏览器与系统 */
export function parseBrowserOsFromDeviceName(deviceName) {
  if (!deviceName || typeof deviceName !== "string") {
    return { browser: "", os: "" };
  }
  const parts = deviceName.split(" / ").map((s) => s.trim());
  if (parts.length >= 2) {
    return { browser: parts[0], os: parts[1] };
  }
  const single = parts[0] || "";
  if (OS_ICON_MAP[single]) {
    return { browser: "", os: single };
  }
  if (BROWSER_ICON_MAP[single]) {
    return { browser: single, os: "" };
  }
  return { browser: single, os: "" };
}
