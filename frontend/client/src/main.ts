import { createApp } from "vue";
import { createPinia } from "pinia";
import { 
  Button, Cell, CellGroup, Field, Form, NavBar, Tab, Tabbar, TabbarItem, Tabs,
  Search, Icon, Tag, ActionSheet, Loading, PullRefresh, List, SwipeCell, Badge, Empty, Image as VanImage,
  Dialog, Popup, Radio, RadioGroup, Grid, GridItem, Uploader
} from "vant";
import "vant/lib/index.css";
import App from "./App.vue";
import router from "./router";
import { showToast } from "vant";
import { getErrorMessage } from "./utils/error";

const app = createApp(App);

app.config.errorHandler = (err) => {
  showToast(getErrorMessage(err, "发生错误"));
};

app
  .use(createPinia())
  .use(router)
  .use(Button)
  .use(Cell)
  .use(CellGroup)
  .use(Field)
  .use(Form)
  .use(NavBar)
  .use(Tab)
  .use(Tabs)
  .use(Tabbar)
  .use(TabbarItem)
  .use(Search)
  .use(Icon)
  .use(Tag)
  .use(ActionSheet)
  .use(Loading)
  .use(PullRefresh)
  .use(List)
  .use(SwipeCell)
  .use(Badge)
  .use(Empty)
  .use(VanImage)
  .use(Dialog)
  .use(Popup)
  .use(Radio)
  .use(RadioGroup)
  .use(Grid)
  .use(GridItem)
  .use(Uploader)
  .mount("#app");
