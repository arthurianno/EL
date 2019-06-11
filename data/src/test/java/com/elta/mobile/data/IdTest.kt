package com.elta.mobile.data

import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventIdGenerator
import org.junit.Test

class IdTest {

    @Test
    fun generate_ids_the_same() {
        val userId = "example@mail.com"
        val mac = "ea:dd:2d:df:45:54"
        val date = "190527162626"
        val expected = "F0EE4B70-889D-385F-A40A-8B87F029BFC0".toLowerCase()

        val generator = DefaultGlucometerEventIdGenerator()
        val id = generator.generate(userId, mac, date)

        assert(id == expected)
    }
}