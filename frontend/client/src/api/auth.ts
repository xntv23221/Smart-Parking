import { request } from "./http";

export async function register(params: { username: string; password: string; role: "ADMIN" | "CLIENT"; phone: string }) {
  return request<{ token: string }>({
    method: "POST",
    url: "/api/public/v1/auth/register",
    data: params
  });
}

export async function login(params: { username: string; password: string }) {
  return request<{ token: string }>({
    method: "POST",
    url: "/api/public/v1/auth/login",
    data: params
  });
}
