package com.dinhlekieulinh.diemdanh.data.repository

import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhResource
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUser
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUserRequest
import com.dinhlekieulinh.diemdanh.data.remote.DinhLeKieuLinhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DinhLeKieuLinhUserRepository(
    private val apiService: DinhLeKieuLinhApiService
) {

    fun getUsers(search: String? = null, role: String? = null): Flow<DinhLeKieuLinhResource<List<DinhLeKieuLinhUser>>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.getUsers(search, role)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Không thể tải danh sách người dùng"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun createUser(request: DinhLeKieuLinhUserRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhUser>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.createUser(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Thêm người dùng thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun updateUser(id: Int, request: DinhLeKieuLinhUserRequest): Flow<DinhLeKieuLinhResource<DinhLeKieuLinhUser>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.updateUser(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(DinhLeKieuLinhResource.Success(body.data))
            } else {
                emit(DinhLeKieuLinhResource.Error(body?.message ?: "Cập nhật người dùng thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }

    fun deleteUser(id: Int): Flow<DinhLeKieuLinhResource<Boolean>> = flow {
        emit(DinhLeKieuLinhResource.Loading)
        try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful) {
                emit(DinhLeKieuLinhResource.Success(true))
            } else {
                emit(DinhLeKieuLinhResource.Error(response.body()?.message ?: "Xóa người dùng thất bại"))
            }
        } catch (e: Exception) {
            emit(DinhLeKieuLinhResource.Error("Lỗi kết nối: ${e.localizedMessage}", e))
        }
    }
}
