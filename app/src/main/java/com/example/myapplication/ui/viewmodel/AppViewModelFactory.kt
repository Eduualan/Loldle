package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.ChampionRepository

class AppViewModelFactory(private val repository: ChampionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(repository)
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> AdminViewModel(repository)
            modelClass.isAssignableFrom(SessionViewModel::class.java) -> SessionViewModel()
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
        return viewModel as T
    }
}
