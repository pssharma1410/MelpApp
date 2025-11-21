package com.example.melpapp.ui.chatlist

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melpapp.domain.model.RecentChat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

val LightBackground = Color(0xFFFFFFFF)
val LightHeader = Color(0xFFF7F7F7)
val ElectricTeal = Color(0xFF00BFA5)
val TextPrimary = Color(0xFF1E1E1E)
val TextSecondary = Color(0xFF6A6A6A)
val ErrorBackground = Color(0xFFFFEEEE)
val ErrorText = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onChatClick: (Int, String, String?) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()

    val refreshState = rememberSwipeRefreshState(
        isRefreshing = state.isRefreshing
    )

    val lazyListState = rememberLazyListState()

    var isMenuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chat App",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = ElectricTeal
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier
                                .background(LightHeader)
                                .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = ElectricTeal)
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = LightBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchBar(
                searchText = state.searchText,
                onQueryChange = { viewModel.onSearchChange(it) },
                apiResponseTimeMs = state.apiResponseTimeMs
            )

            state.errorMessage?.let { msg ->
                Surface(
                    color = ErrorBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = ErrorText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            SwipeRefresh(
                state = refreshState,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.displayedChats) { index, chat ->

                        ChatRow(
                            chat = chat,
                            isTyping = typingUsers.contains(chat.id),
                            onClick = {
                                viewModel.markChatAsRead(chat.id)
                                onChatClick(chat.id, chat.name, chat.avatarUrl)
                            }
                        )

                        Divider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = TextSecondary.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        // 🔥🔥🔥 Pagination Without snapshotFlow — 100% WORKING
        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisible = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val total = state.displayedChats.size
                val threshold = 5

                if (lastVisible == null) false
                else lastVisible >= total - 1 - threshold
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                viewModel.loadMore()
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = { showLogoutDialog = false },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LightHeader,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(12.dp),
        title = { Text("Confirm Logout") },
        text = {
            Text("Are you sure you want to log out of MELD? Your session will be terminated.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    (context as? Activity)?.finish()
                }
            ) {
                Text("Logout", color = ErrorText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ElectricTeal)
            }
        }
    )
}

@Composable
private fun SearchBar(
    searchText: String,
    onQueryChange: (String) -> Unit,
    apiResponseTimeMs: Long?
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search chats...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ElectricTeal) },
        singleLine = true
    )

    if (apiResponseTimeMs != null) {
        Text(
            text = "Loaded in: ${apiResponseTimeMs}ms",
            modifier = Modifier.padding(start = 16.dp),
            color = TextSecondary
        )
    }
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minute = 60_000L
    val hour = minute * 60
    val day = hour * 24

    return when {
        diff < minute -> "Just now"
        diff < hour -> "${diff / minute} min ago"
        diff < day -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        diff < 2 * day -> "Yesterday"
        diff < 7 * day -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun ChatRow(
    chat: RecentChat,
    isTyping: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chat.avatarUrl,
            contentDescription = chat.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    if (chat.isOnline) 2.dp else 0.dp,
                    ElectricTeal,
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                chat.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextPrimary
            )

            Text(
                text = when {
                    isTyping -> "Typing..."
                    else -> chat.lastMessage
                },
                color = if (isTyping) ElectricTeal else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatTime(chat.lastSeen),
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
