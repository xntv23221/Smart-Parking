import { request } from "./http";

export type ParkingOrderStatus = 0 | 1 | 2 | 3;

export const ParkingOrderStatusText: Record<number, string> = {
  0: "已预约",
  1: "停车中",
  2: "已完成",
  3: "已取消"
};

export type ParkingOrder = {
  recordId: number;
  userId: number;
  vehicleId?: number;
  lotId?: number;
  spotId?: number;
  entryTime: string | null;
  exitTime: string | null;
  durationMinutes?: number;
  amount?: number | string | null;
  paidAmount?: number | string | null;
  status: ParkingOrderStatus;
  paymentStatus?: number;
  paymentMethod?: string;
  createdAt?: string;
};

export type UserWallet = {
  id: number;
  userId: number;
  balance: number | string;
  version: number;
};

export type ParkingLot = {
  lotId: number;
  name: string;
  longitude: number;
  latitude: number;
  totalSpots: number;
  availableSpots: number;
  managerId?: number;
  type?: number; // 0=Public, 1=Commercial
};





export async function createRecord(data: { lotId: number; vehicleId?: number; spotId?: number }) {
  return request<any>({
    method: "POST",
    url: "/api/client/v1/records/entry",
    data
  });
}

export async function payRecord(data: { recordId: number; method: string }) {
  return request<any>({
    method: "POST",
    url: "/api/client/v1/records/pay",
    data
  });
}

export async function getParkingLots() {
  return request<ParkingLot[]>({
    method: "GET",
    url: "/api/client/v1/parking-lots"
  });
}

export async function getParkingLotByName(name: string) {
  return request<ParkingLot>({
    method: "GET",
    url: "/api/client/v1/parking-lots/by-name",
    params: { name }
  });
}

export async function register(data: any) {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/auth/register",
    data
  });
}

export async function getAvailableSpaces() {
  return request<{ availableCount: number }>({
    method: "GET",
    url: "/api/client/v1/spaces/available"
  });
}

export async function chatAssistant(message: string) {
  return request<{ answer: string }>({
    method: "POST",
    url: "/api/client/v1/assistant/chat",
    data: { message }
  });
}

export type Invoice = {
  invoiceId: number;
  userId: number;
  amount: number;
  title: string;
  taxNo: string;
  status: number;
  createdAt: string;
};

export type MonthlyCard = {
  cardId: number;
  userId: number;
  lotId: number;
  startDate: string;
  endDate: string;
  plateNumber: string;
  status: number;
  createdAt: string;
};

export async function getMyInvoices() {
  return request<Invoice[]>({
    method: "GET",
    url: "/api/client/v1/invoices"
  });
}

export async function applyInvoice(data: { amount: number; title: string; taxNo: string }) {
  return request<Invoice>({
    method: "POST",
    url: "/api/client/v1/invoices",
    data
  });
}

export async function getMyMonthlyCards() {
  return request<MonthlyCard[]>({
    method: "GET",
    url: "/api/client/v1/monthly-cards"
  });
}

export async function buyMonthlyCard(data: { lotId: number; plateNumber: string; months: number }) {
  return request<MonthlyCard>({
    method: "POST",
    url: "/api/client/v1/monthly-cards",
    data
  });
}

export async function updateProfile(data: { nickname?: string; email?: string; avatarUrl?: string }) {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/user/profile",
    data
  });
}

export async function updatePassword(password: string) {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/user/password",
    data: { password }
  });
}

export async function requestPhoneChange(newPhone: string) {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/user/phone/change-request",
    data: { newPhone }
  });
}

export async function getAuditRequests() {
  return request<any[]>({
    method: "GET",
    url: "/api/client/v1/user/audit-requests"
  });
}

export async function getProfile() {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/user/profile"
  });
}

export type Message = {
  messageId: number;
  senderId: number;
  senderRole: string;
  receiverId: number;
  receiverRole: string;
  content: string;
  type: string;
  isRead: boolean;
  createdAt: string;
};

export async function getMyMessages() {
  return request<Message[]>({
    method: "GET",
    url: "/api/client/v1/messages"
  });
}

export async function getConversation(targetId = 0, targetRole = "admin") {
  return request<Message[]>({
    method: "GET",
    url: "/api/client/v1/messages/conversation",
    params: { targetId, targetRole }
  });
}

export async function sendMessage(content: string, receiverId = 0, receiverRole = "admin") {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/messages/send",
    data: { content, receiverId, receiverRole }
  });
}

export async function readMessage(id: number) {
  return request<void>({
    method: "POST",
    url: `/api/client/v1/messages/${id}/read`
  });
}

