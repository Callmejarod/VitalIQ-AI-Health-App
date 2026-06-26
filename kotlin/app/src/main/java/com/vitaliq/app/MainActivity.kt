package com.vitaliq.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.vitaliq.app.navigation.AppNavigation
import com.vitaliq.app.ui.theme.VitalIQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("VitalIQ", "MainActivity onCreate")
        enableEdgeToEdge()
        setContent {
            VitalIQTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("VitalIQ", "MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("VitalIQ", "MainActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("VitalIQ", "MainActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("VitalIQ", "MainActivity onDestroy")
    }
}
