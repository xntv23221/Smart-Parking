<template>
  <div class="chat-page">
    <van-nav-bar
      title="智停管家"
      fixed
      placeholder
      z-index="100"
      class="custom-nav"
    />

    <div class="chat-container" ref="chatContainerRef" @click="hideKeyboard">
      <div v-if="messages.length === 0" class="empty-tip">
        <div class="empty-icon-wrapper">
           <van-icon name="chat-o" size="40" color="#fff" />
        </div>
        <p>有什么可以帮您的吗？</p>
      </div>

      <div v-for="msg in messages" :key="msg.id" class="message-item" :class="msg.type">
        <div class="avatar">
          <!-- AI 头像 -->
          <img v-if="msg.type === 'ai'" src="https://img.yzcdn.cn/vant/logo.png" alt="AI" class="avatar-img" />
          <!-- 用户头像 -->
          <img v-else :src="userAvatar" alt="Me" class="avatar-img" />
        </div>
        <div class="content">
          <!-- 昵称 (可选，这里省略以保持整洁) -->
          <div class="bubble" :class="msg.type">
            <span v-html="formatContent(msg.content)"></span>
            <!-- 加载动画 -->
            <div v-if="msg.loading" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="footer-area">
      <!-- 快捷功能区 (悬浮在输入框上方) -->
      <transition name="fade-slide">
        <div class="quick-actions" v-if="!sending && showQuickActions">
          <div 
            class="action-chip" 
            v-for="action in quickActions" 
            :key="action.text"
            @click="handleQuickAction(action)"
          >
            {{ action.text }}
          </div>
        </div>
      </transition>

      <!-- 输入框区域 -->
      <div class="input-toolbar">
        <div class="input-box">
          <textarea
            v-model="input"
            rows="1"
            placeholder="发消息..."
            class="native-input"
            @input="autoResize"
            @focus="showQuickActions = false"
            @blur="showQuickActions = true"
            ref="inputRef"
          ></textarea>
        </div>
        <van-button 
          type="success" 
          class="send-btn"
          :disabled="!input.trim()"
          :loading="sending"
          @click="onSend"
        >
          发送
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { 
  chatAssistant, 
  amapIpLocate, 
  amapWeather, 
  amapPlaceAround,
  getProfile
} from "../api/client";
import { getErrorMessage } from "../utils/error";

const router = useRouter();
const chatContainerRef = ref<HTMLElement | null>(null);
const inputRef = ref<HTMLTextAreaElement | null>(null);
const userInfo = ref<any>(null);

const userAvatar = computed(() => {
  return userInfo.value?.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';
});

interface Message {
  id: number;
  type: 'user' | 'ai';
  content: string;
  time: number;
  loading?: boolean;
}

const messages = ref<Message[]>([
  {
    id: 1,
    type: 'ai',
    content: '您好！我是您的智停管家，有什么可以帮您？我可以为您提供停车建议、天气查询等服务。',
    time: Date.now()
  }
]);

const input = ref("");
const sending = ref(false);
const showQuickActions = ref(true);

const quickActions = [
  { text: '查天气', icon: 'cloud-o', type: 'weather' },
  { text: '找附近停车场', icon: 'location-o', type: 'parking' },
  { text: '收费标准', icon: 'gold-coin-o', type: 'price' },
  { text: '怎么用', icon: 'question-o', type: 'help' }
];

// 自动调整输入框高度
const autoResize = () => {
  const el = inputRef.value;
  if (el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 100) + 'px';
  }
};

const formatContent = (text: string) => {
  return text.replace(/\n/g, '<br>');
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
  }
};

const addMessage = (type: 'user' | 'ai', content: string, loading = false) => {
  const msg: Message = {
    id: Date.now(),
    type,
    content,
    time: Date.now(),
    loading
  };
  messages.value.push(msg);
  scrollToBottom();
  // Return the reactive proxy from the array
  return messages.value[messages.value.length - 1];
};

const handleQuickAction = async (action: any) => {
  switch (action.type) {
    case 'weather':
      await handleWeather();
      break;
    case 'parking':
      await handleParking();
      break;
    case 'price':
      addMessage('user', '如何收费？');
      addMessage('ai', '我们的收费标准如下：\n首小时：5元\n之后每小时：3元\n24小时封顶：30元\n月卡用户免费。');
      break;
    case 'help':
      addMessage('user', '使用帮助');
      addMessage('ai', '您可以通过我查询天气、寻找附近的停车场、查询停车记录等。只需在输入框输入您的问题即可。');
      break;
  }
};

const handleWeather = async () => {
  addMessage('user', '查天气');
  sending.value = true;
  // 添加一个临时的 loading 消息
  const loadingMsg = addMessage('ai', '', true);
  
  try {
    const ipRes = await amapIpLocate();
    if (ipRes && ipRes.adcode) {
      const weatherRes = await amapWeather(ipRes.adcode);
      if (weatherRes.lives && weatherRes.lives.length > 0) {
        const live = weatherRes.lives[0];
        const text = `当前${live.city}天气：${live.weather}\n温度：${live.temperature}℃\n风向：${live.winddirection}风 ${live.windpower}级\n湿度：${live.humidity}%`;
        // 替换 loading 消息
        loadingMsg.loading = false;
        loadingMsg.content = text;
      } else {
        loadingMsg.loading = false;
        loadingMsg.content = '抱歉，暂时无法获取当前城市的天气信息。';
      }
    } else {
      loadingMsg.loading = false;
      loadingMsg.content = '抱歉，无法获取您的位置信息。';
    }
  } catch (e) {
    console.error(e);
    loadingMsg.loading = false;
    loadingMsg.content = '获取天气信息失败，请稍后再试。';
  } finally {
    sending.value = false;
  }
};

