package com.userleap.destination

import com.segment.analytics.kotlin.core.*
import com.segment.analytics.kotlin.core.platform.Plugin
import com.userleap.Sprig
import com.userleap.destination.SprigDestination.Companion.EMAIL_KEY
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SprigDestinationTests {

    private val sprigDestination: SprigDestination = SprigDestination(mockk(relaxed = true))

    @BeforeEach
    fun setup() {
        mockkObject(Sprig)
    }

    @Test
    fun `settings are updated correctly`() {
        every { 
            Sprig.configure(
                any(),
                TEST_ENV, 
                any()
            )
        } returns Unit

        // An simple settings blob
        val settingsBlob: Settings = Json.decodeFromString(
            """
            {
              "integrations": {
                "${SprigDestination.KEY}": {
                  "envId": "$TEST_ENV"
                }
              }
            }
        """.trimIndent()
        )

        sprigDestination.update(settingsBlob, Plugin.UpdateType.Initial)

        /* assertions about config */
        assertNotNull(sprigDestination.sprigSettings)
        assertEquals(sprigDestination.sprigSettings?.envId, TEST_ENV)

        // Verify Sprig was configured with the envId
        verify(exactly = 1) { 
            Sprig.configure(
                any(),
                TEST_ENV,
                any()
            )
        }
    }

    @Test
    fun `track is handled correctly`() {
        val eventName = "Product Clicked"
        val id = "anonId"
        val uid = "userId"
        val properties = mapOf("Item Name" to JsonPrimitive("Biscuits"))
        val sampleEvent = TrackEvent(
            event = eventName,
            properties = JsonObject(properties)
        ).apply {
            messageId = "qwerty-1234"
            userId = uid
            anonymousId = id
            integrations = emptyJsonObject
            context = emptyJsonObject
            timestamp = "2021-07-13T00:59:09"
        }
        val trackEvent = sprigDestination.track(sampleEvent)

        /* assertions about new event */
        assertNotNull(trackEvent)
        assertEquals(trackEvent.event, eventName)

        // Verify the event is sent to Sprig with the anonymous ID
        verify(exactly = 1) {
            Sprig.track(eventName, uid, id, mapOf("Item Name" to "Biscuits"), any())
        }
    }

    @Test
    fun `Signed Out track event is handled correctly`() {
        every { Sprig.logout() } returns Unit

        val eventName = SprigDestination.SIGNED_OUT_EVENT
        val id = "anonId"

        val sampleEvent = TrackEvent(
            event = eventName,
            properties = emptyJsonObject
        ).apply {
            messageId = "qwerty-1234"
            anonymousId = id
            integrations = emptyJsonObject
            context = emptyJsonObject
            timestamp = "2021-07-13T00:59:09"
        }
        val trackEvent = sprigDestination.track(sampleEvent)

        /* assertions about new event */
        assertNotNull(trackEvent)
        assertEquals(trackEvent.event, eventName)

        // Verify the event triggers a logout
        verify(exactly = 1) { Sprig.logout() }
    }

    @Test
    fun `identify is handled correctly`() {
        val id = "abc-123"
        val anonId = "anonId"
        val email = "sprig@example.com"

        every { Sprig.setVisitorAttributes(any(), any(), any()) } returns Unit

        val sampleEvent = IdentifyEvent(
            userId = id,
            traits = JsonObject(mapOf("email" to JsonPrimitive(email)))
        ).apply {
            messageId = "qwerty-1234"
            anonymousId = anonId
            integrations = emptyJsonObject
            context = emptyJsonObject
            timestamp = "2021-07-13T00:59:09"
        }
        val identifyEvent = sprigDestination.identify(sampleEvent)

        /* assertions about new event */
        assertNotNull(identifyEvent)
        assertEquals(identifyEvent.userId, id)

        // Verify the correct information is sent to Sprig
        val expectedTraits = mapOf(EMAIL_KEY to email)
        verify(exactly = 1) { Sprig.setVisitorAttributes(expectedTraits, id, anonId) }
    }

    @Test
    fun `alias is handled correctly`() {
        val newId = "new ID"
        val oldId = "old ID"
        val event = AliasEvent(userId = newId, previousId = oldId).apply { anonymousId = "" }

        val aliasEvent = sprigDestination.alias(event)
        assertNotNull(aliasEvent)
        assertEquals(aliasEvent.userId, newId)
        assertEquals(aliasEvent.previousId, oldId)

        // No anonymous ID should be set in aliasing
        verify(exactly = 0) { Sprig.setPartnerAnonymousId(any()) }
        // Sprig is sent the new user ID
        verify(exactly = 1) { Sprig.setUserIdentifier(newId) }
    }

    @Test
    fun `reset is handled correctly`() {
        every { Sprig.logout() } returns Unit
        sprigDestination.reset()
        verify(exactly = 1) { Sprig.logout() }
    }

    companion object {
        private const val TEST_ENV = "Test-Env"
    }
}
