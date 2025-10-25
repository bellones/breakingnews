package com.thiago.android.breakingnews.platform

import android.os.Build

actual  class Platform {
    actual val osName: String
        get () = "Android"
    actual val osVersion: String
        get() = "${Build.VERSION.SDK_INT}"
    actual val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

}