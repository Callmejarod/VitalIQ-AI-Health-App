package com.vitaliq.app

import android.app.Application
import android.util.Log
import com.vitaliq.app.di.ServiceLocator

/**
 * Application entry point. Initializes the manual composition root
 * ([ServiceLocator]) once, with the application context, before any screen or
 * ViewModel is created. This is plain Android — not a DI framework.
 */
class VitalIQApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("VitalIQ", "VitalIQApp onCreate — initializing ServiceLocator")
        ServiceLocator.init(this)
    }
}
