package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

enum class DinhLeKieuLinhTrangThaiDiemDanh(val displayName: String) {
    @SerializedName("CO_MAT")
    CO_MAT("Có mặt"),

    @SerializedName("MUON")
    MUON("Đi muộn"),

    @SerializedName("VANG")
    VANG("Vắng"),

    @SerializedName("CO_PHEP")
    CO_PHEP("Có phép")
}

enum class DinhLeKieuLinhPhuongThucDiemDanh(val displayName: String) {
    @SerializedName("QR")
    QR("Mã QR"),

    @SerializedName("GPS")
    GPS("Vị trí GPS"),

    @SerializedName("QR_GPS")
    QR_GPS("QR + GPS"),

    @SerializedName("THU_CONG")
    THU_CONG("Thủ công")
}

data class DinhLeKieuLinhDiemDanh(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("buoiHocId") val buoiHocId: Int = 0,
    @SerializedName("tenLopHoc") val tenLopHoc: String = "",
    @SerializedName("ngayHoc") val ngayHoc: String = "",
    @SerializedName("sinhVienId") val sinhVienId: Int = 0,
    @SerializedName("sinhVienMaSo") val sinhVienMaSo: String = "",
    @SerializedName("sinhVienHoTen") val sinhVienHoTen: String = "",
    @SerializedName("thoiGianDiemDanh") val thoiGianDiemDanh: String = "",
    @SerializedName("trangThai") val trangThai: DinhLeKieuLinhTrangThaiDiemDanh = DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT,
    @SerializedName("phuongThuc") val phuongThuc: DinhLeKieuLinhPhuongThucDiemDanh = DinhLeKieuLinhPhuongThucDiemDanh.QR_GPS,
    @SerializedName("viTriLat") val viTriLat: Double? = null,
    @SerializedName("viTriLng") val viTriLng: Double? = null,
    @SerializedName("khoangCach") val khoangCach: Double? = null,
    @SerializedName("ghiChu") val ghiChu: String = ""
)

// Request quét QR để điểm danh từ sinh viên
data class DinhLeKieuLinhScanQrRequest(
    @SerializedName("buoiHocId") val buoiHocId: Int,
    @SerializedName("qrToken") val qrToken: String,
    @SerializedName("viTriLat") val viTriLat: Double,
    @SerializedName("viTriLng") val viTriLng: Double
)

// Request điểm danh bù thủ công từ Giảng viên / Admin
data class DinhLeKieuLinhManualDiemDanhRequest(
    @SerializedName("buoiHocId") val buoiHocId: Int,
    @SerializedName("sinhVienId") val sinhVienId: Int,
    @SerializedName("trangThai") val trangThai: DinhLeKieuLinhTrangThaiDiemDanh,
    @SerializedName("ghiChu") val ghiChu: String = ""
)

// Thống kê chuyên cần của sinh viên
data class DinhLeKieuLinhThongKeDiemDanh(
    @SerializedName("sinhVienId") val sinhVienId: Int = 0,
    @SerializedName("sinhVienHoTen") val sinhVienHoTen: String = "",
    @SerializedName("tongSoBuoi") val tongSoBuoi: Int = 0,
    @SerializedName("soBuoiCoMat") val soBuoiCoMat: Int = 0,
    @SerializedName("soBuoiMuon") val soBuoiMuon: Int = 0,
    @SerializedName("soBuoiVang") val soBuoiVang: Int = 0,
    @SerializedName("soBuoiCoPhep") val soBuoiCoPhep: Int = 0,
    @SerializedName("tiLeChuyenCan") val tiLeChuyenCan: Float = 0f,
    // Tính năng tự đề xuất: Cảnh báo đỏ nếu vắng quá hoặc bằng 3 buổi
    @SerializedName("canhBaoVang") val canhBaoVang: Boolean = false
)
