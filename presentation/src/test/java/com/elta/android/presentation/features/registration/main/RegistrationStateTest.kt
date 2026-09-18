package com.elta.android.presentation.features.registration.main

import com.elta.android.presentation.features.registration.main.model.RegistrationState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationStateTest {
    private val filled = RegistrationState(email = "user@example.com", password = "Example123")

    @Test
    fun `valid credentials require explicit consent`() {
        assertFalse(filled.canSubmit)
        assertTrue(filled.copy(isPrivacyPolicyAccepted = true).canSubmit)
    }

    @Test
    fun `revoking consent disables a previously valid form`() {
        val accepted = filled.copy(isPrivacyPolicyAccepted = true)
        assertTrue(accepted.canSubmit)
        assertFalse(accepted.copy(isPrivacyPolicyAccepted = false).canSubmit)
    }

    @Test
    fun `clearing either input disables submission even after valid input`() {
        val accepted = filled.copy(isPrivacyPolicyAccepted = true)
        assertFalse(accepted.copy(email = "").canSubmit)
        assertFalse(accepted.copy(password = "").canSubmit)
    }

    @Test
    fun `consent cannot bypass credential validation`() {
        val accepted = filled.copy(isPrivacyPolicyAccepted = true)
        assertFalse(accepted.copy(email = "user@").canSubmit)
        assertFalse(accepted.copy(password = "short").canSubmit)
    }

    @Test
    fun `loading disables resubmission`() {
        assertFalse(filled.copy(isPrivacyPolicyAccepted = true, isLoading = true).canSubmit)
    }
}
