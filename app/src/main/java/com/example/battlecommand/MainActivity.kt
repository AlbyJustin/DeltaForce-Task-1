package com.example.battlecommand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.battlecommand.ui.screens.HomeScreen
import com.example.battlecommand.ui.theme.BattleCommandTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BattleCommandTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    MainApp(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

enum class Screens {
    HomeScreen,
    GameScreen
}

@Composable
fun MainApp(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screens.HomeScreen.name) {
        composable(Screens.HomeScreen.name) {
            HomeScreen(onPlayClick = { navController.navigate(Screens.GameScreen.name) })
        }
        composable(Screens.GameScreen.name) {
            GameScreen(modifier = modifier)
        }
    }
}







