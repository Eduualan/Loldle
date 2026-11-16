package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.screens.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.AdminViewModel
import com.example.myapplication.ui.viewmodel.AppViewModelFactory
import com.example.myapplication.ui.viewmodel.GameViewModel
import com.example.myapplication.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as LoldleApp).repository
        val factory = AppViewModelFactory(repository)
        val game = ViewModelProvider(this, factory)[GameViewModel::class.java]
        val admin = ViewModelProvider(this, factory)[AdminViewModel::class.java]
        val session = ViewModelProvider(this, factory)[SessionViewModel::class.java]
        if (savedInstanceState == null) lifecycleScope.launch { repository.initialise() }
        setContent {
            MyApplicationTheme { AppNavigation(session, game, admin) }
        }
    }
}
