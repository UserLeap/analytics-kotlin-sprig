package com.userleap.destination

import android.app.Activity
import android.app.Application
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.segment.analytics.kotlin.android.plugins.AndroidLifecycle
import com.segment.analytics.kotlin.core.*
import com.segment.analytics.kotlin.core.platform.DestinationPlugin
import com.segment.analytics.kotlin.core.platform.Plugin
import com.segment.analytics.kotlin.core.utilities.toContent
import com.userleap.Sprig
import com.userleap.SurveyState
import com.userleap.destination.SprigDestination.Companion.EMAIL_KEY
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.lang.ref.WeakReference

@Keep
@Serializable
data class SprigSettings(val envId: String)

@Keep
class SprigDestination(
    private val application: Application
) : DestinationPlugin(), AndroidLifecycle {

    override val key: String = KEY

    @VisibleForTesting
    var sprigSettings: SprigSettings? = null

    // The current active activity to show any surveys on
    private var activityReference: WeakReference<FragmentActivity>? = null

    override fun update(settings: Settings, type: Plugin.UpdateType) {
        super.update(settings, type)
        sprigSettings = settings.destinationSettings<SprigSettings>(key)?.also {
            it.envId.ifNotEmpty()?.let { environment ->
                Sprig.configure(
                    application,
                    environment, 
                    mapOf(
                        "x-ul-installation-method" to "android-segment",
                        "x-ul-package-version" to BuildConfig.PACKAGE_VERSION
                    ),
                    null
                )
            }
        }
    }

    override fun identify(payload: IdentifyEvent) = payload.also {
        Sprig.setVisitorAttributes(
            attributes = getTraits(it.traits),
            userId = it.userId,
            partnerAnonymousId = it.anonymousId
        )
    }

    override fun track(payload: TrackEvent) = payload.also {
        if (it.event == SIGNED_OUT_EVENT) {
            Sprig.logout()
        } else {
            Sprig.track(
                event = it.event,
                userId = it.userId,
                partnerAnonymousId = it.anonymousId,
                properties = getProperties(it.properties),
            ) { surveyState ->
                if (surveyState == SurveyState.READY) {
                    activityReference?.get()?.let(Sprig::presentSurvey)
                }
            }
        }
    }

    override fun screen(payload: ScreenEvent) = payload.also {
        Sprig.track(
            event = it.name,
            userId = it.userId,
            partnerAnonymousId = it.anonymousId,
            properties = getProperties(it.properties),
        ) { surveyState ->
            if (surveyState == SurveyState.READY) {
                activityReference?.get()?.let(Sprig::presentSurvey)
            }
        }
    }

    /**
     * Switch user IDs
     */
    override fun alias(payload: AliasEvent) = payload.also {
        it.userId.ifNotEmpty()?.let { newId ->
            recordAnonymousId(it)
            Sprig.setUserIdentifier(newId)
        }
    }

    override fun reset() {
        Sprig.logout()
    }

    override fun onActivityResumed(activity: Activity?) {
        if (activity is FragmentActivity) {
            activityReference = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity?) {
        activityReference = null
    }

    private fun recordAnonymousId(event: BaseEvent) {
        event.anonymousId.ifNotEmpty()?.let(Sprig::setPartnerAnonymousId)
    }

    private fun getTraits(traits: JsonObject) = mutableMapOf<String, String>().apply {
        traits.entries.mapNotNull { (key, value) ->
            value.asString().ifNotEmpty()?.let {
                this[key.filterEmail()] = it
            }
        }
    }

    private fun getProperties(properties: JsonObject) = mutableMapOf<String, Any>().apply {
        properties.entries.mapNotNull { (key, value) ->
            value.toContent()?.let {
                this[key] = it
            }
        }
    }

    companion object {
        @VisibleForTesting
        const val KEY = "Sprig (Actions)"

        @VisibleForTesting
        const val EMAIL_KEY = "!email"

        @VisibleForTesting
        const val SIGNED_OUT_EVENT = "Signed Out"
    }
}

private fun String?.ifNotEmpty(): String? = takeIf { !this.isNullOrEmpty() }
private fun String.filterEmail() = if (equals("email", ignoreCase = true)) EMAIL_KEY else this
private fun JsonElement.asString() = if (this is JsonPrimitive && isString) content else toString()
