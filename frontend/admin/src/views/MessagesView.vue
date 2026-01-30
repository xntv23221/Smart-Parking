<template>
  <div class="messages-view">
    <div class="conversation-list">
      <div class="list-header">消息列表</div>
      <div 
        v-for="user in conversations" 
        :key="user.userId" 
        class="conversation-item"
        :class="{ active: currentUserId === user.userId }"
        @click="selectUser(user.userId)"
      >
        <div class="user-avatar">
          <el-avatar :icon="UserFilled" />
        </div>
        <div class="user-info">
          <div class="user-name">用户 #{{ user.userId }}</div>
          <div class="last-message">{{ user.lastMessage }}</div>
        </div>
        <div class="time">{{ formatTime(user.lastTime) }}</div>
        <div v-if="user.unreadCount > 0" class="badge">{{ user.unreadCount }}</div>
      </div>
    </div>
    
    <div class="chat-area">
      <template v-if="currentUserId">
        <div class="chat-header">
          与 用户 #{{ currentUserId }} 的对话
        </div>
        <div class="chat-messages" ref="chatBox">
          <div 
            v-for="msg in currentMessages" 
            :key="msg.messageId" 
            class="message-row"
            :class="{ 'me': msg.senderRole === 'admin' }"
          >
            <div class="avatar-container">
               <el-avatar :src="getAvatar(msg)" />
            </div>
            <div class="content-wrapper">
              <div class="message-content">
                {{ msg.content }}
              </div>
              <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input 
            v-model="inputContent" 
            type="textarea" 
            :rows="3" 
            placeholder="输入回复内容..."
            @keydown.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
          </div>
        </div>
      </template>
      <div v-else class="empty-state">
        请选择一个会话
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { UserFilled } from '@element-plus/icons-vue';
import { listMessages, getConversation, replyMessage, readConversation, type Message } from '../api/admin';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';

const conversations = ref<any[]>([]);
const currentUserId = ref<number | null>(null);
const currentMessages = ref<Message[]>([]);
const inputContent = ref('');
const sending = ref(false);
const chatBox = ref<HTMLElement | null>(null);
const pollTimer = ref<number | null>(null);

onMounted(() => {
  loadConversations();
  startPolling();
});

onUnmounted(() => {
  stopPolling();
});

function startPolling() {
  stopPolling();
  pollTimer.value = window.setInterval(() => {
    // Refresh conversation list to see new messages
    loadConversations(true);
    // Refresh current chat if open
    if (currentUserId.value) {
      loadMessages(currentUserId.value, true);
    }
  }, 3000);
}

function stopPolling() {
  if (pollTimer.value) {
    clearInterval(pollTimer.value);
    pollTimer.value = null;
  }
}

async function loadConversations(isPolling = false) {
  try {
    const msgs = await listMessages();
    // Group by userId (senderId if sender is user, receiverId if receiver is user)
    const map = new Map<number, any>();
    
    msgs.forEach(msg => {
      const isFromUser = msg.senderRole === 'user';
      const userId = isFromUser ? msg.senderId : msg.receiverId;
      
      if (!map.has(userId)) {
        map.set(userId, {
          userId,
          lastMessage: '',
          lastTime: '',
          unreadCount: 0,
          rawTime: 0
        });
      }
      
      const conv = map.get(userId);
      const msgTime = new Date(msg.createdAt).getTime();
      if (msgTime > conv.rawTime) {
        conv.rawTime = msgTime;
        conv.lastTime = msg.createdAt;
        conv.lastMessage = msg.content;
      }
      
      if (isFromUser && msg.isRead === 0) {
        conv.unreadCount++;
      }
    });
    
    conversations.value = Array.from(map.values()).sort((a, b) => b.rawTime - a.rawTime);
  } catch (e) {
    if (!isPolling) console.error(e);
  }
}

async function selectUser(userId: number) {
  currentUserId.value = userId;
  await loadMessages(userId);
  scrollToBottom();
}

async function loadMessages(userId: number, isPolling = false) {
  try {
    const res = await getConversation(userId);
    const shouldScroll = !isPolling || res.length > currentMessages.value.length;
    currentMessages.value = res;
    
    // Only mark as read if not polling or if we are active
    // If polling, we might not want to continuously mark read if window not focused?
    // For simplicity, just mark read.
    if (!isPolling || shouldScroll) {
       await readConversation(userId);
       // Update local unread count
       const conv = conversations.value.find(c => c.userId === userId);
       if (conv) conv.unreadCount = 0;
    }

    if (shouldScroll) {
      scrollToBottom();
    }
  } catch (e) {
    if (!isPolling) console.error(e);
  }
}

async function sendMessage() {
  if (!inputContent.value.trim() || !currentUserId.value) return;
  
  sending.value = true;
  try {
    await replyMessage(currentUserId.value, inputContent.value);
    inputContent.value = '';
    await loadMessages(currentUserId.value);
    scrollToBottom();
    // Update last message in list
    loadConversations(); 
  } catch (e) {
    ElMessage.error('发送失败');
  } finally {
    sending.value = false;
  }
}

function getAvatar(msg: Message) {
  if (msg.senderRole === 'user') {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=User${msg.senderId}`;
  } else if (msg.senderRole === 'admin') {
    return `https://api.dicebear.com/7.x/bottts/svg?seed=Admin`;
  } else {
    return `https://api.dicebear.com/7.x/bottts/svg?seed=Manager${msg.senderId}`;
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) {
      chatBox.value.scrollTop = chatBox.value.scrollHeight;
    }
  });
}

function formatTime(time: string) {
  return dayjs(time).format('MM-DD HH:mm');
}
</script>

<style scoped>
.messages-view {
  display: flex;
  height: calc(100vh - 100px);
  border: 1px solid var(--el-border-color);
  background: #fff;
}

.conversation-list {
  width: 280px;
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 16px;
  font-weight: bold;
  border-bottom: 1px solid var(--el-border-color);
}

.conversation-item {
  padding: 12px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.conversation-item:hover {
  background-color: var(--el-fill-color-light);
}

.conversation-item.active {
  background-color: var(--el-color-primary-light-9);
}

.user-avatar {
  margin-right: 12px;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.last-message {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  position: absolute;
  top: 12px;
  right: 12px;
}

.badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background-color: var(--el-color-danger);
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 16px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color);
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.message-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message-row.me {
  flex-direction: row-reverse;
}

.avatar-container {
  margin: 0 10px;
  flex-shrink: 0;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-row.me .content-wrapper {
  align-items: flex-end;
}

.message-content {
  padding: 10px 14px;
  background-color: #fff;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  word-break: break-all;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message-row.me .message-content {
  background-color: #95ec69; /* WeChat Green */
  color: #000;
}

/* Chat bubble arrow */
.message-content::before {
  content: '';
  position: absolute;
  top: 10px;
  border: 6px solid transparent;
}

.message-row:not(.me) .message-content::before {
  left: -12px;
  border-right-color: #fff;
}

.message-row.me .message-content::before {
  right: -12px;
  border-left-color: #95ec69;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  padding: 0 4px;
}

.chat-input {
  padding: 20px;
  border-top: 1px solid var(--el-border-color);
  background-color: #fff;
}

.input-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}
</style>
