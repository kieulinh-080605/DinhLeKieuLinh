package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.model.*
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhBuoiHocRepository(
    private val apiService: DinhLeKieuLinhApiService
) {

    fun getBuoiHocs(lopHocId: Int? = null, ngayHoc: String? = null): Flow<DinhLeKieuLinhResource<List<DinhLeKieuLinhBuoiHoc>>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getBuoiHocs(lopHocId, ngayHoc)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải danh sách buổi học"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun createBuoiHoc(request: DinhLeKieuLinhBuoiHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhBuoiHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.createBuoiHoc(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Tạo buổi học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun updateBuoiHoc(id: Int, request: DinhLeKieuLinhBuoiHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhBuoiHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.updateBuoiHoc(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Cập nhật buổi học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun deleteBuoiHoc(id: Int): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.deleteBuoiHoc(id)
            if (response.isSuccessful) {
                emit(DinhLeKieuLinhResource.Success(true))
            } else {
                emit(DinhLeKieuLinhResource.Error(response.body()?.message ?: "Xóa buổi học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    // Dynamic QR tự sinh đổi mỗi 30 giây
    fun generateDynamicQr(buoiHocId: Int): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhDynamicQr>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.generateDynamicQr(buoiHocId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tạo Dynamic QR"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi tạo Dynamic QR: ${e.localizedMessage}", e))
        }
    }
}
