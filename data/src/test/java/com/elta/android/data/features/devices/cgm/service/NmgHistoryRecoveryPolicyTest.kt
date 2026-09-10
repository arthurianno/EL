package com.elta.android.data.features.devices.cgm.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NmgHistoryRecoveryPolicyTest {
    @Test
    fun `does not recover when current advertisement fills the next history bucket`() {
        assertFalse(NmgHistoryRecoveryPolicy.hasMissingHistoryBucket(485, 600))
    }

    @Test
    fun `recovers when a complete history bucket is missing`() {
        assertTrue(NmgHistoryRecoveryPolicy.hasMissingHistoryBucket(485, 720))
    }

    @Test
    fun `does not recover for advertisements in the current history bucket`() {
        assertFalse(NmgHistoryRecoveryPolicy.hasMissingHistoryBucket(485, 595))
    }
}
