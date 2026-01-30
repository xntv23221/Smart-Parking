import { defineStore } from "pinia";
import * as authApi from "../api/auth";

type Role = "ADMIN" | "CLIENT";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("admin_token") || "",
    role: localStorage.getItem("admin_role") || "",
    username: ""
  }),
  getters: {
    isAuthed: (s) => Boolean(s.token),
    isAdmin: (s) => s.role === "ADMIN",
    isManager: (s) => s.role === "MANAGER"
  },
  actions: {
    setToken(token: string) {
      this.token = token;
      localStorage.setItem("admin_token", token);
      
      // Parse role from token
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.role = payload.role || "";
        localStorage.setItem("admin_role", this.role);
      } catch (e) {
        console.error("Failed to parse token", e);
      }
    },
    logout() {
      this.token = "";
      this.role = "";
      localStorage.removeItem("admin_token");
      localStorage.removeItem("admin_role");
    },
    async login(username: string, password: string) {
      const data = await authApi.login({ username, password });
      this.setToken(data.token);
      this.username = username;
    },
    async register(username: string, password: string, role: Role, phone?: string | null) {
      const data = await authApi.register({ username, password, role, phone });
      this.setToken(data.token);
      this.username = username;
    }
  }
});
