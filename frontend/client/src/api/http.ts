import axios from "axios";
import { getErrorMessage } from "../utils/error";

export type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
};

export const http = axios.create({
  baseURL: "/",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("client_token");
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

function normalizeErrorMessage(error: unknown): string {
  return getErrorMessage(error, "请求失败");
}

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown> | unknown;
    if (body && typeof body === "object" && "code" in body && typeof (body as any).code === "number") {
      const result = body as ApiResult<unknown>;
      if (result.code === 0) {
        return { ...response, data: result.data };
      }
      throw new Error(result.message || "请求失败");
    }
    return response;
  },
  (error) => {
    const errorRecord = error as { response?: { status?: number; data?: unknown }; message?: unknown } | undefined;
    const status = errorRecord?.response?.status;
    if (status === 401 || status === 403) {
      localStorage.removeItem("client_token");
      if (window.location.pathname !== "/login") {
        window.location.replace("/login");
      }
    }
    const responseData = errorRecord?.response?.data as Record<string, unknown> | undefined;
    const message = responseData?.message ?? responseData?.error ?? errorRecord?.message ?? "请求失败";
    return Promise.reject(new Error(normalizeErrorMessage(message)));
  }
);

export async function request<T>(config: Parameters<typeof http.request>[0]) {
  const response = await http.request<T>(config);
  return response.data;
}
