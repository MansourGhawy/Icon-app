package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppGridScreen
import com.example.ui.IconEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppListViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: AppListViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          IconicAppNavigation(viewModel)
        }
      }
    }
  }
}

@Composable
fun IconicAppNavigation(viewModel: AppListViewModel) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = "apps"
  ) {
    composable("apps") {
      AppGridScreen(
        viewModel = viewModel,
        onAppSelected = {
          navController.navigate("editor")
        }
      )
    }
    composable("editor") {
      IconEditorScreen(
        viewModel = viewModel,
        onBack = {
          navController.popBackStack()
        }
      )
    }
  }
}

