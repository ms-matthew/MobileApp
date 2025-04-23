package com.example.zanieczyszczeniepowietrza.instances

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("commune") val commune: Commune
)

data class Commune(
    @SerializedName("communeName") val communeName: String,
    @SerializedName("districtName") val districtName: String,
    @SerializedName("provinceName") val provinceName: String
)

data class RestIndex(
    @SerializedName("id") val id: String,
    @SerializedName("stCalcDate") val stCalcDate: String,
    @SerializedName("stIndexLevel") val stIL: IndexLevel?,
    @SerializedName("stSourceDataDate") val stSourceDataDate: String,
    @SerializedName("so2CalcDate") val so2CalcDate: String,
    @SerializedName("so2IndexLevel") val so2IndexLevel: IndexLevel?,
    @SerializedName("so2SourceDataDate") val so2SourceDataDate: String,
    @SerializedName("no2CalcDate") val no2CalcDate: String,
    @SerializedName("no2IndexLevel") val no2IL: IndexLevel?,
    @SerializedName("no2SourceDataDate") val no2SourceDataDate: String,
    @SerializedName("pm10CalcDate") val pm10CalcDate: String,
    @SerializedName("pm10IndexLevel") val pm10IL: IndexLevel?,
    @SerializedName("pm10SourceDataDate") val pm10SourceDataDate: String,
    @SerializedName("pm25CalcDate") val pm25CalcDate: String,
    @SerializedName("pm25IndexLevel") val pm25IL: IndexLevel?,
    @SerializedName("pm25SourceDataDate") val pm25SourceDataDate: String,
    @SerializedName("o3CalcDate") val o3CalcDate: String,
    @SerializedName("o3IndexLevel") val o3IL: IndexLevel?,
    @SerializedName("o3SourceDataDate") val o3SourceDataDate: String,
    @SerializedName("stIndexStatus") val stIndexStatus: String,
    @SerializedName("stIndexCrParam") val stIndexCrParam: String
)

data class IndexLevel(
    @SerializedName("id") val id: String?,
    @SerializedName("indexLevelName") val indexLevelName: String?
)
