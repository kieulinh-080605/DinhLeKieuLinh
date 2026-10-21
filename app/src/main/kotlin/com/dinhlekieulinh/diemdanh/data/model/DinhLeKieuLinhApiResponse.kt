package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

// Lớp phản hồi chung từ API
data class DinhLeKieuLinhApiResponse<T>(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null
)

// Sealed class phục vụ quản lý trạng thái UI (State Flow) trong ViewModel
sealed class DinhLeKieuLinhResource<out T> {
    object Idle : DinhLeKieuLinhResource<Nothing>()
    object Loading : DinhLeKieuLinhResource<Nothing>()
    data class Success<out T>(val data: T) : DinhLeKieuLinhResource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : DinhLeKieuLinhResource<Nothing>()
}
