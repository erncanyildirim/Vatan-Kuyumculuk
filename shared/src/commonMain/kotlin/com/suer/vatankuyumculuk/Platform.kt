package com.suer.vatankuyumculuk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform