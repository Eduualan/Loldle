package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.ChampionRepository

class AppViewModelFactory(private val repository: ChampionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        GameViewModel::class.java -> GameViewModel(repository) as T
        AdminViewModel::class.java -> AdminViewModel(repository) as T
        SessionViewModel::class.java -> SessionViewModel() as T
        else -> error("ViewModel desconhecido: ${modelClass.name}")
    }
}
