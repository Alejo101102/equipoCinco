package com.univalle.equipocinco.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.univalle.equipocinco.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var fromWidget: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fromWidget = intent.getBooleanExtra("fromWidget", false)
        handleWidgetNavigation()
    }

    private fun handleWidgetNavigation() {
        val destination = intent.getStringExtra("destination")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        when (destination) {
            "login" -> {
                navController?.navigate(R.id.loginFragment)
            }
            "home" -> {
                navController?.navigate(R.id.homeFragment)
            }
        }
    }
}