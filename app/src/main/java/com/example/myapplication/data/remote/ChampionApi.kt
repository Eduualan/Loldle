package com.example.myapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ChampionResponse(val data: Map<String, ChampionDto>)
data class ChampionDto(
    val id: String,
    val name: String,
    val title: String,
    val tags: List<String>,
    val partype: String,
    val stats: ChampionStats
)
data class ChampionStats(val attackrange: Double)

interface ChampionApi {
    @GET("api/versions.json") suspend fun versions(): List<String>
    @GET("cdn/{version}/data/en_US/champion.json")
    suspend fun champions(@Path("version") version: String): ChampionResponse
}

object ApiFactory {
    val api: ChampionApi = Retrofit.Builder()
        .baseUrl("https://ddragon.leagueoflegends.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChampionApi::class.java)
}