export async function readAllMessages() {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/messages/read-all"
  });
}

export type WalletLog = {
  logId: number;
  walletId: number;
  amount: number;
  type: number; // 1=Recharge, 2=Payment, 3=Refund
  balanceAfter: number;
  orderId: number | null;
  remark: string;
  createdAt: string;
};

export async function getMyWallet() {
  return request<UserWallet>({
    method: "GET",
    url: "/api/client/v1/wallet"
  });
}

export async function getWalletHistory() {
  return request<WalletLog[]>({
    method: "GET",
    url: "/api/client/v1/wallet/history"
  });
}

export async function recharge(amount: number) {
  return request<UserWallet>({
    method: "POST",
    url: "/api/client/v1/wallet/recharge",
    data: { amount }
  });
}

export async function withdraw(data: { amount: number; accountInfo: string }) {
  return request<UserWallet>({
    method: "POST",
    url: "/api/client/v1/wallet/withdraw",
    data
  });
}

export type Vehicle = {
  vehicleId: number;
  userId: number;
  plateNumber: string;
  brand: string;
  model: string;
  color: string;
  vehicleType: number;
  isDefault: boolean;
};

export async function getMyVehicles() {
  return request<Vehicle[]>({
    method: "GET",
    url: "/api/client/v1/vehicles"
  });
}

export async function addVehicle(data: Partial<Vehicle>) {
  return request<void>({
    method: "POST",
    url: "/api/client/v1/vehicles",
    data
  });
}

export async function deleteVehicle(id: number) {
  return request<void>({
    method: "DELETE",
    url: `/api/client/v1/vehicles/${id}`
  });
}

export async function setDefaultVehicle(id: number) {
  return request<void>({
    method: "POST",
    url: `/api/client/v1/vehicles/${id}/default`
  });
}




export async function entry(orderId: number, time: string) {
  // 注意：后端 entry 接口实际上需要 lotId 和 vehicleId。
  // 这里为了兼容旧代码，暂时保留，但调用可能会失败，除非后端做了适配。
  // 正确的做法是传入 { lotId, vehicleId }
  return request<ParkingOrder>({
    method: "POST",
    url: "/api/client/v1/records/entry",
    data: { orderId, time }
  });
}

export async function book(lotId: number, vehicleId?: number) {
  return request<ParkingOrder>({
    method: "POST",
    url: "/api/client/v1/records/book",
    data: { lotId, vehicleId }
  });
}

export async function cancel(recordId: number) {
  return request<ParkingOrder>({
    method: "POST",
    url: "/api/client/v1/records/cancel",
    data: { recordId }
  });
}

export async function exit(recordId: number, time: string) {
  return request<ParkingOrder>({
    method: "POST",
    url: "/api/client/v1/records/exit",
    data: { recordId, time }
  });
}

export async function pay(recordId: number) {
  return request<ParkingOrder>({
    method: "POST",
    url: "/api/client/v1/records/pay",
    data: { recordId, method: "ALIPAY" } // Default method
  });
}

export async function listOrders(limit = 20) {
  return request<ParkingOrder[]>({
    method: "GET",
    url: "/api/client/v1/records",
    params: { limit }
  });
}

// --- 高德地图 Web 服务 API 代理 ---

/**
 * IP 定位
 */
export async function amapIpLocate() {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/ip"
  });
}

/**
 * 天气查询
 * @param city 城市 adcode
 */
export async function amapWeather(city: string) {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/weather",
    params: { city }
  });
}

/**
 * 地理编码
 */
export async function amapGeocode(address: string, city = "哈尔滨") {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/geocode",
    params: { address, city }
  });
}

/**
 * 逆地理编码
 */
export async function amapRegeocode(location: string) {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/regeocode",
    params: { location }
  });
}

/**
 * 驾车路径规划
 */
export async function amapDriving(origin: string, destination: string, strategy = 0) {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/direction/driving",
    params: { origin, destination, strategy }
  });
}

/**
 * 关键字搜索
 */
export async function amapPlaceText(keywords: string, city = "哈尔滨") {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/place/text",
    params: { keywords, city }
  });
}

/**
 * 周边搜索
 */
export async function amapPlaceAround(location: string, keywords: string, radius = 2000) {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/place/around",
    params: { location, keywords, radius }
  });
}

/**
 * 输入提示
 */
export async function amapInputTips(keywords: string, city = "哈尔滨") {
  return request<any>({
    method: "GET",
    url: "/api/client/v1/map/assistant/inputtips",
    params: { keywords, city }
  });
}
