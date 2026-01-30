<template>
  <div class="page">
    <van-nav-bar :title="navTitle" right-text="全部已读" @click-right="onReadAll" />
    
    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="全部" name="all" />
      <van-tab title="系统" name="system" />
      <van-tab title="停车场" name="parking" />
      <van-tab title="会话" name="private" />
    </van-tabs>

    <!-- 列表模式 -->
    <van-pull-refresh v-if="activeTab !== 'private'" v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-if="filteredMessages.length === 0 && !loading" class="empty-state">
          <van-empty description="暂无消息" />
        </div>
        
        <van-swipe-cell v-for="item in filteredMessages" :key="item.messageId">
          <van-cell 
            :title="getMessageTitle(item)" 
            :label="getMessageLabel(item)"
            :class="{ 'unread': !item.isRead }"
            @click="onMessageClick(item)"
          >
            <template #icon>
              <div class="icon-box" :style="{ backgroundColor: getIconColor(item.type) }">
                <van-icon :name="getIconName(item.type)" color="#fff" size="20" />
              </div>
            </template>
            <template #right-icon>
              <van-badge v-if="!item.isRead" dot />
            </template>
          </van-cell>
        </van-swipe-cell>
      </van-list>
    </van-pull-refresh>

    <!-- 聊天模式 -->
    <div v-else class="chat-container">
      <div class="chat-header-bar">
        <span>{{ getChatHeaderTitle() }}</span>
        <van-button 
          v-if="targetRole !== 'admin'" 
          size="mini" 
          type="primary" 
          plain 
          @click="resetToAdmin"
        >
          切换回平台客服
        </van-button>
      </div>
      <div class="chat-list" ref="chatListRef">
        <div v-if="chatMessages.length === 0" class="empty-chat">
          <van-empty description="暂无对话，快联系客服吧" />
        </div>
        <div 
          v-for="msg in chatMessages" 
          :key="msg.messageId" 
          class="chat-bubble-row"
          :class="{ 'me': isMe(msg) }"
        >
          <div class="avatar-container">
             <van-image 
               round 
               width="40" 
               height="40" 
               :src="getAvatar(msg)" 
             />
          </div>
          <div class="content-wrapper">
            <div class="chat-bubble">
              {{ msg.content }}
            </div>
            <div class="chat-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>
      <div class="chat-input-bar">
        <van-field 
          v-model="inputContent" 
          center 
          clearable 
          placeholder="输入消息..." 
          :disabled="sending"
        >
          <template #button>
            <van-button size="small" type="primary" @click="onSend" :loading="sending" :disabled="!inputContent.trim()">发送</van-button>
          </template>
        </van-field>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { showToast } from "vant";
import { getMyMessages, readMessage, readAllMessages, sendMessage, getConversation, getProfile, type Message } from "../api/client";
import { getErrorMessage } from "../utils/error";
import { formatTime } from "../utils/format";

const route = useRoute();
const activeTab = ref("all");
const messages = ref<Message[]>([]);
const chatMessages = ref<Message[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const inputContent = ref("");
const sending = ref(false);
const chatListRef = ref<HTMLElement | null>(null);
const pollTimer = ref<number | null>(null);
const userAvatar = ref<string>("");

// Chat Target
const targetId = ref(0);
const targetRole = ref("admin");

const navTitle = computed(() => {
  if (activeTab.value === 'private') {
    return targetRole.value === 'admin' ? '联系客服' : '联系管理';
  }
  return '消息中心';
});

const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return messages.value;
  return messages.value.filter(m => m.type === activeTab.value);
});

onMounted(async () => {
  if (route.query.targetId) {
    targetId.value = Number(route.query.targetId);
    if (route.query.targetRole) {
      targetRole.value = String(route.query.targetRole);
    }
    activeTab.value = 'private';
  }
  
  // Load user profile for avatar
  try {
      const profile = await getProfile();
      if (profile && profile.avatarUrl) {
          userAvatar.value = profile.avatarUrl;
      }
  } catch (e) {
      console.error(e);
  }

  // Start polling
  startPolling();
});

onUnmounted(() => {
  stopPolling();
});

const startPolling = () => {
  stopPolling();
  pollTimer.value = window.setInterval(() => {
    if (activeTab.value === 'private') {
      loadConversation(true);
    } else {
      // Optional: Poll for list updates if needed, but might disturb user interaction
      // For now, only poll chat
    }
  }, 3000);
};

const stopPolling = () => {
  if (pollTimer.value) {
    clearInterval(pollTimer.value);
    pollTimer.value = null;
  }
};

watch(activeTab, (val) => {
  if (val === 'private') {
    loadConversation();
  }
});

const scrollToBottom = () => {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
    }
  });
};

const loadConversation = async (isPolling = false) => {
  try {
    const res = await getConversation(targetId.value, targetRole.value);
    // Only scroll if new messages or first load
    const shouldScroll = !isPolling || res.length > chatMessages.value.length;
    chatMessages.value = res;
    if (shouldScroll) {
      scrollToBottom();
    }
  } catch (error) {
    if (!isPolling) {
      showToast(getErrorMessage(error, "加载对话失败"));
    }
  }
};

const resetToAdmin = () => {
  targetId.value = 0;
  targetRole.value = 'admin';
  loadConversation();
};

