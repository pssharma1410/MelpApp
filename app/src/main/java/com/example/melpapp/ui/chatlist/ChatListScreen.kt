package com.example.melpapp.ui.chatlist

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.util.Date
import java.util.Locale

// --- Futuristic/Light Color Palette ---
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
    // onChatClick ab 3 parameters leta hai: id, name, aur avatarUrl
    onChatClick: (Int, String, String?) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()

    val refreshState = rememberSwipeRefreshState(
        isRefreshing = state.isRefreshing
    )

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
                                text = {
                                    Text(
                                        "Logout",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout Icon", tint = ElectricTeal)
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    showLogoutDialog = true
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = TextPrimary,
                                    leadingIconColor = ElectricTeal,
                                )
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
                        text = "Connection Error: $msg",
                        color = ErrorText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            SwipeRefresh(
                state = refreshState,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                ) {
                    itemsIndexed(
                        items = state.displayedChats,
                        key = { _, item -> item.id }
                    ) { index, chat ->
                        ChatRow(
                            chat = chat,
                            isTyping = typingUsers.contains(chat.id),
                            onClick = {
                                viewModel.markChatAsRead(chat.id)
                                // Yahan teeno values pass karein
                                onChatClick(chat.id, chat.name, chat.avatarUrl)
                            }
                        )

                        Box(modifier = Modifier.padding(start = 72.dp)) {
                            Divider(
                                color = TextSecondary.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }

                        if (index == state.displayedChats.lastIndex) {
                            LaunchedEffect(index, state.displayedChats.size) {
                                viewModel.loadMore()
                            }
                        }
                    }
                }
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
        text = { Text("Are you sure you want to log out of MELD? Your session will be terminated and the application will close.") },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    (context as? Activity)?.finish()
                }
            ) {
                Text(
                    "Logout & Exit",
                    color = ErrorText,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = ElectricTeal,
                    fontWeight = FontWeight.Bold
                )
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search chats...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = ElectricTeal) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricTeal,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = LightHeader,
            unfocusedContainerColor = LightHeader,
            cursorColor = ElectricTeal
        ),
        shape = RoundedCornerShape(12.dp)
    )

    if (apiResponseTimeMs != null) {
        Text(
            text = "Loaded in: ${apiResponseTimeMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
    }
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "Just now"

        diff < hour -> {
            val mins = diff / minute
            "$mins min ago"
        }

        diff < day -> {
            val hours = diff / hour
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            formatter.format(Date(timestamp))
        }

        diff < 2 * day -> "Yesterday"

        diff < 7 * day -> {
            val formatter = SimpleDateFormat("EEE", Locale.getDefault())
            formatter.format(Date(timestamp))
        }

        else -> {
            val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
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
            .background(if (chat.unreadCount > 0) ElectricTeal.copy(alpha = 0.05f) else Color.Transparent)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                    width = if (chat.isOnline) 2.dp else 0.dp,
                    color = ElectricTeal,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextPrimary
            )

            Text(
                text = when {
                    isTyping -> "Typing..."
                    else -> chat.lastMessage
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = when {
                    isTyping -> ElectricTeal
                    chat.unreadCount > 0 -> TextPrimary
                    else -> TextSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            val timeText = remember(chat.lastSeen) {
                formatTime(chat.lastSeen)
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = if (chat.unreadCount > 0) ElectricTeal else TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (chat.unreadCount > 0 && !isTyping) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                        .clip(CircleShape)
                        .background(ElectricTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = LightBackground,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}