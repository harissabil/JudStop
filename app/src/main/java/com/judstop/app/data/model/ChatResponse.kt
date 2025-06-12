package com.judstop.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("choices") val choices: List<Choice>?,
    @SerializedName("created") val created: Long?,
    @SerializedName("id") val id: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("object") val objectType: String?, // "object" adalah keyword di Kotlin
    @SerializedName("prompt_filter_results") val promptFilterResults: List<PromptFilterResult>?,
    @SerializedName("system_fingerprint") val systemFingerprint: String?,
    @SerializedName("usage") val usage: Usage?
)

data class Choice(
    @SerializedName("content_filter_results") val contentFilterResults: ContentFilterResults?,
    @SerializedName("finish_reason") val finishReason: String?,
    @SerializedName("index") val index: Int?,
    // logprobs bisa kompleks, untuk kesederhanaan, Any? atau spesifik jika tahu strukturnya
    @SerializedName("logprobs") val logprobs: Any? = null,
    @SerializedName("message") val message: ChatMessage?
)

data class ContentFilterResults(
    @SerializedName("hate") val hate: FilterDetail?,
    @SerializedName("protected_material_code") val protectedMaterialCode: ProtectedMaterialCodeDetail?,
    @SerializedName("protected_material_text") val protectedMaterialText: ProtectedMaterialTextDetail?,
    @SerializedName("self_harm") val selfHarm: FilterDetail?,
    @SerializedName("sexual") val sexual: FilterDetail?,
    @SerializedName("violence") val violence: FilterDetail?
)

data class FilterDetail(
    @SerializedName("filtered") val filtered: Boolean?,
    @SerializedName("severity") val severity: String?
)

data class ProtectedMaterialCodeDetail( // Berbeda dari FilterDetail
    @SerializedName("filtered") val filtered: Boolean?,
    @SerializedName("detected") val detected: Boolean?
)
data class ProtectedMaterialTextDetail( // Berbeda dari FilterDetail
    @SerializedName("filtered") val filtered: Boolean?,
    @SerializedName("detected") val detected: Boolean?
)


data class PromptFilterResult(
    @SerializedName("prompt_index") val promptIndex: Int?,
    @SerializedName("content_filter_results") val contentFilterResults: ContentFilterResultsForPrompt? // Beda struktur sedikit
)

// Struktur ContentFilterResults untuk prompt_filter_results
data class ContentFilterResultsForPrompt(
    @SerializedName("hate") val hate: FilterDetail?,
    @SerializedName("jailbreak") val jailbreak: JailbreakDetail?, // Ada tambahan jailbreak
    @SerializedName("self_harm") val selfHarm: FilterDetail?,
    @SerializedName("sexual") val sexual: FilterDetail?,
    @SerializedName("violence") val violence: FilterDetail?
)

data class JailbreakDetail(
    @SerializedName("filtered") val filtered: Boolean?,
    @SerializedName("detected") val detected: Boolean?
)


data class Usage(
    @SerializedName("completion_tokens") val completionTokens: Int?,
    // Ada sub-detail di contoh, untuk simpelnya bisa diabaikan atau dibuatkan model
    // @SerializedName("completion_tokens_details") val completionTokensDetails: Any?,
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    // @SerializedName("prompt_tokens_details") val promptTokensDetails: Any?,
    @SerializedName("total_tokens") val totalTokens: Int?
)