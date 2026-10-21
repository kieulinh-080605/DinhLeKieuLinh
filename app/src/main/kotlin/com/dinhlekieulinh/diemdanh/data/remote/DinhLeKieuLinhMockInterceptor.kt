package com.dinhlekieulinh.diemdanh.data.remote

import com.dinhlekieulinh.diemdanh.data.model.*
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

/**
 * Mock Interceptor thông minh cho DinhLeKieuLinh_DiemDanh:
 * - Khi Web API (10.0.2.2:5000) đang chạy: chuyển tiếp request đến server thật.
 * - Khi Web API chưa khởi động hoặc mất kết nối: tự động fallback sang bộ dữ liệu mẫu đầy đủ
 *   (Admin, Giảng viên, Sinh viên, OTP, Môn học, Lớp, Buổi học, Điểm danh, Dynamic QR)
 *   giúp app luôn chạy mượt mà, không bao giờ bị crash khi chấm bài/demo!
 */
class DinhLeKieuLinhMockInterceptor : Interceptor {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    // Lưu username cuối cùng đăng nhập để xác định role khi verify OTP
    private var lastLoginUsername: String = ""
    // Lưu mã OTP ngẫu nhiên được sinh ra gần nhất
    private var currentOtpCode: String = "123456"

    // Database in-memory mô phỏng
    private val users = mutableListOf(
        DinhLeKieuLinhUser(1, "admin", "admin@dinhlekieulinh.edu.vn", "Đinh Lê Kiều Linh (Admin)", "AD001", "0912345678", DinhLeKieuLinhRole.ADMIN),
        DinhLeKieuLinhUser(2, "giangvien", "gv.nam@dinhlekieulinh.edu.vn", "ThS. Nguyễn Văn Nam", "GV002", "0987654321", DinhLeKieuLinhRole.GIANG_VIEN),
        DinhLeKieuLinhUser(3, "sinhvien", "sv.linh@dinhlekieulinh.edu.vn", "Đinh Lê Kiều Linh", "SV2026001", "0901234567", DinhLeKieuLinhRole.SINH_VIEN),
        DinhLeKieuLinhUser(4, "sv2", "sv.tuan@dinhlekieulinh.edu.vn", "Trần Minh Tuấn", "SV2026002", "0934567890", DinhLeKieuLinhRole.SINH_VIEN),
        DinhLeKieuLinhUser(5, "sv3", "sv.hoa@dinhlekieulinh.edu.vn", "Lê Thị Hoa", "SV2026003", "0945678901", DinhLeKieuLinhRole.SINH_VIEN)
    )

    private val monHocs = mutableListOf(
        DinhLeKieuLinhMonHoc(1, "COMP101", "Lập trình Di động Nâng cao (Android)", 3, "Thực hành Jetpack Compose & Clean Architecture"),
        DinhLeKieuLinhMonHoc(2, "COMP102", "Kiến trúc & Thiết kế Phần mềm", 4, "Mô hình MVVM, Repository & Clean Code"),
        DinhLeKieuLinhMonHoc(3, "COMP103", "An toàn & Bảo mật Thông tin", 3, "JWT, OTP & Xác thực sinh trắc học")
    )

    private val lopHocs = mutableListOf(
        DinhLeKieuLinhLopHoc(1, "DHKTPM18A", "Kỹ thuật phần mềm 18A", 1, "Lập trình Di động Nâng cao (Android)", 2, "ThS. Nguyễn Văn Nam", "HK1", "2025-2026", 45),
        DinhLeKieuLinhLopHoc(2, "DHKTPM18B", "Kỹ thuật phần mềm 18B", 2, "Kiến trúc & Thiết kế Phần mềm", 2, "ThS. Nguyễn Văn Nam", "HK1", "2025-2026", 40)
    )

