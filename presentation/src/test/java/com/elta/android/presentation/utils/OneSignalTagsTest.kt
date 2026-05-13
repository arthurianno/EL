package com.elta.android.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneSignalTagsTest {

    @Test
    fun `buildTags contains required targeting keys`() {
        val tags = OneSignalTags.buildTags(
            languageCode = "ru",
            countryCode = "in",
            version = "2.10.7",
            isDebug = true
        )

        assertEquals("dev", tags.getValue("env"))
        assertEquals("dev", tags.getValue("environment"))
        assertEquals("ru", tags.getValue("language_tag"))
        assertEquals("android", tags.getValue("platform"))
        assertEquals("2.10.7", tags.getValue("version"))
        assertEquals("ru|android|2.10.7", tags.getValue("news_segment"))
        assertEquals("IN", tags.getValue("country_code"))
    }

    @Test
    fun `buildTags falls back to supported language and country`() {
        val tags = OneSignalTags.buildTags(
            languageCode = "de",
            countryCode = "invalid",
            version = "2.10.7",
            isDebug = false
        )

        assertEquals("prod", tags.getValue("env"))
        assertEquals("en", tags.getValue("language_tag"))
        assertEquals("RU", tags.getValue("country_code"))
        assertTrue(tags.keys.containsAll(listOf("env", "language_tag", "platform", "news_segment", "country_code")))
    }
}
