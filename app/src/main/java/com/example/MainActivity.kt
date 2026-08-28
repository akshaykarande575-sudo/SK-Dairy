package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.DairyDatabase
import com.example.data.DairyRepository
import com.example.ui.DairyApp
import com.example.ui.DairyViewModel
import com.example.ui.DairyViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

  private val viewModel: DairyViewModel by viewModels {
    val db = DairyDatabase.getDatabase(applicationContext, lifecycleScope)
    val repository = DairyRepository(
      cowDao = db.cowDao(),
      breedingDao = db.breedingDao(),
      milkDao = db.milkDao(),
      expenseDao = db.expenseDao()
    )
    DairyViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channel for 5-day delivery and 3-month post-calving alerts
    NotificationHelper.createNotificationChannel(this)

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          DairyApp(viewModel = viewModel)
        }
      }
    }
  }
}
