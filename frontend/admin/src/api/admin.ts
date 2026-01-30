import { request } from "./http";

export type ParkingSpaceStatus = "FREE" | "BOOKED" | "OCCUPIED" | "LOCKED";

export type ParkingSpace = {
  id: number;
  code: string;
  area: string;
  status: ParkingSpaceStatus;
  type: string;
};

export type ParkingOrderStatus = "BOOKED" | "ACTIVE" | "PAID" | "CANCELED";

export type ParkingOrder = {
  id: number;
  userId: number;
  parkingSpaceId: number;
  entryTime: string | null;
  exitTime: string | null;
  estimatedFee: string | null;
  paidFee: string | null;
  status: ParkingOrderStatus;
};

export async function getDashboardStats() {
  return request<Record<string, unknown>>({
    method: "GET",
    url: "/api/admin/v1/dashboard/stats"
  });
}

export async function createSpace(params: { code: string; area: string; status?: ParkingSpaceStatus; type: string }) {
  return request<ParkingSpace>({
    method: "POST",
    url: "/api/admin/v1/spaces",
    data: params
  });
}

export async function createSpacesBatch(params: Array<{ code: string; area: string; status?: ParkingSpaceStatus; type: string }>) {
  return request<number>({
    method: "POST",
    url: "/api/admin/v1/spaces/batch",
    data: params
  });
}

export async function forceUpdateSpaceStatus(id: number, status: ParkingSpaceStatus) {
  return request<ParkingSpace>({
    method: "PUT",
    url: `/api/admin/v1/spaces/${id}/status`,
    data: { status }
  });
}

export async function listOrdersByUser(userId: number, limit = 50) {
  return request<ParkingOrder[]>({
    method: "GET",
    url: "/api/admin/v1/orders",
    params: { userId, limit }
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
  isRead: number;
  createdAt: string;
};

export async function listMessages() {
  return request<Message[]>({
    method: "GET",
    url: "/api/admin/v1/messages"
  });
}

export async function getConversation(userId: number) {
  return request<Message[]>({
    method: "GET",
    url: `/api/admin/v1/messages/user/${userId}`
  });
}

export async function replyMessage(userId: number, content: string) {
  return request<void>({
    method: "POST",
    url: "/api/admin/v1/messages/reply",
    data: { userId, content }
  });
}

export async function readConversation(userId: number) {
  return request<void>({
    method: "PUT",
    url: `/api/admin/v1/messages/user/${userId}/read`
  });
}

export type ParkingLot = {
  id: number;
  name: string;
  address: string;
  totalSpots: number;
  availableSpots: number;
  latitude: number;
  longitude: number;
  status: "OPEN" | "CLOSED" | "MAINTENANCE";
  managerId: number | null;
};

export async function listParkingLots() {
  return request<ParkingLot[]>({
    method: "GET",
    url: "/api/admin/v1/parking-lots"
  });
}

export async function updateParkingLot(id: number, data: Partial<ParkingLot>) {
  return request<void>({
    method: "PUT",
    url: `/api/admin/v1/parking-lots/${id}`,
    data
  });
}

export async function createParkingLot(data: Partial<ParkingLot>) {
  return request<void>({
    method: "POST",
    url: "/api/admin/v1/parking-lots",
    data
  });
}
