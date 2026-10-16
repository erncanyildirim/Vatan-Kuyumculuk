package com.suer.vatankuyumculuk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

/** Requires UIViewControllerBasedStatusBarAppearance = NO in Info.plist. */
@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) {
    SideEffect {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (darkTheme) UIStatusBarStyleLightContent else UIStatusBarStyleDarkContent,
        )
    }
}
