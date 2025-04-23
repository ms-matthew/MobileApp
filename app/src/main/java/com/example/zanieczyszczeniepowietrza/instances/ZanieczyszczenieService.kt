package com.example.zanieczyszczeniepowietrza.instances


import retrofit2.http.GET
import retrofit2.http.Path

interface ZanieczyszczenieService {
    @GET("rest/station/findAll")
    suspend fun searchZanieczyszczenie(): List<Zanieczyszczenie>

    @GET("rest/aqindex/getIndex/{stationID}")
    suspend fun ZanieczyszczeniePoID(
        @Path("stationID") stationID: String
    ): RestIndex
}