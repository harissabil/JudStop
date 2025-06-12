package com.judstop.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("messages") val messages: List<ChatMessage>,
    @SerializedName("max_completion_tokens") val maxCompletionTokens: Int = 800,
    @SerializedName("temperature") val temperature: Double = 0.8,
    @SerializedName("top_p") val topP: Double = 1.0, // API spec bilang int, tapi contoh double, pakai double lebih aman
    @SerializedName("frequency_penalty") val frequencyPenalty: Int = 0,
    @SerializedName("presence_penalty") val presencePenalty: Double = 0.3,
    @SerializedName("model") val model: String // e.g. "gpt-4.1" (sesuaikan dengan deployment)
)