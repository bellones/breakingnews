package com.thiago.android.breakingnews.platform

import platform.UIKit.UIDevice

actual  class Platform {
    actual val osName: String
        get () = UIDevice.Companion.currentDevice.systemName
    actual val osVersion: String
        get() = UIDevice.Companion.currentDevice.systemVersion
    actual val deviceModel: String
        get() = UIDevice.Companion.currentDevice.model

}