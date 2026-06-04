package com.elta.android.data.features.reports

import com.elta.android.data.core.network.GsonFactory
import com.elta.android.data.features.reports.dto.ReportNetworkRequest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportNetworkRequestSerializationTest {

    @Test
    fun `report token request serializes glucose format as snake case and includes languageTag and locale`() {
        val json = GsonFactory.create().toJson(
            ReportNetworkRequest(
                startDate = "20260501",
                endDate = "20260512",
                glucoseFormat = "PLASMA",
                languageTag = "en",
                locale = "en"
            )
        )

        assertTrue(json.contains("\"glucose_format\":\"PLASMA\""))
        assertTrue(json.contains("\"languageTag\":\"en\""))
        assertTrue(json.contains("\"locale\":\"en\""))
        assertFalse(json.contains("glucoseFormat"))
    }
}
