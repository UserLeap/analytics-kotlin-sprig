package com.userleap.destination.example

import android.app.Application
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.userleap.destination.example.BuildConfig
import com.userleap.destination.SprigDestination

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        analytics = Analytics(BuildConfig.SEGMENT_WRITE_KEY, this) {
            // Automatically track Lifecycle events
            trackApplicationLifecycleEvents = true
            flushAt = 3
            flushInterval = 10
        }.add(SprigDestination(application = this))
    }

    companion object {
        lateinit var analytics: Analytics
    }
}