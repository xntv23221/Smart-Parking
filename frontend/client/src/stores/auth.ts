import { defineStore } from "pinia";
import * as authApi from "../api/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("client_token") || "",
    username: ""
  }),
  getters: {
    isAuthed: (s) => Boolean(s.token)
  },
  actions: {
    setToken(token: string) {
      this.token = token;
      localStorage.setItem("client_token", token);
    },
    logout() {
      this.token = "";
      localStorage.removeItem("client_token");
    },
    async login(username: string, password: string) {
      const data = await authApi.login({ username, password });
      this.setToken(data.token);
      this.username = username;
    },
    async register(username: string, password: string, phone: string) {
      const data = await authApi.register({ username, password, role: "CLIENT", phone });
      this.setToken(data.token);
      this.username = username;
    }
  }
});
