package com.elta.android.domain

import com.elta.android.domain.features.user.interactor.isNameValid
import org.junit.Test

class UserNameTest {

    @Test
    fun minFirstNameLengthFailed() {
        assert(!isNameValid(name = "a"))
    }

    @Test
    fun minSecondNameLengthFailed() {
        assert(!isNameValid(name = "first"))
    }

    @Test
    fun minNameLengthSuccess() {
        assert(isNameValid(name = "fi"))
    }

    @Test
    fun emptyNameFailed() {
        assert(!isNameValid(name = ""))
    }

    @Test
    fun maxFirstNameLengthFailed() {
        assert(
            !isNameValid(
                name = "se"
            )
        )
    }

    @Test
    fun maxSecondNameLengthFailed() {
        assert(
            !isNameValid(
                name = "aaaaaaaaaassssssssssaaaaaaaaaassssssssssddddddddddq"
            )
        )
    }
}
