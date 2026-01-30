import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
      meta: { public: true }
    },
    {
      path: "/",
      component: () => import("../views/layout/AdminLayout.vue"),
      children: [
        { path: "", redirect: "/messages" },
        { path: "users", name: "users", component: () => import("../views/UsersView.vue"), meta: { requiresAuth: true } },
        { path: "messages", name: "messages", component: () => import("../views/MessagesView.vue"), meta: { requiresAuth: true } },
        { path: "parking-lots", name: "parking-lots", component: () => import("../views/ParkingLotsView.vue"), meta: { requiresAuth: true } },
        { path: "spaces", name: "spaces", component: () => import("../views/SpacesView.vue"), meta: { requiresAuth: true } },
        { path: "orders", name: "orders", component: () => import("../views/OrdersView.vue"), meta: { requiresAuth: true } }
      ]
    },
    { path: "/:pathMatch(.*)*", redirect: "/messages" }
  ]
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    if (to.path === "/login" && auth.isAuthed) {
      return "/messages";
    }
    return true;
  }
  if (to.meta.requiresAuth && !auth.isAuthed) {
    return "/login";
  }
  return true;
});

export default router;

