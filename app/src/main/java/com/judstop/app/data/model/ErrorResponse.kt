package com.judstop.app.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error") val error: ApiError?
)

data class ApiError(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)