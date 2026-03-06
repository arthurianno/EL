package com.elta.android.data.features.devices.glucometer.service.firmware

import org.junit.Assert.assertEquals
import org.junit.Test

class BootChunkingTest {

    @Test
    fun `splitChunkSizes does not create empty tail chunk when divisible`() {
        assertEquals(listOf(236), splitChunkSizes(totalSize = 236, chunkSize = 236))
        assertEquals(listOf(236, 236), splitChunkSizes(totalSize = 472, chunkSize = 236))
    }

    @Test
    fun `splitChunkSizes creates tail chunk for remainder`() {
        assertEquals(listOf(236, 236, 8), splitChunkSizes(totalSize = 480, chunkSize = 236))
    }

    @Test
    fun `splitChunkSizes handles empty payload`() {
        assertEquals(emptyList<Int>(), splitChunkSizes(totalSize = 0, chunkSize = 236))
    }
}
