package com.elta.android.domain

import com.elta.android.domain.features.user.interactor.isNameValid
import org.junit.Test

class UserNameTest {

    @Test
    fun minNameLengthFailed() {
        assert(!isNameValid(name = "a"))
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
    fun maxNameLengthFailed() {
        assert(
            !isNameValid(
                name = "qqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrrrrtttttttttty"
            )
        )
    }
}
