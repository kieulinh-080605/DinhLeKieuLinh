package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

enum class DinhLeKieuLinhRole(val displayName: String) {
    @SerializedName("ADMIN")
    ADMIN("Quản trị viên"),

    @SerializedName("GIANG_VIEN")
    GIANG_VIEN("Giảng viên"),

    @SerializedName("SINH_VIEN")
    SINH_VIEN("Sinh viên")
}

data class DinhLeKieuLinhUser(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("hoTen") val hoTen: String = "",
    @SerializedName("maSo") val maSo: String = "",
    @SerializedName("soDienThoai") val soDienThoai: String = "",
    @SerializedName("role") val role: DinhLeKieuLinhRole = DinhLeKieuLinhRole.SINH_VIEN,
    @SerializedName("isActive") val isActive: Boolean = true
)

data class DinhLeKieuLinhUserRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("hoTen") val hoTen: String,
    @SerializedName("maSo") val maSo: String,
    @SerializedName("soDienThoai") val soDienThoai: String,
    @SerializedName("role") val role: DinhLeKieuLinhRole,
    @SerializedName("isActive") val isActive: Boolean = true
)
