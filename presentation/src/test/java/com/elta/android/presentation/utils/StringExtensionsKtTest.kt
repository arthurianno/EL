package com.elta.android.presentation.utils

import org.junit.Test
import kotlin.test.assertIs

class StringExtensionsKtTest {

    private val serialPrefix = "D2300"
    private val serials = (0..999999).map { number: Int ->
        "$serialPrefix${
            number.toString()
                .run {
                    "0".repeat(6 - this.length).plus(number)
                }
        }"
    }

    @Test
    fun pinCodesLength_Test() {
        serials.map {
            assert(it.extractPinCode().length == 3)
        }
    }

    @Test
    fun pinCodeIntConvert_Test() {
        serials.map {
            assertIs<Int>(it.extractPinCode().toInt())
        }
    }

    @Test
    fun piCodeEquals_Test() {
        assert("D2211000230".extractPinCode() == "621")
    }
}
