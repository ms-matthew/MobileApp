package com.example.zanieczyszczeniepowietrza.instances

data class Zanieczyszczenie(
    val id: String = "",
    val stationName: String = "",
    val gegrLat: String = "",
    val gegrLon: String = "",
    val city: City,
    val addressStreet: String = ""
)