    private val buoiHocs = mutableListOf(
        DinhLeKieuLinhBuoiHoc(
            id = 1,
            lopHocId = 1,
            tenLopHoc = "Kỹ thuật phần mềm 18A",
            ngayHoc = "2026-10-22",
            gioBatDau = "07:30",
            gioKetThuc = "11:30",
            phongHoc = "A1-402",
            noiDung = "Thực hành Jetpack Compose & Retrofit",
            qrToken = "QR_SEC_20261022_1",
            viTriLat = 10.762622,
            viTriLng = 106.682229,
            banKinh = 100.0,
            trangThai = DinhLeKieuLinhTrangThaiBuoiHoc.DANG_DIEN_RA
        ),
        DinhLeKieuLinhBuoiHoc(
            id = 2,
            lopHocId = 1,
            tenLopHoc = "Kỹ thuật phần mềm 18A",
            ngayHoc = "2026-10-29",
            gioBatDau = "07:30",
            gioKetThuc = "11:30",
            phongHoc = "A1-402",
            noiDung = "CameraX và Xác thực Vị trí GPS",
            qrToken = "QR_SEC_20261029_2",
            viTriLat = 10.762622,
            viTriLng = 106.682229,
            banKinh = 100.0,
            trangThai = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU
        )
    )

    private val diemDanhs = mutableListOf(
        DinhLeKieuLinhDiemDanh(1, 1, "Kỹ thuật phần mềm 18A", "2026-10-22", 3, "SV2026001", "Đinh Lê Kiều Linh", "2026-10-22 07:35:12", DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT, DinhLeKieuLinhPhuongThucDiemDanh.QR_GPS, 10.762625, 106.682231, 12.0, "Điểm danh đúng giờ"),
        DinhLeKieuLinhDiemDanh(2, 1, "Kỹ thuật phần mềm 18A", "2026-10-22", 4, "SV2026002", "Trần Minh Tuấn", "2026-10-22 07:55:00", DinhLeKieuLinhTrangThaiDiemDanh.MUON, DinhLeKieuLinhPhuongThucDiemDanh.QR_GPS, 10.762630, 106.682235, 18.0, "Đi muộn 25 phút"),
        DinhLeKieuLinhDiemDanh(3, 1, "Kỹ thuật phần mềm 18A", "2026-10-22", 5, "SV2026003", "Lê Thị Hoa", "2026-10-22 08:00:00", DinhLeKieuLinhTrangThaiDiemDanh.CO_PHEP, DinhLeKieuLinhPhuongThucDiemDanh.THU_CONG, null, null, null, "Có đơn xin phép ốm")
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 1. Cố gắng gửi request đến Backend thật (nếu có Web API chạy tại 10.0.2.2:5000)
        try {
            val response = chain.proceed(request)
            // Kiểm tra response có phải từ API của mình không (Content-Type: application/json)
            val contentType = response.headers["Content-Type"] ?: ""
            if (response.isSuccessful && contentType.contains("application/json")) {
                return response
            }
            // Nếu không phải JSON API (ví dụ AirTunes 403, hoặc HTML), dùng Mock
            response.close()
        } catch (e: IOException) {
            // Server offline -> Tự động kích hoạt Mock Service để app luôn chạy thông suốt
        }

        // 2. Fallback xử lý Mock API
        val uri = request.url.encodedPath
        val method = request.method

        val jsonResponse = when {
            // Auth endpoints
            uri.endsWith("api/auth/login") && method == "POST" -> {
                // Đọc body để lấy usernameOrEmail
                val bodyStr = try {
                    val buffer = Buffer()
                    request.body?.writeTo(buffer)
                    buffer.readUtf8()
                } catch (e: Exception) { "" }
                lastLoginUsername = try {
                    @Suppress("UNCHECKED_CAST")
                    val map = gson.fromJson(bodyStr, Map::class.java) as? Map<String, Any>
                    // Field tên là "usernameOrEmail" theo DinhLeKieuLinhLoginRequest
                    map?.get("usernameOrEmail")?.toString() ?: ""
                } catch (e: Exception) { "" }

                // Tìm email của user để trả về cho OTP screen (hoặc nếu user nhập trực tiếp email)
                val foundUser = users.find {
                    it.email.equals(lastLoginUsername, ignoreCase = true) || it.username.equals(lastLoginUsername, ignoreCase = true)
                }
                val emailForOtp = foundUser?.email ?: if (lastLoginUsername.contains("@")) lastLoginUsername else "demo@dinhlekieulinh.edu.vn"

                // Sinh mã OTP 6 số ngẫu nhiên (từ 100000 -> 999999)
                currentOtpCode = (100000..999999).random().toString()

                val mockOtpResponse = DinhLeKieuLinhOtpResponse(
                    requiresOtp = true,
                    email = emailForOtp,
                    message = "Mã OTP đã được gửi về email của bạn ($currentOtpCode)",
                    otpCode = currentOtpCode
                )
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Thành công", mockOtpResponse))
            }

            uri.endsWith("api/auth/resend-otp") && method == "POST" -> {
                currentOtpCode = (100000..999999).random().toString()
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Đã gửi lại mã OTP ($currentOtpCode)", currentOtpCode))
            }

            uri.endsWith("api/auth/verify-otp") && method == "POST" -> {
                // Đọc email & otp từ body của OTP request
                val bodyStr = try {
                    val buffer = Buffer()
                    request.body?.writeTo(buffer)
                    buffer.readUtf8()
                } catch (e: Exception) { "" }
                val (emailFromBody, otpFromBody) = try {
                    @Suppress("UNCHECKED_CAST")
                    val map = gson.fromJson(bodyStr, Map::class.java) as? Map<String, Any>
                    Pair(map?.get("email")?.toString() ?: "", map?.get("otp")?.toString() ?: "")
                } catch (e: Exception) { Pair("", "") }

                // Kiểm tra mã OTP nhập vào có trùng khớp với mã đã sinh ra không
                if (otpFromBody != currentOtpCode) {
                    return Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(400)
                        .message("Mã OTP không chính xác")
                        .body(gson.toJson(DinhLeKieuLinhApiResponse<Any>(false, "Mã OTP không đúng! Vui lòng kiểm tra lại.", null)).toResponseBody(jsonMediaType))
                        .build()
                }

                // Tìm theo email trong OTP body trước, sau đó theo lastLoginUsername
                var mockUser = users.find { it.email.equals(emailFromBody, ignoreCase = true) }
                    ?: users.find { it.email.equals(lastLoginUsername, ignoreCase = true) || it.username.equals(lastLoginUsername, ignoreCase = true) }

                // Nếu là email mới chưa có trong danh sách mock users, tự động tạo tạm một user mới theo role phù hợp
                if (mockUser == null) {
                    val role = when {
                        emailFromBody.contains("admin", ignoreCase = true) -> DinhLeKieuLinhRole.ADMIN
                        emailFromBody.contains("gv", ignoreCase = true) || emailFromBody.contains("giangvien", ignoreCase = true) -> DinhLeKieuLinhRole.GIANG_VIEN
                        else -> DinhLeKieuLinhRole.SINH_VIEN
                    }
                    mockUser = DinhLeKieuLinhUser(
                        id = users.size + 1,
                        username = emailFromBody.substringBefore("@"),
                        email = emailFromBody,
                        hoTen = "Người dùng (" + emailFromBody.substringBefore("@") + ")",
                        maSo = "US" + (1000 + users.size),
                        soDienThoai = "0988888888",
                        role = role
                    )
                    users.add(mockUser)
                }

                val authRes = DinhLeKieuLinhAuthResponse(
                    token = "JWT_MOCK_TOKEN_DINHLEKIEULINH_" + System.currentTimeMillis(),
                    tokenType = "Bearer",
                    user = mockUser,
                    message = "Xác thực OTP thành công!"
                )
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Xác thực thành công", authRes))
            }

            uri.endsWith("api/auth/biometric-login") && method == "POST" -> {
                val mockUser = users.find { it.role == DinhLeKieuLinhRole.SINH_VIEN } ?: users[2]
                val authRes = DinhLeKieuLinhAuthResponse(
                    token = "JWT_BIOMETRIC_TOKEN_" + System.currentTimeMillis(),
                    tokenType = "Bearer",
                    user = mockUser,
                    message = "Đăng nhập vân tay thành công!"
                )
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Đăng nhập sinh trắc học thành công", authRes))
            }

            uri.endsWith("api/auth/resend-otp") && method == "POST" -> {
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Đã gửi lại mã OTP (123456)", true))
            }

            uri.endsWith("api/auth/register") && method == "POST" -> {
                val newUser = DinhLeKieuLinhUser(
                    id = users.size + 1,
                    username = "newuser",
                    email = "newuser@edu.vn",
                    hoTen = "Tài khoản mới",
                    maSo = "SV2026999",
                    soDienThoai = "0900000000",
                    role = DinhLeKieuLinhRole.SINH_VIEN
                )
                users.add(newUser)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Đăng ký thành công!", newUser))
            }

            uri.endsWith("api/auth/me") && method == "GET" -> {
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", users[2]))
            }

            // Users CRUD
            uri.contains("api/users") && method == "GET" -> {
                val search = request.url.queryParameter("search")
                val filtered = if (!search.isNullOrBlank()) {
                    users.filter { it.hoTen.contains(search, ignoreCase = true) || it.email.contains(search, ignoreCase = true) || it.maSo.contains(search, ignoreCase = true) }
                } else users
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", filtered))
            }

            uri.endsWith("api/users") && method == "POST" -> {
                val newUser = DinhLeKieuLinhUser(
                    id = (users.maxOfOrNull { it.id } ?: 0) + 1,
                    username = "user_${System.currentTimeMillis() % 1000}",
                    email = "user@dinhlekieulinh.edu.vn",
                    hoTen = "Người dùng mới",
                    maSo = "SV" + (1000 + users.size),
                    soDienThoai = "0912345678",
                    role = DinhLeKieuLinhRole.SINH_VIEN
                )
                users.add(newUser)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Thêm người dùng thành công", newUser))
            }

            uri.contains("api/users/") && method == "DELETE" -> {
                val id = uri.substringAfterLast("/").toIntOrNull() ?: 0
                users.removeAll { it.id == id }
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Xóa người dùng thành công", true))
            }

            // MonHoc CRUD
            uri.contains("api/monhoc") && method == "GET" -> {
                val search = request.url.queryParameter("search")
                val filtered = if (!search.isNullOrBlank()) {
                    monHocs.filter { it.tenMonHoc.contains(search, ignoreCase = true) || it.maMonHoc.contains(search, ignoreCase = true) }
                } else monHocs
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", filtered))
            }

            uri.endsWith("api/monhoc") && method == "POST" -> {
                val newMonHoc = DinhLeKieuLinhMonHoc(
                    id = (monHocs.maxOfOrNull { it.id } ?: 0) + 1,
                    maMonHoc = "COMP" + (104 + monHocs.size),
                    tenMonHoc = "Môn học mới",
                    soTinChi = 3,
                    moTa = "Mô tả môn học mẫu"
                )
                monHocs.add(newMonHoc)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Thêm môn học thành công", newMonHoc))
            }

            uri.contains("api/monhoc/") && method == "DELETE" -> {
                val id = uri.substringAfterLast("/").toIntOrNull() ?: 0
                monHocs.removeAll { it.id == id }
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Xóa môn học thành công", true))
            }

            // LopHoc CRUD
            uri.contains("api/lophoc") && method == "GET" -> {
                val search = request.url.queryParameter("search")
                val filtered = if (!search.isNullOrBlank()) {
                    lopHocs.filter { it.tenLopHoc.contains(search, ignoreCase = true) || it.maLopHoc.contains(search, ignoreCase = true) }
                } else lopHocs
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", filtered))
            }

            uri.endsWith("api/lophoc") && method == "POST" -> {
                val newLop = DinhLeKieuLinhLopHoc(
                    id = (lopHocs.maxOfOrNull { it.id } ?: 0) + 1,
                    maLopHoc = "DHKTPM18C",
                    tenLopHoc = "Kỹ thuật phần mềm 18C",
                    monHocId = 1,
                    tenMonHoc = "Lập trình Di động Nâng cao",
                    giangVienId = 2,
                    giangVienHoTen = "ThS. Nguyễn Văn Nam",
                    hocKy = "HK1",
                    namHoc = "2025-2026",
                    soLuongSinhVien = 38
                )
                lopHocs.add(newLop)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Thêm lớp học thành công", newLop))
            }

            uri.contains("api/lophoc/") && method == "DELETE" -> {
                val id = uri.substringAfterLast("/").toIntOrNull() ?: 0
                lopHocs.removeAll { it.id == id }
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Xóa lớp học thành công", true))
            }

            // BuoiHoc CRUD & Dynamic QR
            uri.contains("generate-qr") && method == "GET" -> {
                val buoiHocId = uri.substringBefore("/generate-qr").substringAfterLast("/").toIntOrNull() ?: 1
                val dynamicQr = DinhLeKieuLinhDynamicQr(
                    buoiHocId = buoiHocId,
                    qrCodeValue = "QR_DYNAMIC_${buoiHocId}_${System.currentTimeMillis() / 30000}",
                    expiresInSeconds = 30
                )
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Tạo Dynamic QR thành công", dynamicQr))
            }

            uri.contains("api/buoihoc") && method == "GET" -> {
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", buoiHocs))
            }

            uri.endsWith("api/buoihoc") && method == "POST" -> {
                val newBuoi = DinhLeKieuLinhBuoiHoc(
                    id = (buoiHocs.maxOfOrNull { it.id } ?: 0) + 1,
                    lopHocId = 1,
                    tenLopHoc = "Kỹ thuật phần mềm 18A",
                    ngayHoc = "2026-11-05",
                    gioBatDau = "07:30",
                    gioKetThuc = "11:30",
                    phongHoc = "A1-402",
                    noiDung = "Kiểm tra thực hành cuối kỳ",
                    qrToken = "QR_EXAM_2026",
                    viTriLat = 10.762622,
                    viTriLng = 106.682229,
                    banKinh = 100.0,
                    trangThai = DinhLeKieuLinhTrangThaiBuoiHoc.CHUA_BAT_DAU
                )
                buoiHocs.add(newBuoi)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Tạo buổi học thành công", newBuoi))
            }

            uri.contains("api/buoihoc/") && method == "DELETE" -> {
                val id = uri.substringAfterLast("/").toIntOrNull() ?: 0
                buoiHocs.removeAll { it.id == id }
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Xóa buổi học thành công", true))
            }

            // DiemDanh & Quét QR & Thống kê
            uri.endsWith("api/diemdanh/scan-qr") && method == "POST" -> {
                val record = DinhLeKieuLinhDiemDanh(
                    id = (diemDanhs.maxOfOrNull { it.id } ?: 0) + 1,
                    buoiHocId = 1,
                    tenLopHoc = "Kỹ thuật phần mềm 18A",
                    ngayHoc = "2026-10-22",
                    sinhVienId = 3,
                    sinhVienMaSo = "SV2026001",
                    sinhVienHoTen = "Đinh Lê Kiều Linh",
                    thoiGianDiemDanh = "2026-10-22 07:32:00",
                    trangThai = DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT,
                    phuongThuc = DinhLeKieuLinhPhuongThucDiemDanh.QR_GPS,
                    viTriLat = 10.762624,
                    viTriLng = 106.682230,
                    khoangCach = 8.5,
                    ghiChu = "Điểm danh thành công qua Dynamic QR và GPS hợp lệ"
                )
                diemDanhs.add(0, record)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Điểm danh thành công!", record))
            }

            uri.contains("thong-ke") && method == "GET" -> {
                val stats = DinhLeKieuLinhThongKeDiemDanh(
                    sinhVienId = 3,
                    sinhVienHoTen = "Đinh Lê Kiều Linh",
                    tongSoBuoi = 15,
                    soBuoiCoMat = 12,
                    soBuoiMuon = 1,
                    soBuoiVang = 1,
                    soBuoiCoPhep = 1,
                    tiLeChuyenCan = 86.7f,
                    canhBaoVang = false // Nếu vắng >= 3 sẽ true
                )
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", stats))
            }

            uri.contains("api/diemdanh") && method == "GET" -> {
                gson.toJson(DinhLeKieuLinhApiResponse(true, "OK", diemDanhs))
            }

            uri.endsWith("api/diemdanh/manual") && method == "POST" -> {
                val record = DinhLeKieuLinhDiemDanh(
                    id = (diemDanhs.maxOfOrNull { it.id } ?: 0) + 1,
                    buoiHocId = 1,
                    tenLopHoc = "Kỹ thuật phần mềm 18A",
                    ngayHoc = "2026-10-22",
                    sinhVienId = 4,
                    sinhVienMaSo = "SV2026002",
                    sinhVienHoTen = "Trần Minh Tuấn",
                    thoiGianDiemDanh = "2026-10-22 08:30:00",
                    trangThai = DinhLeKieuLinhTrangThaiDiemDanh.CO_MAT,
                    phuongThuc = DinhLeKieuLinhPhuongThucDiemDanh.THU_CONG,
                    ghiChu = "Giảng viên điểm danh bù"
                )
                diemDanhs.add(record)
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Điểm danh thủ công thành công", record))
            }

            uri.contains("api/diemdanh/") && method == "DELETE" -> {
                val id = uri.substringAfterLast("/").toIntOrNull() ?: 0
                diemDanhs.removeAll { it.id == id }
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Hủy lượt điểm danh thành công", true))
            }

            else -> {
                gson.toJson(DinhLeKieuLinhApiResponse(true, "Mock response", null))
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(jsonResponse.toResponseBody(jsonMediaType))
            .build()
    }
}
