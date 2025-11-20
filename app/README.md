# 📱 MELP Chat App - Modern Android Chat Demo

A modern chat application built using **Jetpack Compose**, leveraging a **modular Clean Architecture** and popular libraries like **Hilt**, **Retrofit**, **Room**, and **StateFlow**. This project provides a sophisticated UI/UX demo simulating a real chatting experience, complete with animated UIs, chat lists, message threads, and local message storage.

---

## 🚀 Key Features

### 💬 Chat List Screen
* **Data Source:** Loads real user data from **DummyJSON API** (`/users`).
* **UI Elements:** Displays profile image, username, last message preview (mocked), and online indicator (mocked).
* **Interactivity:** Shows unread message badge (mocked), "Typing..." indicator (simulated), and has **Pull-to-refresh** support.
* **Optimization:** Smooth scrolling with **pagination** (30 items per page).
* **Search:** Functionality with **300ms debounce** (matches name or last message text, case-insensitive).
* **Functionality:** Includes a Settings menu with Logout confirmation.
* **Behavior:** Ordering is exact based on API response (user ID). Chat items highlight when unread, and are marked as read upon opening.

### ✉️ Chat Detail Screen
* **Design:** **iMessage-style chat bubble UI** featuring gradient “sent” bubbles and frosted glass “received” bubbles.
* **Experience:** Subtle animations for message appearance.
* **Data:** Shows full message thread for the selected user. Messages are **stored locally in Room**.
* **Layout:** Smooth layout using `LazyColumn`.

---

## 🔧 Data Source Breakdown

| Category | Source | Details |
| :--- | :--- | :--- |
| **Real (API)** | DummyJSON API | User list (name, image, email, gender), User IDs, Basic user information. |
| **Mocked / Local** | Generated Locally | Last message preview, Online status, Typing indicator, Message history per chat, Timestamps, Unread message count. |

> **Note:** DummyJSON lacks chat/message APIs. Message data is created locally purely for UI demonstration purposes.

---

## 🧭 Application Flow

### 🚀 **App Launch**
1.  Fetches user profiles from DummyJSON API.
2.  Saves profiles to the **Room** database.
3.  Loads user list from the database into the UI.
4.  Applies search filter, pagination, and typing simulation.
5.  Displays chat list sorted by user ID (API order).

### 탭 **Chat Tap**
1.  `markChatAsRead(chatId)` is called, setting the unread count to `0`.
2.  Navigates to the Chat Detail Screen.
3.  Loads local messages from **Room**.
4.  UI displays the chat thread with animations.

### 🔄 **Pull-to-Refresh**
1.  A new API request is made for user profiles.
2.  The **Room** database is updated with fresh data.
3.  The Chat List is rebuilt, maintaining the original API ordering.

---

## 🏗 Modular Clean Architecture

The project adheres to a clear **modular Clean Architecture** structure: