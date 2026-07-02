# Direct Messaging (Chat) Frontend Handoff

## Overview

Persisted, real-time direct messaging between any two users (owner ↔ sitter, rehomer ↔ applicant, foster ↔ admin, etc.). Messages are stored in the database and delivered live over the existing WebSocket connection; if the recipient is offline, the message is still saved and shows up the next time they fetch or reconnect.

Base URL:
- `http://localhost:8080/api/chat`
- WebSocket: `ws://localhost:8080/ws?token={accessToken}` (same endpoint already used for notifications — see below)

## Current Frontend State (read this first)

- `src/services/websocketService.ts` and `src/hooks/useWebSocket.ts` are already correctly wired: they connect to `${VITE_WS_URL}?token=${accessToken}`, handle reconnect/heartbeat, and dispatch parsed messages to listeners. **No changes needed there** — just add a `case 'CHAT_MESSAGE':` branch wherever messages are consumed (see envelope shape below).
- `src/components/community/ChatSection.tsx` ("Donor Chat" tab) is currently **100% local mock state** — hardcoded fake conversations/messages, no API calls at all. This is the main integration work: replace the mock `chats` array with `GET /api/chat/conversations`, wire `sendMessage` to `POST /api/chat/conversations/{id}/messages`, and listen for `CHAT_MESSAGE` WebSocket events to append incoming messages live instead of only showing what the current user typed.
- There is no `chatAPI` in `lib/api.ts` yet — you'll need to add one (see suggested shape below).

## Authentication

All `/api/chat/**` endpoints require a JWT (`Authorization: Bearer {token}`) — there's no public/anonymous access to any of them.

The WebSocket connection authenticates via the `token` query param (already implemented in `websocketService.ts`). An invalid or missing token gets the connection closed immediately with code `1008` and reason `"Missing or invalid token"` — if you ever see WS connections dying right after opening, check the token first.

## Main Data Shapes

### ChatResponse (conversation, for the inbox list)

```json
{
  "id": 1,
  "otherUser": {
    "id": 5,
    "name": "Second User",
    "email": "seconduser1@example.com"
  },
  "lastMessage": "Yes, still available!",
  "unreadCount": 1,
  "lastMessageAt": "2026-07-02 23:13:50",
  "lastMessageTimeAgo": "Just now",
  "createdAt": "2026-07-02 23:13:49",
  "isActive": true
}
```

- `otherUser` is always the *other* participant relative to whoever is asking — the same conversation looks different depending on who's fetching it.
- `otherUser` only reliably has `id`/`name`/`email` populated (same limitation as the Community Posts and Rehoming `User` sub-objects — no avatar/bio/online-status data exists on the `User` entity yet).
- `unreadCount` counts messages sent *to* the requester that they haven't marked read — it's per-viewer, not a global count on the conversation.
- `recentMessages` (present in the DTO) is **not populated** by the list endpoint — always fetch `GET /api/chat/conversations/{id}/messages` separately for the actual thread.

### MessageResponse

```json
{
  "id": 1,
  "sender": { "id": 4, "name": "Existing Owner", "email": "existingowner1@example.com" },
  "receiver": { "id": 5, "name": "Second User", "email": "seconduser1@example.com" },
  "content": "Hi, is Bella still available?",
  "isRead": false,
  "createdAt": "2026-07-02 23:13:49",
  "timeAgo": "Just now",
  "readAt": null
}
```

- `messageType`, `fileUrl`, `fileName`, `fileSize` exist on this DTO (shared with an earlier, unused chat design) but are **never populated** — this MVP is text-only, no attachments. Treat them as always absent.

## API Contract

### 1) List my conversations

- `GET /api/chat/conversations`
- Returns an array of `ChatResponse`, most recently active first (by `lastMessageAt`, falling back to `createdAt` for conversations with no messages yet).
- Frontend: populate the inbox/conversation-list panel. Show `otherUser.name`, `lastMessage`, `lastMessageTimeAgo`, and a badge for `unreadCount` when > 0.

### 2) Start a new conversation

- `POST /api/chat/conversations`
- Body: `{ "participantId": 5 }`
- **Idempotent** — if a conversation between you and that user already exists, it returns the existing one instead of creating a duplicate. Safe to call every time a user clicks "Message" on someone's profile without checking first.
- Returns `400` with `"You cannot start a conversation with yourself"` or `"User not found with id: {id}"` as appropriate.

