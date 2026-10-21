package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

enum class DinhLeKieuLinhTrangThaiBuoiHoc(val displayName: String) {
    @SerializedName("CHUA_BAT_DAU")
    CHUA_BAT_DAU("Chưa bắt đầu"),

    @SerializedName("DANG_DIEN_RA")
    DANG_DIEN_RA("Đang diễn ra"),

    @SerializedName("DA_KET_THUC")
    DA_KET_THUC("Đã kết thúc")
}

data class DinhLeKieuLinhBuoiHoc(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("lopHocId") val lopHocId: Int = 0,
    @SerializedName("tenLopHoc") val tenLopHoc: String = "",
    @SerializedName("ngayHoc") val ngayHoc: String = "",
    @SerializedName("gioBatDau") val gioBatDau: String = "07:30",
    @SerializedName("gioKetThuc") val gioKetThuc: String = "11:30",
    @SerializedName("phongHoc") val phongHoc: String = "A1-402",
    @SerializedName("noiDung") val noiDung: String = "",
    @SerializedName("qrToken") val qrToken: String = "",
    @SerializedName("viTriLat") val viTriLat: Double = 10.762622,
    @SerializedName("viTriLng") val viTriLng: Double = 106.682229,
    @SerializedName("banKinh") val banKinh: Double = 100.0,
    @SerializedName("trangThai") val trangThai: DinhLeKieuLinhTrangThaiBuoiHoc = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU
)

data class DinhLeKieuLinhBuoiHocRequest(
    @SerializedName("lopHocId") val lopHocId: Int,
    @SerializedName("ngayHoc") val ngayHoc: String,
    @SerializedName("gioBatDau") val gioBatDau: String,
    @SerializedName("gioKetThuc") val gioKetThuc: String,
    @SerializedName("phongHoc") val phongHoc: String,
    @SerializedName("noiDung") val noiDung: String = "",
    @SerializedName("viTriLat") val viTriLat: Double = 10.762622,
    @SerializedName("viTriLng") val viTriLng: Double = 106.682229,
    @SerializedName("banKinh") val banKinh: Double = 100.0,
    @SerializedName("trangThai") val trangThai: DinhLeKieuLinhTrangThaiBuoiHoc = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU
)

// Dữ liệu mã Dynamic QR trả về mỗi 30s
data class DinhLeKieuLinhDynamicQr(
    @SerializedName("buoiHocId") val buoiHocId: Int,
    @SerializedName("qrCodeValue") val qrCodeValue: String,
    @SerializedName("expiresInSeconds") val expiresInSeconds: Int = 30,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)
