package com.judstop.app.data

import com.google.gson.Gson
import com.judstop.app.data.model.ChatRequest
import com.judstop.app.data.model.ChatResponse
import com.judstop.app.data.model.ErrorResponse
import com.judstop.app.data.retrofit.AzureAiApiService
import com.judstop.app.data.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ChatRepository(private val apiService: AzureAiApiService = RetrofitClient.instance) {
    suspend fun getChatCompletion(
        request: ChatRequest,
    ): ApiResult<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getChatCompletion(
                    deploymentName = RetrofitClient.AZURE_DEPLOYMENT_NAME,
                    authToken = RetrofitClient.AZURE_API_KEY_BEARER,
                    requestBody = request
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("204", "Response body is null but successful (No Content)")
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody.isNullOrEmpty()) {
                        ApiResult.Error(
                            response.code().toString(),
                            response.message() ?: "Unknown HTTP error"
                        )
                    } else {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBody, ErrorResponse::class.java)
                            ApiResult.Error(
                                errorResponse?.error?.code ?: response.code().toString(),
                                errorResponse?.error?.message ?: "Failed to parse error body"
                            )
                        } catch (_: Exception) {
                            ApiResult.Error(
                                response.code().toString(),
                                "Error parsing error JSON: $errorBody"
                            )
                        }
                    }
                }
            } catch (e: HttpException) {
                ApiResult.Error(e.code().toString(), e.message())
            } catch (e: IOException) {
                ApiResult.Exception(e) // Network error
            } catch (e: Exception) {
                ApiResult.Exception(e) // Other unexpected errors
            }
        }
    }
}