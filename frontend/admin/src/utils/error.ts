export function getErrorMessage(error: unknown, fallback = "请求失败") {
  if (typeof error === "string") {
    return error;
  }
  if (!error || typeof error !== "object") {
    return fallback;
  }
  const maybeMessage = (error as Record<string, unknown>).message;
  if (typeof maybeMessage === "string" && maybeMessage.trim()) {
    return maybeMessage;
  }
  return fallback;
}
