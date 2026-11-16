package com.example.myapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ChampionResponse(val data: Map<String, ChampionDto>)
data class ChampionDto(val id: String, val name: String, val title: String, val tags: List<String>, val partype: String, val stats: ChampionStats)
data class ChampionStats(val attackrange: Double)
interface ChampionApi {
    @GET("api/versions.json") suspend fun versions(): List<String>
    @GET("cdn/{version}/data/en_US/champion.json")
    suspend fun champions(@Path("version") version: String): ChampionResponse
}

// Gênero, espécie e regiões complementares extraídos do repositório comunitário LoLdleData.
data class ChampionTraitsDto(val id: String, val gender: String?, val species: List<String>?, val regions: List<String>?)
interface ChampionTraitsApi {
    @GET("iEranDEV/loldle-data/refs/heads/master/output.json")
    suspend fun championTraits(): List<ChampionTraitsDto>
}

object ApiFactory {
    val api: ChampionApi = create(
        baseUrl = "https://ddragon.leagueoflegends.com/",
        service = ChampionApi::class.java
    )

    val traitsApi: ChampionTraitsApi = create(
        baseUrl = "https://raw.githubusercontent.com/",
        service = ChampionTraitsApi::class.java
    )

    private fun <T> create(baseUrl: String, service: Class<T>): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(service)
}
