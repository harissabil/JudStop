package com.judstop.app.ui.screen.chat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.judstop.app.data.ApiResult
import com.judstop.app.data.ChatRepository
import com.judstop.app.data.model.ChatMessage
import com.judstop.app.data.model.ChatRequest
import com.judstop.app.data.retrofit.RetrofitClient
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {

    private val _chatMessages = mutableStateListOf<ChatMessage>()
    val chatMessages: List<ChatMessage> = _chatMessages

    private var conversationHistoryForApi = mutableListOf<ChatMessage>()

    val currentMessageInput = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val systemPrompt = ChatMessage(
        role = "system",
        content = """
        Kamu adalah JudStop AI, chatbot motivasional yang dirancang khusus untuk membantu pengguna menghindari kebiasaan judi online. Kamu adalah bagian dari aplikasi JudStop, yang memanfaatkan Android Accessibility Service untuk memblokir akses ke situs judi online secara proaktif.

        Tugasmu:
        - Memberikan dukungan emosional dan motivasi kepada pengguna yang ingin berhenti berjudi.
        - Menyediakan tips, edukasi, dan teknik untuk mengendalikan keinginan berjudi.
        - Mendorong kebiasaan positif seperti olahraga, membaca, atau aktivitas produktif lainnya.
        - Bersikap empatik, suportif, tidak menghakimi, dan tidak menggurui.

        Aturan penting:
        - Jangan membahas topik di luar konteks menghindari judi online, meskipun dengan tujuan untuk menghibur.
        - Jangan menyebut, mempromosikan, atau memberikan informasi tentang situs atau strategi judi.
        - Jika pengguna keluar konteks, arahkan kembali dengan lembut ke topik utama.
        - Jaga gaya bahasa tetap ramah, penuh semangat, dan membangun.

        Contoh:
        Pengguna: Aku lagi pengen banget main judi, susah banget nahan.
        JudStop AI: Terima kasih sudah jujur. Aku tahu ini nggak mudah, tapi kamu udah melangkah sejauh ini. Yuk cari aktivitas lain bareng, mungkin bisa coba jalan-jalan sebentar atau ngobrol bareng aku tentang hal-hal positif yang kamu suka.

        Fokus utama kamu adalah membantu pengguna menghindari judi online dan mendukung proses pemulihan diri mereka.
        """.trimIndent()
    )

    init {
        resetChat()
    }

    fun sendMessage() {
        val userText = currentMessageInput.value.trim()
        if (userText.isBlank() || isLoading.value) return

        val userChatMessage = ChatMessage(role = "user", content = userText)
        _chatMessages.add(userChatMessage)
        // Untuk API, kita hanya butuh role dan content, ID dan isUserMessage tidak relevan
        conversationHistoryForApi.add(ChatMessage(role = "user", content = userText))
        currentMessageInput.value = "" // Clear input
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            val request = ChatRequest(
                messages = ArrayList(conversationHistoryForApi), // Kirim salinan
                model = RetrofitClient.AZURE_DEPLOYMENT_NAME // Model sesuai deployment
            )
            Log.d("ChatViewModel", "Sending request: $request")

            when (val result = repository.getChatCompletion(request)) {
                is ApiResult.Success -> {
                    val aiResponseMessage = result.data.choices?.firstOrNull()?.message
                    if (aiResponseMessage != null && aiResponseMessage.content.isNotBlank()) {
                        _chatMessages.add(aiResponseMessage) // Ini sudah punya role "assistant" & content
                        conversationHistoryForApi.add(ChatMessage(role = aiResponseMessage.role, content = aiResponseMessage.content))
                    } else {
                        val errorMsg = "Respon AI kosong atau tidak valid."
                        Log.e("ChatViewModel", errorMsg)
                        _chatMessages.add(ChatMessage(role = "assistant", content = "Maaf, saya tidak bisa memberikan respon saat ini."))
                        errorMessage.value = errorMsg
                    }
                }
                is ApiResult.Error -> {
                    val errorMsg = "Gagal menghubungi AI: ${result.message} (Code: ${result.code})"
                    Log.e("ChatViewModel", errorMsg)
                    _chatMessages.add(ChatMessage(role = "assistant", content = "Oops, terjadi kesalahan. Coba lagi nanti, ya."))
                    errorMessage.value = errorMsg
                }
                is ApiResult.Exception -> {
                    val errorMsg = "Terjadi kesalahan jaringan: ${result.throwable.localizedMessage}"
                    Log.e("ChatViewModel", errorMsg, result.throwable)
                    _chatMessages.add(ChatMessage(role = "assistant", content = "Koneksi internet bermasalah. Periksa koneksimu."))
                    errorMessage.value = errorMsg
                }
            }
            isLoading.value = false
        }
    }

    fun resetChat() {
        Log.d("ChatViewModel", "Resetting chat.")
        _chatMessages.clear()
        conversationHistoryForApi.clear()
        conversationHistoryForApi.add(systemPrompt) // Selalu mulai API history dengan system prompt
        isLoading.value = false
        errorMessage.value = null
        currentMessageInput.value = ""
        // Tambahkan pesan sambutan awal ke UI
        _chatMessages.add(
            ChatMessage(
                role = "assistant",
                content = "Halo! Aku JudStop AI. Ada yang bisa aku bantu untuk mendukungmu hari ini?"
            )
        )
    }
}