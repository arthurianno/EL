package com.elta.android.data.features.diary.events

import com.elta.android.data.core.network.GsonFactory
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.v2.EventDataV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.elta.android.data.features.diary.events.dto.v2.toNetworkInsulinTypeCode
import com.elta.android.data.features.diary.events.dto.v2.toRequestDto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsulinEventRequestSerializationTest {

    private val gson = GsonFactory.create()

    @Test
    fun `reference insulin event request serializes medicament as id only`() {
        val json = gson.toJson(
            insulinEvent(
                insulinMedicament = insulinMedicament(id = 123, isOther = false)
            ).toRequestDto()
        )

        assertTrue(json.contains("\"insulinMedicament\":{\"id\":123}"))
        assertFalse(json.contains("\"isOther\""))
        assertFalse(json.contains("\"deleted\""))
        assertFalse(json.contains("\"insulinType\":{\"code\""))
    }

    @Test
    fun `custom insulin event request serializes name and normalized insulin type`() {
        val json = gson.toJson(
            insulinEvent(
                name = "Custom insulin",
                insulinMedicament = insulinMedicament(
                    id = 51,
                    code = "ULTRASHORT",
                    isOther = true
                )
            ).toRequestDto()
        )

        assertTrue(json.contains("\"insulinMedicament\":{\"name\":\"Custom insulin\",\"insulinType\":\"SHORT\"}"))
        assertFalse(json.contains("\"id\":51"))
        assertFalse(json.contains("ULTRASHORT"))
        assertFalse(json.contains("ULTRA_SHORT"))
    }

    @Test
    fun `legacy ultra short codes normalize to SHORT for network request`() {
        assertTrue("SHORT" == "ULTRASHORT".toNetworkInsulinTypeCode())
        assertTrue("SHORT" == "ULTRA_SHORT".toNetworkInsulinTypeCode())
        assertTrue("PROLONGED" == "PROLONGED".toNetworkInsulinTypeCode())
    }

    private fun insulinEvent(
        name: String? = null,
        insulinMedicament: InsulinMedicamentDto
    ): EventV2Dto =
        EventV2Dto(
            id = "event-id",
            state = StateDto.CREATED,
            additionTime = "2026-05-12T10:00:00Z",
            tagId = null,
            note = null,
            modificationTime = null,
            data = EventDataV2Dto(
                type = EventTypeDto.INSULIN,
                value = 1.0,
                kind = null,
                name = name,
                temperature = null,
                duration = null,
                activityType = null,
                mealTag = null,
                inputType = null,
                insulinMedicament = insulinMedicament,
                medicament = null,
                tabletsNumber = null,
                glucometerSerialNumber = null,
                products = null,
                productsCount = null
            )
        )

    private fun insulinMedicament(
        id: Int,
        code: String = "SHORT",
        isOther: Boolean
    ): InsulinMedicamentDto =
        InsulinMedicamentDto(
            id = id,
            name = "Regular",
            insulinType = InsulinMedicamentDto.MedicamentInsulinTypeDto(
                code = code,
                id = 1,
                name = "Short"
            ),
            deleted = false,
            isOther = isOther
        )
}
