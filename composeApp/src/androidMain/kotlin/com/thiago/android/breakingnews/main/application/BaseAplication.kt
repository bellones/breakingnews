package com.thiago.android.breakingnews.main.application
import android.app.Application
import com.thiago.android.breakingnews.di.main.initializeKoin
import org.koin.android.ext.koin.androidContext

class BaseAplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@BaseAplication)
        }
    }

}