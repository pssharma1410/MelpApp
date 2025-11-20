package com.example.melpapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.melpapp.ui.chatdetail.ChatDetailScreen
import com.example.melpapp.ui.chatlist.ChatListScreen
import com.example.melpapp.ui.chatlist.ChatListViewModel

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.net.URLDecoder

object Routes {
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CHAT_LIST,
        modifier = modifier
    ) {
        composable(Routes.CHAT_LIST) {
            val vm: ChatListViewModel = hiltViewModel()
            ChatListScreen(
                viewModel = vm,
                // onChatClick ab 3 parameters leta hai: id, name, aur avatarUrl
                onChatClick = { id, name, avatarUrl ->
                    // 1. URL Encoding: Name aur URL ko encode karna zaroori hai
                    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())

                    val encodedUrl = if (avatarUrl != null) {
                        URLEncoder.encode(avatarUrl, StandardCharsets.UTF_8.toString())
                    } else {
                        "null" // Agar URL null hai, toh "null" string pass karein
                    }

                    // 2. Naye arguments ke saath navigate karein
                    navController.navigate("${Routes.CHAT_DETAIL}/$id?name=$encodedName&url=$encodedUrl")
                }
            )
        }

        composable(
            // Route mein chatId, name, aur url arguments define kiye gaye hain
            route = "${Routes.CHAT_DETAIL}/{chatId}?name={contactName}&url={profilePicUrl}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType },
                navArgument("contactName") { type = NavType.StringType; nullable = true },
                navArgument("profilePicUrl") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0

            // 3. Arguments extract aur decode karein
            val contactNameEncoded = backStackEntry.arguments?.getString("contactName")
            val profilePicUrlEncoded = backStackEntry.arguments?.getString("profilePicUrl")

            // Name ko decode karein
            val contactName = contactNameEncoded?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: "Unknown"

            // URL ko decode karein aur check karein agar woh "null" string hai
            val profilePicUrl = profilePicUrlEncoded?.let { url ->
                if (url == "null") null else URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
            }

            // 4. ChatDetailScreen ko teeno values pass karein
            ChatDetailScreen(
                chatId = chatId,
                contactName = contactName,
                profilePicUrl = profilePicUrl
            )
        }
    }
}