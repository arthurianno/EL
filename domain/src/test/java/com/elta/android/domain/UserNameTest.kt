package com.elta.android.domain

import com.elta.android.domain.features.user.interactor.isNameValid
import org.junit.Test

class UserNameTest {

    @Test
    fun minFirstNameLengthFailed() {
        assert(!isNameValid(firstName = "a", secondName = "second"))
    }

    @Test
    fun minSecondNameLengthFailed() {
        assert(!isNameValid(firstName = "first", secondName = "s"))
    }

    @Test
    fun minNameLengthSuccess() {
        assert(isNameValid(firstName = "fi", secondName = "se"))
    }

    @Test
    fun emptyNameFailed() {
        assert(!isNameValid(firstName = "", secondName = ""))
    }

    @Test
    fun maxFirstNameLengthFailed() {
        assert(
            !isNameValid(
                firstName = "aaaaaaaaaassssssssssaaaaaaaaaassssssssssddddddddddq",
                secondName = "se"
            )
        )
    }

    @Test
    fun maxSecondNameLengthFailed() {
        assert(
            !isNameValid(
                firstName = "sa",
                secondName = "aaaaaaaaaassssssssssaaaaaaaaaassssssssssddddddddddq"
            )
        )
    }
}
