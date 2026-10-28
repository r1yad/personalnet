package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.PersonaNetApp
import com.example.ui.PersonaNetViewModel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.PersonaNetTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PersonaNetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonaNetTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    PersonaNetApp(viewModel = viewModel)
                }
            }
        }
    }
}
