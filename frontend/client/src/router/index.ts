import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "splash", component: () => import("../views/SplashView.vue"), meta: { public: true } },
    { path: "/login", name: "login", component: () => import("../views/LoginView.vue"), meta: { public: true } },
    { path: "/register", name: "register", component: () => import("../views/RegisterView.vue"), meta: { public: true } },
    {
      path: "/",
      component: () => import("../views/layout/MobileLayout.vue"),
      children: [
        { path: "home", name: "home", component: () => import("../views/HomeView.vue"), meta: { requiresAuth: true } },
        { path: "assistant", name: "assistant", component: () => import("../views/AssistantView.vue"), meta: { requiresAuth: true } },
        { path: "message", name: "message", component: () => import("../views/MessageView.vue"), meta: { requiresAuth: true } },
        { path: "wallet", name: "wallet", component: () => import("../views/WalletView.vue"), meta: { requiresAuth: true } },
        { path: "profile", name: "profile", component: () => import("../views/ProfileView.vue"), meta: { requiresAuth: true } },
        { path: "profile/edit", name: "profile-edit", component: () => import("../views/ProfileEditView.vue"), meta: { requiresAuth: true } },
        { path: "vehicles", name: "vehicles", component: () => import("../views/VehicleListView.vue"), meta: { requiresAuth: true } },
        { path: "vehicles/add", name: "vehicle-add", component: () => import("../views/VehicleAddView.vue"), meta: { requiresAuth: true } },
        { path: "invoices", name: "invoices", component: () => import("../views/InvoiceView.vue"), meta: { requiresAuth: true } },
        { path: "monthly-cards", name: "monthly-cards", component: () => import("../views/MonthlyCardView.vue"), meta: { requiresAuth: true } },
        { path: "orders", name: "orders", component: () => import("../views/OrdersView.vue"), meta: { requiresAuth: true } }
      ]
    },
    { path: "/:pathMatch(.*)*", redirect: "/home" }
  ]
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    if (to.path === "/login" && auth.isAuthed) {
      return "/home";
    }
    return true;
  }
  if (to.meta.requiresAuth && !auth.isAuthed) {
    return "/login";
  }
  return true;
});

export default router;