### 3) Get messages in a conversation

- `GET /api/chat/conversations/{id}/messages`
- Returns the full thread as an array of `MessageResponse`, oldest first.
- Only participants can view it — `400` with `"You are not a participant in this conversation"` otherwise (not a 403 — same "authenticated but not authorized" convention used by the rehoming/community modules).
- No pagination yet — for very long threads this returns everything in one call.

### 4) Send a message

- `POST /api/chat/conversations/{id}/messages`
- Body: `{ "content": "Hi, is Bella still available?" }` (max 2000 chars)
- Persists the message, updates the conversation's `lastMessageAt`/`lastMessagePreview` (truncated to 100 chars), and pushes a `CHAT_MESSAGE` WebSocket event to the other participant **if they're currently connected**. If they're offline, nothing is lost — it's just sitting in the DB for them to fetch next time.
- Returns the created `MessageResponse`.

### 5) Mark a conversation as read

- `PATCH /api/chat/conversations/{id}/read`
- Marks every message in that conversation sent *to* you as read (sets `isRead: true`, `readAt: now`). Call this when the user opens/focuses a conversation.
- No request body.

### 6) Get total unread count

- `GET /api/chat/unread-count`
- Returns `{ "unreadCount": 3 }` — total unread messages across *all* your conversations. Use this for a badge on the chat/inbox nav icon.

## WebSocket: Real-Time Message Delivery

Connect to the same `/ws` endpoint already used for notifications (`websocketService.ts` already does this correctly). When someone sends you a message while you're connected, you'll receive:

```json
{
  "type": "CHAT_MESSAGE",
  "conversationId": 1,
  "senderId": 4,
  "senderName": "Existing Owner",
  "content": "Hi, is Bella still available?",
  "timestamp": "2026-07-02T23:13:49.568591"
}
```

Frontend: in `websocketService.ts`'s `handleMessage` switch (or wherever you consume messages), add a case for `CHAT_MESSAGE` that:
- Appends the message to the open conversation's message list if `conversationId` matches what's currently open, and/or
- Bumps that conversation to the top of the inbox list and increments its unread badge if it's not currently open.

This event is **delivery-only** — it doesn't replace fetching `GET /api/chat/conversations/{id}/messages` for history; it's how you learn about new messages that arrive while the tab is open. There's no `MESSAGE_READ` push event yet — read-state changes are only visible by refetching.

## Error Handling

Same convention as the rest of the API: `{"status":"error","message":"..."}`, mostly `400` for ownership/participation violations (not `403`) since these come from application logic, not the security filter. Truly unauthenticated requests to any `/api/chat/**` endpoint get a plain `403` before reaching app logic.

Common messages: `"Conversation not found with id: {id}"`, `"You are not a participant in this conversation"`, `"You cannot start a conversation with yourself"`, `"User not found with id: {id}"`, `"Authentication required"`.

## Suggested `chatAPI` shape for `lib/api.ts`

```ts
export const chatAPI = {
  getConversations: () => api.get('/api/chat/conversations'),
  startConversation: (participantId: number) => api.post('/api/chat/conversations', { participantId }),
  getMessages: (conversationId: number) => api.get(`/api/chat/conversations/${conversationId}/messages`),
  sendMessage: (conversationId: number, content: string) =>
    api.post(`/api/chat/conversations/${conversationId}/messages`, { content }),
  markAsRead: (conversationId: number) => api.patch(`/api/chat/conversations/${conversationId}/read`),
  getUnreadCount: () => api.get('/api/chat/unread-count'),
};
```

## Known Gaps (not implemented)

- No message pagination (a very long thread loads in full every time).
- No typing indicators, delivery receipts, or "seen" push events — only the `CHAT_MESSAGE` new-message push and pull-based read state.
- No group chats — conversations are strictly one-to-one (`participant1`/`participant2`).
- No message editing or deletion.
- No file/image attachments, despite unused fields on `MessageResponse` suggesting otherwise.
- `otherUser`/`sender`/`receiver` only carry `id`/`name`/`email` — no avatar or online-status data.
