package com.elta.android.presentation.features.auth.login

import com.elta.android.presentation.features.auth.login.viewmodel.LoginDestination
import com.elta.android.presentation.features.auth.login.viewmodel.resolveLoginDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginSessionTest {
    @Test
    fun `unactivated account opens email activation`() {
        assertEquals(
            LoginDestination.Activate,
            resolveLoginDestination(false, true, true, true)
        )
    }

    @Test
    fun `server email confirmation takes precedence over onboarding`() {
        assertEquals(
            LoginDestination.Activate,
            resolveLoginDestination(true, false, true, true)
        )
    }

    @Test
    fun `confirmed account without onboarding opens onboarding`() {
        assertEquals(
            LoginDestination.Onboarding,
            resolveLoginDestination(true, true, false, true)
        )
    }

    @Test
    fun `completed onboarding selects the configured home variant`() {
        assertEquals(LoginDestination.Home, resolveLoginDestination(true, true, true, true))
        assertEquals(LoginDestination.HomeVariantA, resolveLoginDestination(true, true, true, false))
    }
}
