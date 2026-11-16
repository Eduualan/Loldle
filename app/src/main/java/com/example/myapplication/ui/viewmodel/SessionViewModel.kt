package com.example.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SessionViewModel : ViewModel() {
    var role by mutableStateOf(ROLE_PLAYER)
        private set

    fun loginPlayer() { role = ROLE_PLAYER }

    fun loginAdmin(pin: String): Boolean {
        if (pin != ADMIN_PIN) return false
        role = ROLE_ADMIN
        return true
    }

    private companion object {
        const val ROLE_PLAYER = "PLAYER"
        const val ROLE_ADMIN = "ADMIN"
        const val ADMIN_PIN = "1234"
    }
}
