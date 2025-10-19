package com.thiago.android.breakingnews

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform