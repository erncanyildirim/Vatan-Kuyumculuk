package com.suer.vatankuyumculuk

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUserDefaults

/** Launch argument `-startScreen CALCULATOR` (read through NSUserDefaults) opens that screen; see [App]. */
fun MainViewController() = ComposeUIViewController {
    App(startScreen = NSUserDefaults.standardUserDefaults.stringForKey("startScreen"))
}