const handleParking = async () => {
  addMessage('user', '找车位');
  sending.value = true;
  const loadingMsg = addMessage('ai', '', true);

  const searchParking = async (location: string, source: string) => {
    try {
      const res = await amapPlaceAround(location, '停车场', 3000);
      if (res.pois && res.pois.length > 0) {
        let text = `为您找到附近 ${res.pois.length} 个停车场（3km内，基于${source}）：\n`;
        res.pois.slice(0, 5).forEach((poi: any, index: number) => {
          text += `${index + 1}. ${poi.name} (距您${poi.distance}米)\n`;
        });
        loadingMsg.loading = false;
        loadingMsg.content = text;
      } else {
        loadingMsg.loading = false;
        loadingMsg.content = `抱歉，附近3km内未找到停车场（基于${source}）。`;
      }
    } catch (e) {
      console.error(e);
      loadingMsg.loading = false;
      loadingMsg.content = '搜索停车场失败，请稍后再试。';
    } finally {
      sending.value = false;
    }
  };

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const location = `${position.coords.longitude},${position.coords.latitude}`;
        await searchParking(location, "GPS定位");
      },
      async (err) => {
        console.warn("GPS failed, trying IP...", err);
        await fallbackIpParking(searchParking);
      },
      { enableHighAccuracy: true, timeout: 5000 }
    );
  } else {
    await fallbackIpParking(searchParking);
  }
};

const fallbackIpParking = async (callback: (loc: string, src: string) => Promise<void>) => {
  try {
    const ipRes = await amapIpLocate();
    if (ipRes && ipRes.rectangle) {
      const rect = ipRes.rectangle.split(';');
      if (rect.length > 0) {
        await callback(rect[0], "IP定位");
        return;
      }
    }
    throw new Error("IP location invalid");
  } catch (e) {
    console.error("IP locate failed, using default", e);
    const defaultLoc = "126.63,45.75";
    await callback(defaultLoc, "默认位置(哈尔滨)");
  }
}

const onSend = async () => {
  const content = input.value.trim();
  if (!content) return;

  addMessage('user', content);
  input.value = "";
  autoResize(); // Reset height
  
  sending.value = true;
  const loadingMsg = addMessage('ai', '', true);

  try {
    const resp = await chatAssistant(content);
    loadingMsg.loading = false;
    loadingMsg.content = resp.answer || "抱歉，我暂时无法回答这个问题。";
  } catch (e: unknown) {
    showToast(getErrorMessage(e, "请求失败"));
    loadingMsg.loading = false;
    loadingMsg.content = "抱歉，遇到了一些网络问题，请稍后再试。";
  } finally {
    sending.value = false;
  }
};

const hideKeyboard = () => {
  // 简单实现：点击聊天区域收起键盘（失去焦点）
  // inputRef.value?.blur(); 
  // 可选：是否需要此行为，微信点击空白处并不总是收起键盘
};

onMounted(async () => {
  scrollToBottom();
  try {
    const res = await getProfile();
    userInfo.value = res;
  } catch (e) {
    console.error("Failed to load user info", e);
  }
});
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  background-color: #ededed;
  position: relative;
}

/* 导航栏自定义 */
.custom-nav {
  --van-nav-bar-background: #ededed;
  --van-nav-bar-title-text-color: #000;
}
:deep(.van-nav-bar__content) {
  background-color: #ededed;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  padding-bottom: 20px; /* 只需要一点底部padding，因为footer-area是绝对定位覆盖的吗？不，我们用 flex 布局 */
  display: flex;
  flex-direction: column;
  gap: 15px;
  -webkit-overflow-scrolling: touch;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50%;
  color: #999;
  font-size: 14px;
}
.empty-icon-wrapper {
  width: 60px;
  height: 60px;
  background-color: #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.ai {
  align-self: flex-start;
}

.avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}

.avatar-text {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  background-color: #fff;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.content {
  display: flex;
  flex-direction: column;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px; /* 圆角加大 */
  font-size: 16px;
  line-height: 1.5;
  word-break: break-all;
  position: relative;
  max-width: 100%;
  box-shadow: 0 1px 1px rgba(0,0,0,0.05);
}

.bubble.user {
  background-color: #95ec69;
  color: #000;
  border-top-right-radius: 2px; /* 微信风格：右上角尖角 */
}

.bubble.ai {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 2px; /* 微信风格：左上角尖角 */
}

/* 气泡箭头 */
.bubble.user::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-left: 6px solid #95ec69;
}

.bubble.ai::after {
  content: '';
  position: absolute;
  left: -6px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid #fff;
}

/* 底部区域 */
.footer-area {
  background-color: #f7f7f7;
  border-top: 1px solid #e5e5e5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  z-index: 99;
}

.quick-actions {
  padding: 8px 10px;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  background-color: #f7f7f7;
  border-bottom: 1px solid #f0f0f0;
}
.action-chip {
  background: #fff;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  color: #576b95;
  white-space: nowrap;
  border: 1px solid #e0e0e0;
}

.input-toolbar {
  display: flex;
  align-items: flex-end;
  padding: 10px;
  gap: 10px;
}

.tool-icon {
  padding-bottom: 4px; /* Align with input bottom */
}

.input-box {
  flex: 1;
  background-color: #fff;
  border-radius: 20px;
  padding: 8px 15px;
  min-height: 40px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.native-input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  line-height: 1.4;
  resize: none;
  max-height: 120px;
  padding: 0;
  margin: 0;
}

.send-btn {
  height: 36px;
  padding: 0 16px;
  margin-bottom: 2px;
  font-size: 14px;
  border-radius: 18px;
}

/* 正在输入动画 */
.typing-indicator span {
  display: inline-block;
  width: 4px;
  height: 4px;
  background-color: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
  margin: 0 2px;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from, .fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
