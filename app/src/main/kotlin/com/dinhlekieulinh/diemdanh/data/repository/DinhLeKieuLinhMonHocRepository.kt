package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHoc
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhMonHocRequest
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhResource
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhMonHocRepository(
    private val apiService: DinhLeKieuLinhApiService
) {

    fun getMonHocs(search: String? = null): Flow<DinhLeKieuLinhResource<List<DinhLeKieuLinhMonHoc>>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getMonHocs(search)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải môn học"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun createMonHoc(request: DinhLeKieuLinhMonHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhMonHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.createMonHoc(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Thêm môn học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun updateMonHoc(id: Int, request: DinhLeKieuLinhMonHocRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhMonHoc>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.updateMonHoc(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Cập nhật môn học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun deleteMonHoc(id: Int): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.deleteMonHoc(id)
            if (response.isSuccessful) {
                emit(DinhLeKieuLinhResource.Success(true))
            } else {
                emit(DinhLeKieuLinhResource.Error(response.body()?.message ?: "Xóa môn học thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }
}
