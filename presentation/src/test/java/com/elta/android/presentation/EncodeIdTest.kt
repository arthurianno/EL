package com.elta.android.presentation

import com.elta.android.presentation.analytics.encodeUserId
import org.junit.Test

class EncodeIdTest {

    @Test
    fun encodeId_correct() {
        val id = "test"

        val expected = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        val idHash = encodeUserId(id)

        assert(idHash == expected)
    }
}
