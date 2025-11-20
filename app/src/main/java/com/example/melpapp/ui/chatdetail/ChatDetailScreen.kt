package com.example.melpapp.ui.chatdetail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.melpapp.domain.model.Message

// COLORS
val LightBackground = Color(0xFFFFFFFF)
val LightHeader = Color(0xFFF7F7F7)
val ElectricTeal = Color(0xFF00BFA5)
val TextPrimary = Color(0xFF1E1E1E)
val TextSecondary = Color(0xFF6A6A6A)
val AppBarColor = ElectricTeal
val SentBubblePrimary = ElectricTeal.copy(alpha = 0.8f)
val ReceivedBubbleBackground = LightHeader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: Int,
    contactName: String,
    profilePicUrl: String?,
    onBackClick: () -> Unit = {},
    viewModel: ChatDetailViewModel = hiltViewModel()
) {

    val messages by viewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            ChatHeader(
                contactName = contactName,
                profilePicUrl = profilePicUrl,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            MessageInputBar(viewModel)
        },
        containerColor = LightBackground
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(LightBackground)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                AnimatedMessageBubble(msg)
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    contactName: String,
    profilePicUrl: String?,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppBarColor),
                    contentAlignment = Alignment.Center
                ) {

                    if (!profilePicUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profilePicUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = contactName.first().toString(),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = contactName,
                        color = LightBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "online",
                        color = LightBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },

        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Call, "Call", tint = Color.White)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, "More", tint = Color.White)
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(containerColor = ElectricTeal)
    )
}



@Composable
fun MessageInputBar(viewModel: ChatDetailViewModel) {

    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBackground)
            .border(1.dp, LightHeader)
            .padding(8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Type a message...", color = TextSecondary) },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightHeader,
                unfocusedContainerColor = LightHeader,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = ElectricTeal
            ),
            singleLine = true
        )

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    viewModel.sendMessage(text)
                    text = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}



@Composable
fun AnimatedMessageBubble(msg: Message) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(250)) +
                slideInHorizontally(
                    initialOffsetX = { if (msg.isMe) 300 else -300 }
                )
    ) {
        if (msg.isMe) SentBubble(msg.text) else ReceivedBubble(msg.text)
    }
}


@Composable
fun SentBubble(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(SentBubblePrimary)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, color = Color.White)
        }
    }
}

@Composable
fun ReceivedBubble(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(ReceivedBubbleBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, color = TextPrimary)
        }
    }
}
