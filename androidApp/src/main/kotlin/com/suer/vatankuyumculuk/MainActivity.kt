package com.suer.vatankuyumculuk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // `adb shell am start -n com.suer.vatankuyumculuk/.MainActivity -e startScreen CALCULATOR` opens that screen.
        val startScreen = intent.getStringExtra("startScreen")
        setContent {
            App(startScreen)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}