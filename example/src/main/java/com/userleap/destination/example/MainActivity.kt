package com.userleap.destination.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.segment.analytics.kotlin.core.Properties
import com.userleap.destination.example.databinding.ActivityMainBinding
import com.userleap.destination.example.ExampleApplication.Companion.analytics
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(ActivityMainBinding.inflate(layoutInflater)) {
            setContentView(root)

            track.setOnClickListener {
                analytics.track(
                    "[TEST] All Question Types - No Routing",
                    properties = Properties(
                        mapOf(
                            "age" to JsonPrimitive(12),
                            "item" to JsonPrimitive("bread"),
                            "isFreePlan" to JsonPrimitive(false)
                        )
                    )
                )
                analytics.track("Track")
            }

            screen.setOnClickListener {
                analytics.screen(
                    "Android Segment Screen",
                    properties = Properties(
                        mapOf(
                            "segmentActionsAndroid" to JsonPrimitive(true),
                            "deviceType" to JsonPrimitive("android")
                        )
                    )
                )
            }

            signedOut.setOnClickListener {
                analytics.track("Signed Out")
            }

            identify.setOnClickListener {
                analytics.identify(
                    userId = "X-1234567890",
                    traits = JsonObject(mapOf("wow" to JsonPrimitive(123)))
                )
            }
        }
    }
}