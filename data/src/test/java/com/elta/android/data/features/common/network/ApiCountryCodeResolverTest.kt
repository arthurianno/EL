package com.elta.android.data.features.common.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiCountryCodeResolverTest {

    @Test
    fun `normalize returns uppercase iso alpha 2 code`() {
        assertEquals("IN", ApiCountryCodeResolver.normalize(" in "))
    }

    @Test
    fun `normalize falls back to RU for missing or invalid code`() {
        assertEquals("RU", ApiCountryCodeResolver.normalize(null))
        assertEquals("RU", ApiCountryCodeResolver.normalize(""))
        assertEquals("RU", ApiCountryCodeResolver.normalize("rus"))
        assertEquals("RU", ApiCountryCodeResolver.normalize("1N"))
    }
}
