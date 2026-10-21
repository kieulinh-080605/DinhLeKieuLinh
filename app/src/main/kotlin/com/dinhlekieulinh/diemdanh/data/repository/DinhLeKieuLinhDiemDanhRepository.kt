package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhDiemDanhRepository(
    private val apiService: DinhLeKieuLinhApiService
) {

    fun getDiemDanhs(buoiHocId: Int? = null, sinhVienId: Int? = null): Flow<DinhLeKieuLinhResource<List<DinhLeKieuLinhDiemDanh>>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getDiemDanhs(buoiHocId, sinhVienId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải danh sách điểm danh"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Quét QR + Xác thực Vị trí GPS bán kính 100m
    fun scanQrAttendance(request: DinhLeKieuLinhScanQrRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhDiemDanh>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.scanQrAttendance(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Điểm danh qua QR thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Điểm danh thủ công
    fun manualAttendance(request: DinhLeKieuLinhManualDiemDanhRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhDiemDanh>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.manualAttendance(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Điểm danh thủ công thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun updateDiemDanh(id: Int, request: DinhLeKieuLinhManualDiemDanhRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhDiemDanh>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.updateDiemDanh(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Cập nhật lượt điểm danh thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun deleteDiemDanh(id: Int): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.deleteDiemDanh(id)
            if (response.isSuccessful) {
                emit(DinhLeKieuLinhResource.Success(true))
            } else {
                emit(DinhLeKieuLinhResource.Error(response.body()?.message ?: "Hủy lượt điểm danh thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Thống kê chuyên cần và kiểm tra vắng >= 3 buổi
    fun getThongKeDiemDanh(sinhVienId: Int): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhThongKeDiemDanh>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getThongKeDiemDanh(sinhVienId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải thống kê chuyên cần"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }
}
