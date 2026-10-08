package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SendMessageUseCaseTest {
    @Test
    fun `file send waits for successful upload callback`() = runBlocking {
        flowOf(WebimMessageSendStatus.Sending, WebimMessageSendStatus.Sent).awaitSent()
    }

    @Test
    fun `file send propagates upload failure`() = runBlocking {
        try {
            flowOf(
                WebimMessageSendStatus.Sending,
                WebimMessageSendStatus.Error("Upload failed")
            ).awaitSent()
            fail("Expected upload failure")
        } catch (error: IOException) {
            assertEquals("Upload failed", error.message)
        }
    }
}
