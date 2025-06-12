package com.judstop.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
    // ID unik untuk LazyColumn key dan Transient agar tidak dikirim ke API via Gson
    @Transient val id: String = java.util.UUID.randomUUID().toString(),
    @Transient val isUserMessage: Boolean = role == "user"
)