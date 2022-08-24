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
                analytics.track("Track")
            }

            signedOut.setOnClickListener {
                analytics.track("Signed Out")
            }

            identify.setOnClickListener {
                analytics.identify(
                    userId = "X-1234567890",
                    traits = JsonObject(mapOf("abc" to JsonPrimitive(1)))
                )
            }
        }
    }
}