package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhLopHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhLopHocRequest
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhResource
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhLopHocRepository(
    private val apiService: DinhLeKieuLinhApiService
) {

    fun getLopHocs(search: String? = null, giangVienId: Int? = null): Flow<DinhLeKieuLinhResource<List<DinhLeKieuLinhLopHoc>>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getLopHocs(search, giangVienId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải danh sách lớp học"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun createLopHoc(request: DinhLeKieuLinhLopHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhLopHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.createLopHoc(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Thêm lớp học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun updateLopHoc(id: Int, request: DinhLeKieuLinhLopHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhLopHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.updateLopHoc(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Cập nhật lớp học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun deleteLopHoc(id: Int): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.deleteLopHoc(id)
            if (response.isSuccessful) {
                emit(DinhLeKieuLinhResource.Success(true))
            } else {
                emit(DinhLeKieuLinhResource.Error(response.body()?.message ?: "Xóa lớp học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }
}
