package com.thiago.android.breakingnews.platform

expect class Platform() {
    val osName: String
    val osVersion: String
    val deviceModel: String
}