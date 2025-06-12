package com.judstop.app.data.retrofit

import com.judstop.app.data.model.ChatRequest
import com.judstop.app.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AzureAiApiService {
    @POST("openai/deployments/{deploymentName}/chat/completions")
    suspend fun getChatCompletion(
        @Path("deploymentName") deploymentName: String,
        @Header("Authorization") authToken: String,
        @Query("api-version") apiVersion: String = "2025-01-01-preview", // Dari URL
        @Body requestBody: ChatRequest,
    ): Response<ChatResponse> // Menggunakan Response<T> untuk error handling
}