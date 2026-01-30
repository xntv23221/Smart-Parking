import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import { ElMessage } from "element-plus";
import { getErrorMessage } from "./utils/error";

const app = createApp(App);

app.config.errorHandler = (err) => {
  ElMessage.error(getErrorMessage(err, "发生错误"));
};

app.use(createPinia()).use(router).use(ElementPlus).mount("#app");
