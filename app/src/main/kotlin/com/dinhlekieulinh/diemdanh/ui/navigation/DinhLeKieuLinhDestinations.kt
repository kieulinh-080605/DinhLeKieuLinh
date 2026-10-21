package com.dinhlekieulinh.diemdanh.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Tuyến đường xác thực (Auth)
object DinhLeKieuLinhDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp/{email}"
    fun createOtpRoute(email: String) = "otp/$email"

    // Dashboard gốc theo vai trò
    const val ADMIN_ROOT = "admin_root"
    const val GIANG_VIEN_ROOT = "giangvien_root"
    const val SINH_VIEN_ROOT = "sinhvien_root"
}

// NavItem dạng data class để tránh null khi sealed class object chưa init
data class DinhLeKieuLinhNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

// Danh sách Bottom Nav cho Admin
object DinhLeKieuLinhAdminNav {
    val items = listOf(
        DinhLeKieuLinhNavItem("admin_users",   "Người dùng", Icons.Default.People),
        DinhLeKieuLinhNavItem("admin_monhoc",  "Môn học",    Icons.Default.MenuBook),
        DinhLeKieuLinhNavItem("admin_lophoc",  "Lớp học",    Icons.Default.Class),
        DinhLeKieuLinhNavItem("admin_buoihoc", "Buổi học",   Icons.Default.CalendarMonth),
        DinhLeKieuLinhNavItem("admin_diemdanh","Điểm danh",  Icons.Default.FactCheck)
    )
}

// Danh sách Bottom Nav cho Giảng viên
object DinhLeKieuLinhGiangVienNav {
    val items = listOf(
        DinhLeKieuLinhNavItem("gv_lophoc",  "Lớp dạy",  Icons.Default.Class),
        DinhLeKieuLinhNavItem("gv_buoihoc", "Lịch dạy", Icons.Default.CalendarToday),
        DinhLeKieuLinhNavItem("gv_diemdanh","Điểm danh",Icons.Default.FactCheck),
        DinhLeKieuLinhNavItem("gv_profile", "Cá nhân",  Icons.Default.AccountCircle)
    )
}

// Danh sách Bottom Nav cho Sinh viên
object DinhLeKieuLinhSinhVienNav {
    val items = listOf(
        DinhLeKieuLinhNavItem("sv_home",    "Lịch học",   Icons.Default.CalendarToday),
        DinhLeKieuLinhNavItem("sv_scan",    "Quét QR",    Icons.Default.QrCodeScanner),
        DinhLeKieuLinhNavItem("sv_history", "Chuyên cần", Icons.Default.History),
        DinhLeKieuLinhNavItem("sv_profile", "Cá nhân",    Icons.Default.AccountCircle)
    )
}

// Giữ lại sealed class cũ để tránh break các file khác tham chiếu
sealed class DinhLeKieuLinhAdminNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Users    : DinhLeKieuLinhAdminNavItem("admin_users",    "Người dùng", Icons.Default.People)
    object MonHoc   : DinhLeKieuLinhAdminNavItem("admin_monhoc",   "Môn học",    Icons.Default.MenuBook)
    object LopHoc   : DinhLeKieuLinhAdminNavItem("admin_lophoc",   "Lớp học",    Icons.Default.Class)
    object BuoiHoc  : DinhLeKieuLinhAdminNavItem("admin_buoihoc",  "Buổi học",   Icons.Default.CalendarMonth)
    object DiemDanh : DinhLeKieuLinhAdminNavItem("admin_diemdanh", "Điểm danh",  Icons.Default.FactCheck)
    companion object { val items = listOf<DinhLeKieuLinhAdminNavItem>(Users, MonHoc, LopHoc, BuoiHoc, DiemDanh) }
}

sealed class DinhLeKieuLinhGiangVienNavItem(val route: String, val title: String, val icon: ImageVector) {
    object LopHoc   : DinhLeKieuLinhGiangVienNavItem("gv_lophoc",   "Lớp dạy",  Icons.Default.Class)
    object BuoiHoc  : DinhLeKieuLinhGiangVienNavItem("gv_buoihoc",  "Lịch dạy", Icons.Default.CalendarToday)
    object DiemDanh : DinhLeKieuLinhGiangVienNavItem("gv_diemdanh", "Điểm danh",Icons.Default.FactCheck)
    object Profile  : DinhLeKieuLinhGiangVienNavItem("gv_profile",  "Cá nhân",  Icons.Default.AccountCircle)
    companion object { val items = listOf<DinhLeKieuLinhGiangVienNavItem>(LopHoc, BuoiHoc, DiemDanh, Profile) }
}

sealed class DinhLeKieuLinhSinhVienNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home    : DinhLeKieuLinhSinhVienNavItem("sv_home",    "Lịch học",   Icons.Default.CalendarToday)
    object ScanQr  : DinhLeKieuLinhSinhVienNavItem("sv_scan",    "Quét QR",    Icons.Default.QrCodeScanner)
    object History : DinhLeKieuLinhSinhVienNavItem("sv_history", "Chuyên cần", Icons.Default.History)
    object Profile : DinhLeKieuLinhSinhVienNavItem("sv_profile", "Cá nhân",    Icons.Default.AccountCircle)
    companion object { val items = listOf<DinhLeKieuLinhSinhVienNavItem>(Home, ScanQr, History, Profile) }
}
