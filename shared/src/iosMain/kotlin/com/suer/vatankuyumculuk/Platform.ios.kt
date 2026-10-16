package com.suer.vatankuyumculuk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

internal actual val platformKind: PlatformKind = PlatformKind.IOS

@Composable
internal actual fun rememberShareText(): (String) -> Unit = remember {
    { text: String ->
        val root = keyWindow()?.rootViewController
        var top: UIViewController? = root
        while (top?.presentedViewController != null) top = top.presentedViewController
        if (top != null) {
            val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            controller.popoverPresentationController?.sourceView = top.view
            top.presentViewController(controller, animated = true, completion = null)
        }
    }
}

private fun keyWindow(): UIWindow? {
    val scenes = UIApplication.sharedApplication.connectedScenes
    for (scene in scenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        @Suppress("UNCHECKED_CAST")
        val windows = windowScene.windows as List<UIWindow>
        windows.firstOrNull { it.isKeyWindow() }?.let { return it }
        windows.firstOrNull()?.let { return it }
    }
    return null
}

internal actual fun platformConfigEndpoints(): List<String> = AppLinks.configEndpoints
