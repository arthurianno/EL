package com.elta.android.presentation.features.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthFormValidationTest {
    @Test
    fun `empty fields have no error and valid credentials clear errors`() {
        assertNull(emailInputError("", "Invalid email"))
        assertNull(passwordInputError("", "Invalid password"))
        assertNull(emailInputError("user@example.com", "Invalid email"))
        assertNull(passwordInputError("Example123", "Invalid password"))
    }

    @Test
    fun `invalid nonempty credentials show their own error`() {
        assertEquals("Invalid email", emailInputError("user@", "Invalid email"))
        assertEquals("Invalid password", passwordInputError("short", "Invalid password"))
    }
}
