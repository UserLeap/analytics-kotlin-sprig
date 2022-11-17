package com.userleap.destination.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.userleap.destination.example.ExampleApplication.Companion.analytics
import com.userleap.destination.example.databinding.ActivityMainBinding
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(ActivityMainBinding.inflate(layoutInflater)) {
            setContentView(root)

            track.setOnClickListener {
                analytics.track(
                    "android segment",
                    properties = JsonObject(mapOf(
                        "age" to JsonPrimitive(12),
                        "item" to JsonPrimitive("bread"),
                        "isFreePlan" to JsonPrimitive(false)))
                )
                analytics.track("Track")
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