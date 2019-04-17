package com.elta.mobile.data

import com.elta.android.data.features.devices.glucometer.toDfuAddress
import org.junit.Test

class MacAddressUtilsTest {

    @Test
    fun toDfuAddress_00_01() {
        val original = "00:11:22:33:FF:00"
        val expected = "00:11:22:33:FF:01"
        assert(expected.equals(original.toDfuAddress(), true))
    }

    @Test
    fun toDfuAddress_09_0A() {
        val original = "00:11:22:33:FF:09"
        val expected = "00:11:22:33:FF:0A"
        assert(expected.equals(original.toDfuAddress(), true))
    }

    @Test
    fun toDfuAddress_0F_10() {
        val original = "00:11:22:33:FF:0F"
        val expected = "00:11:22:33:FF:10"
        assert(expected.equals(original.toDfuAddress(), true))
    }

    @Test
    fun toDfuAddress_FF_00() {
        val original = "00:11:22:33:FF:FF"
        val expected = "00:11:22:33:FF:00"
        assert(expected.equals(original.toDfuAddress(), true))
    }
}