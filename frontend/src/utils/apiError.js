/**
 * 从 Axios 错误响应中提取后端 message 字段。
 */
export function getApiErrorMessage(error, fallback = "请求失败，请稍后重试") {
  const message = error?.response?.data?.message;
  if (typeof message === "string" && message.trim()) {
    return message.trim();
  }
  return fallback;
}
