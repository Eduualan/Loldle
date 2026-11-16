package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.GameDatabase
import com.example.myapplication.data.remote.ApiFactory
import com.example.myapplication.data.repository.ChampionRepository

class LoldleApp : Application() {
    val repository by lazy {
        val db = GameDatabase.create(this)
        ChampionRepository(db.champions(), db.games(), ApiFactory.api, ApiFactory.traitsApi)
    }
}
