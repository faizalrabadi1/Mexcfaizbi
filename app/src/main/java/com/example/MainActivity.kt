package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val navController = rememberNavController()
          NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
          ) {
            composable("auth") {
              AuthScreen(
                onNavigateToAdmin = { navController.navigate("admin") },
                onNavigateToDashboard = { navController.navigate("dashboard") }
              )
            }
            composable("admin") {
              AdminScreen(onBack = { navController.popBackStack() })
            }
            composable("dashboard") {
              DashboardScreen(
                onLogout = {
                  navController.navigate("auth") {
                    popUpTo(0)
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

