import {Timestamp} from "firebase-admin/firestore";

export enum MessageType {
  TEXT = "TEXT",
  IMAGE = "IMAGE"
}

export enum ChatType {
  INDIVIDUAL = "INDIVIDUAL",
  GROUP = "GROUP"
}

export interface Message {
  message_id: string;
  sender_id: string;
  sender_name: string;
  receiver_id?: string;
  chat_id: string;
  content: string;
  message_type: MessageType;
  timestamp: Timestamp;
  is_delivered: boolean;
  is_read: boolean;
  read_by: string[];
  media_url?: string;
  delivered_at?: Timestamp;
}

export interface Chat {
  chat_id: string;
  participants: string[];
  participant_details?: {[userId: string]: UserDTO};
  chat_type: ChatType;
  last_message?: Message;
  last_message_timestamp?: Timestamp;
  unread_count: number;
  group_name?: string;
  group_image_url?: string;
  created_by: string;
  created_at: Timestamp;
}

export interface UserDTO {
  user_id: string;
  display_name: string;
  profile_image_url?: string;
  last_seen?: Timestamp;
  is_online: boolean;
  fcm_tokens: string[];
}

export interface NotificationRetry {
  retry_id: string;
  message_id: string;
  chat_id: string;
  recipient_ids: string[];
  failed_tokens: {[userId: string]: string[]};
  attempt_count: number;
  next_retry_at: Timestamp;
  last_error: string;
  created_at: Timestamp;
}

export interface NotificationFailure {
  failure_id: string;
  message_id: string;
  chat_id: string;
  recipient_ids: string[];
  failed_tokens: {[userId: string]: string[]};
  total_attempts: number;
  final_error: string;
  created_at: Timestamp;
  requires_manual_investigation: boolean;
}

export interface FCMPayload {
  notification: {
    title: string;
    body: string;
  };
  data: {
    chatId: string;
    messageId: string;
    senderId: string;
    senderName: string;
    messageType: string;
    timestamp: string;
    clickAction: string;
    mediaUrl?: string;
  };
  android: {
    priority: string;
    notification: {
      channelId: string;
      sound: string;
      defaultVibrateTimings: boolean;
    };
  };
}
