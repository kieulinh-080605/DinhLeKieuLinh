package com.dinhlekieulinh.diemdanh.data.model

import com.google.gson.annotations.SerializedName

data class DinhLeKieuLinhMonHoc(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("maMonHoc") val maMonHoc: String = "",
    @SerializedName("tenMonHoc") val tenMonHoc: String = "",
    @SerializedName("soTinChi") val soTinChi: Int = 3,
    @SerializedName("moTa") val moTa: String = ""
)

data class DinhLeKieuLinhMonHocRequest(
    @SerializedName("maMonHoc") val maMonHoc: String,
    @SerializedName("tenMonHoc") val tenMonHoc: String,
    @SerializedName("soTinChi") val soTinChi: Int,
    @SerializedName("moTa") val moTa: String = ""
)
