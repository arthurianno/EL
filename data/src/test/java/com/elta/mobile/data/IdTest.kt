package com.elta.mobile.data

import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventIdGenerator
import org.junit.Test

class IdTest {

    @Test
    fun generate_ids_the_same() {
        val mac = "AC:DA:48:01:02:05"
        val date = "1812151936"
        val expected = "BEE03470-8899-3805-9CA4-41CBF6A1ECA5".toLowerCase()

        val generator = DefaultGlucometerEventIdGenerator()
        val id = generator.generate(mac, date)

        assert(id == expected)
    }
}