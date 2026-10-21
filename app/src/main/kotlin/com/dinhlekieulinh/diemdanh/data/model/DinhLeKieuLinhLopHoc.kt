package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

data class DinhLeKieuLinhLopHoc(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("maLopHoc") val maLopHoc: String = "",
    @SerializedName("tenLopHoc") val tenLopHoc: String = "",
    @SerializedName("monHocId") val monHocId: Int = 0,
    @SerializedName("tenMonHoc") val tenMonHoc: String = "",
    @SerializedName("giangVienId") val giangVienId: Int = 0,
    @SerializedName("giangVienHoTen") val giangVienHoTen: String = "",
    @SerializedName("hocKy") val hocKy: String = "HK1",
    @SerializedName("namHoc") val namHoc: String = "2025-2026",
    @SerializedName("soLuongSinhVien") val soLuongSinhVien: Int = 0
)

data class DinhLeKieuLinhLopHocRequest(
    @SerializedName("maLopHoc") val maLopHoc: String,
    @SerializedName("tenLopHoc") val tenLopHoc: String,
    @SerializedName("monHocId") val monHocId: Int,
    @SerializedName("giangVienId") val giangVienId: Int,
    @SerializedName("hocKy") val hocKy: String,
    @SerializedName("namHoc") val namHoc: String
)
