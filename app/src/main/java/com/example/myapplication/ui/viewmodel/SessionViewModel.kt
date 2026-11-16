package com.example.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SessionViewModel : ViewModel() {
    var role by mutableStateOf(PLAYER)
        private set

    fun loginPlayer() {
        role = PLAYER
    }

    fun loginAdmin(pin: String): Boolean {
        if (pin != ADMIN_PIN) return false
        role = ADMIN
        return true
    }
    private companion object {
        const val PLAYER = "PLAYER"
        const val ADMIN = "ADMIN"
        const val ADMIN_PIN = "1234"
    }
}
