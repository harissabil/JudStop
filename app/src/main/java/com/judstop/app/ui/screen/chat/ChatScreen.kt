package com.judstop.app.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.judstop.app.data.model.ChatMessage
import com.judstop.app.ui.screen.chat.component.MarkdownPreview
import com.judstop.app.ui.theme.DarkerBlueText
import com.judstop.app.ui.theme.JudStopTheme
import com.judstop.app.ui.theme.LightBlueBackground
import com.judstop.app.ui.theme.LightBluePrimary
import kotlinx.coroutines.launch

val UserBubbleColor = LightBluePrimary
val AiBubbleColor = Color.White // Atau Color(0xFFE0E0E0) untuk abu-abu muda
val AiTextColor = DarkerBlueText
val UserTextColor = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages = chatViewModel.chatMessages
    val currentInput = chatViewModel.currentMessageInput.value
    val isLoading = chatViewModel.isLoading.value
    val errorMessage = chatViewModel.errorMessage.value

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Reset chat saat screen di dispose (keluar dari screen)
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.resetChat()
        }
    }

    // Scroll ke item terakhir saat ada pesan baru
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            chatViewModel.errorMessage.value = null // Clear error setelah ditampilkan
        }
    }

    Scaffold(
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        // horizontalAlignment = Alignment.Start bisa ditambahkan jika perlu,
                        // tapi biasanya default sudah baik untuk title
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            text = "JudStop AI",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 14.sp,
                            fontSize = 18.sp // Sesuaikan ukuran agar pas dengan subtitle
                        )
                        Text(
                            text = "Powered by Azure OpenAI",
                            fontSize = 11.sp, // Ukuran lebih kecil untuk subtitle
                            color = Color.White.copy(alpha = 0.8f), // Warna lebih soft
                            lineHeight = 14.sp, // Jarak antar baris
                            // fontWeight = FontWeight.Light bisa ditambahkan jika ingin lebih tipis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBluePrimary // Samakan dengan hero section MainScreen
                )
            )
        },
        bottomBar = {
            ChatInputRow(
                modifier = Modifier.imePadding(),
                text = currentInput,
                onTextChanged = { chatViewModel.currentMessageInput.value = it },
                onSendClicked = {
                    chatViewModel.sendMessage()
                    keyboardController?.hide() // Sembunyikan keyboard setelah send
                },
                isLoading = isLoading
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding dari Scaffold (TopAppBar & BottomBar)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Jarak antar bubble
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageBubble(message = message)
            }
            // Indikator loading jika AI sedang "mengetik"
            if (isLoading && messages.lastOrNull()?.isUserMessage == true) {
                item {
                    AiTypingIndicatorItem()
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.isUserMessage
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isUser) UserBubbleColor else AiBubbleColor
    val textColor = if (isUser) UserTextColor else AiTextColor
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 56.dp else 0.dp, // Indentasi agar tidak full width
                end = if (isUser) 0.dp else 56.dp
            ),
        contentAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = "AI Avatar",
                    tint = LightBluePrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Surface( // Menggunakan Surface untuk shadow dan bentuk
                shape = bubbleShape,
                color = bubbleColor,
                tonalElevation = 2.dp, // Sedikit shadow
                modifier = Modifier.wrapContentSize()
            ) {
                MarkdownPreview(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    lineHeight = 22.sp
                )
            }
            // Untuk user, avatar bisa dihilangkan atau diganti ikon user jika ada
        }
    }
}

@Composable
fun AiTypingIndicatorItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.SupportAgent,
            contentDescription = "AI Avatar",
            tint = LightBluePrimary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "JudStop AI sedang mengetik...",
            fontSize = 14.sp,
            color = DarkerBlueText.copy(alpha = 0.7f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}


@Composable
fun ChatInputRow(
    modifier: Modifier = Modifier,
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp, // Shadow untuk memisahkan dari konten chat
        color = MaterialTheme.colorScheme.surface // Atau LightBlueBackground jika ingin sama
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesan...", color = DarkerBlueText.copy(alpha = 0.6f)) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightBluePrimary,
                    unfocusedBorderColor = DarkerBlueText.copy(alpha = 0.4f),
                    cursorColor = LightBluePrimary,
                    focusedTextColor = DarkerBlueText,
                    unfocusedTextColor = DarkerBlueText,
                    disabledBorderColor = Color.Transparent // Jika dinonaktifkan
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (!isLoading && text.isNotBlank()) onSendClicked() }
                ),
                maxLines = 5, // Izinkan beberapa baris
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onSendClicked,
                enabled = !isLoading && text.isNotBlank(),
                modifier = Modifier.size(48.dp), // Ukuran tombol yang baik
                colors = IconButtonDefaults.filledIconButtonColors( // Menggunakan filled untuk kontras
                    containerColor = LightBluePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = LightBluePrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim")
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8EAF6)
@Composable
fun ChatScreenPreview() {
    JudStopTheme {
        ChatScreen(onNavigateBack = {})
    }
}

@Preview
@Composable
fun ChatMessageBubbleUserPreview() {
    JudStopTheme {
        Column(Modifier.background(LightBlueBackground).padding(10.dp)) {
            ChatMessageBubble(message = ChatMessage(role = "user", content = "Ini pesan dari pengguna."))
        }
    }
}

@Preview
@Composable
fun ChatMessageBubbleAiPreview() {
    JudStopTheme {
        Column(Modifier.background(LightBlueBackground).padding(10.dp)) {
            ChatMessageBubble(message = ChatMessage(role = "assistant", content = "Ini respon dari JudStop AI."))
        }
    }
}