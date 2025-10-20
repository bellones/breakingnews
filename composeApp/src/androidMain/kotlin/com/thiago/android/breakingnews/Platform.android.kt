package com.thiago.android.breakingnews

import android.os.Build


actual  class Platform {
    actual val osName: String
        get () = "Android"
    actual val osVersion: String
        get() = "${Build.VERSION.SDK_INT}"
    actual val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

}