const getChatHeaderTitle = () => {
  if (targetRole.value === 'admin') return '正在与平台客服对话';
  if (targetRole.value === 'manager') return '正在与停车场管理员对话';
  return '对话中';
};

const getMessageTitle = (item: Message) => {
  if (item.type === 'private') {
    if (item.senderRole === 'admin') return '平台客服';
    if (item.senderRole === 'manager') return '停车场管理员';
    if (item.senderRole === 'user') return `我 -> ${item.receiverRole === 'admin' ? '平台客服' : '停车场管理员'}`;
  }
  return item.content;
};

const getMessageLabel = (item: Message) => {
  const time = formatTime(item.createdAt);
  if (item.type === 'private') {
    return `${item.content} - ${time}`;
  }
  return time;
};

const isMe = (msg: Message) => {
  return msg.senderRole && msg.senderRole.toLowerCase() === 'user';
};

const getAvatar = (msg: Message) => {
  if (isMe(msg)) {
    // Use user's real avatar if available, otherwise fallback to default cat image (same as ProfileView)
    return userAvatar.value || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';
  } else if (msg.senderRole === 'admin') {
    return `https://api.dicebear.com/7.x/bottts/svg?seed=Admin`;
  } else {
    // manager
    return `https://api.dicebear.com/7.x/bottts/svg?seed=Manager${msg.senderId}`;
  }
};

const onMessageClick = (item: Message) => {
  onRead(item);
  // If user clicks a message from admin or manager, go to chat
  if (item.senderRole === 'admin' || item.senderRole === 'manager') {
      targetId.value = item.senderId;
      targetRole.value = item.senderRole;
      activeTab.value = 'private';
  }
};

const onSend = async () => {
  if (!inputContent.value.trim()) return;
  sending.value = true;
  try {
    await sendMessage(inputContent.value, targetId.value, targetRole.value);
    inputContent.value = "";
    await loadConversation();
  } catch (error) {
    showToast(getErrorMessage(error, "发送失败"));
  } finally {
    sending.value = false;
  }
};

const onLoad = async () => {
  if (refreshing.value) {
    messages.value = [];
    refreshing.value = false;
  }

  try {
    const res = await getMyMessages();
    messages.value = res;
    finished.value = true;
  } catch (error) {
    showToast(getErrorMessage(error, "加载失败"));
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

const onRefresh = () => {
  finished.value = false;
  loading.value = true;
  onLoad();
};

const onRead = async (item: Message) => {
  if (item.isRead) return;
  try {
    await readMessage(item.messageId);
    item.isRead = true;
  } catch {
    // ignore
  }
};

const onReadAll = async () => {
  try {
    await readAllMessages();
    messages.value.forEach(m => m.isRead = true);
    showToast("已全部标记为已读");
  } catch (error) {
    showToast(getErrorMessage(error, "操作失败"));
  }
};

const getIconColor = (type: string) => {
  switch (type) {
    case 'system': return '#ee0a24';
    case 'parking': return '#1989fa';
    case 'private': return '#07c160';
    default: return '#969799';
  }
};

const getIconName = (type: string) => {
  switch (type) {
    case 'system': return 'volume-o';
    case 'parking': return 'shop-o';
    case 'private': return 'chat-o';
    default: return 'comment-o';
  }
};

const formatTime = (iso: string | any) => {
  if (!iso) return '-';
  if (typeof iso === 'string') {
    return iso.replace('T', ' ').slice(0, 16);
  }
  // Handle array or other formats via Date
  return new Date(iso).toLocaleString();
};
</script>

<style scoped>
.page {
  padding-bottom: 50px;
  background-color: #f7f8fa;
  min-height: 100vh;
}

.empty-state {
  padding-top: 50px;
}

.icon-box {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.unread :deep(.van-cell__title) {
  font-weight: bold;
}

/* Chat Styles */
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background-color: #f7f8fa;
}

.chat-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background-color: #e8f3ff;
  font-size: 14px;
  color: #1989fa;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 60px;
}

.empty-chat {
  padding-top: 50px;
}

.chat-bubble-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.chat-bubble-row.me {
  flex-direction: row-reverse;
}

.avatar-container {
  flex-shrink: 0;
  margin: 0 8px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.chat-bubble-row.me .content-wrapper {
  align-items: flex-end;
}

.chat-bubble {
  padding: 10px 14px;
  background-color: #fff;
  border-radius: 8px;
  font-size: 15px;
  line-height: 1.5;
  color: #323233;
  word-break: break-all;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.chat-bubble-row.me .chat-bubble {
  background-color: #95ec69; /* WeChat Green */
  color: #000;
}

/* Optional: Chat bubble arrow */
.chat-bubble::before {
  content: '';
  position: absolute;
  top: 14px;
  border: 6px solid transparent;
}

.chat-bubble-row:not(.me) .chat-bubble::before {
  left: -12px;
  border-right-color: #fff;
}

.chat-bubble-row.me .chat-bubble::before {
  right: -12px;
  border-left-color: #95ec69;
}

.chat-time {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
  padding: 0 4px;
}

.chat-input-bar {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  background-color: #fff;
  border-top: 1px solid #ebedf0;
  padding: 8px 0;
  z-index: 99;
}
</style>
