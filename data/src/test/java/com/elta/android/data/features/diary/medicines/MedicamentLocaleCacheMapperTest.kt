package com.elta.android.data.features.diary.medicines

import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.elta.android.data.features.diary.medicines.mapper.medicamentCacheId
import com.elta.android.data.features.diary.medicines.mapper.toDB
import com.elta.android.data.features.diary.medicines.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MedicamentLocaleCacheMapperTest {

    @Test
    fun `cache id is scoped by country and language`() {
        val ruRuId = medicamentCacheId(medicamentId = 123L, countryCode = "RU", languageTag = "ru")
        val ruEnId = medicamentCacheId(medicamentId = 123L, countryCode = "RU", languageTag = "en")
        val inRuId = medicamentCacheId(medicamentId = 123L, countryCode = "IN", languageTag = "ru")

        assertNotEquals(ruRuId, ruEnId)
        assertNotEquals(ruRuId, inRuId)
        assertNotEquals(ruEnId, inRuId)
    }

    @Test
    fun `network medicament keeps backend id separately from locale cache id`() {
        val network = MedicamentNetworkResponse(
            id = 123L,
            name = "Aspirin",
            deleted = false,
            other = false,
            touchedAt = 456L
        )

        val db = network.toDB(countryCode = "in", languageTag = "EN")
        val domain = listOf(db).toDomain().single()

        assertEquals(123L, db.medicamentId)
        assertEquals("IN", db.countryCode)
        assertEquals("en", db.languageTag)
        assertNotEquals(123L, db.id)
        assertEquals(123L, domain.id)
        assertEquals("Aspirin", domain.name)
    }
}
