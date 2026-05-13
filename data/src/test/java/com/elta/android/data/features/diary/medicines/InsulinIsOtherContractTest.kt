package com.elta.android.data.features.diary.medicines

import com.elta.android.data.core.network.GsonFactory
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.elta.android.data.features.diary.events.mapper.toDomain
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.mapper.toDb
import com.elta.android.data.features.diary.medicines.mapper.toDomainMedicines
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsulinIsOtherContractTest {

    private val gson = GsonFactory.create()

    @Test
    fun `insulin medicaments response deserializes isOther true`() {
        val response = gson.fromJson(
            """
            {
              "insulinMedicamentsByType": {
                "SHORT": [
                  {
                    "id": 51,
                    "name": "Other",
                    "deleted": false,
                    "isOther": true,
                    "insulinType": { "code": "SHORT", "id": 1, "name": "Short" }
                  }
                ]
              },
              "bolusInsulinTypes": ["SHORT"],
              "basalInsulinTypes": []
            }
            """.trimIndent(),
            InsulinMedicamentsNetworkResponse::class.java
        )

        val dbMedicaments = response.toDb().first
        val domainMedicaments = dbMedicaments.toDomainMedicines()

        assertTrue(response.insulinMedicamentsByType.getValue("SHORT").single().isOther)
        assertTrue(dbMedicaments.single().isOther)
        assertTrue(domainMedicaments.single().isOther)
    }

    @Test
    fun `missing isOther in insulin medicaments response defaults to false`() {
        val response = gson.fromJson(
            """
            {
              "insulinMedicamentsByType": {
                "SHORT": [
                  {
                    "id": 10,
                    "name": "Regular",
                    "deleted": false,
                    "insulinType": { "code": "SHORT", "id": 1, "name": "Short" }
                  }
                ]
              },
              "bolusInsulinTypes": ["SHORT"],
              "basalInsulinTypes": []
            }
            """.trimIndent(),
            InsulinMedicamentsNetworkResponse::class.java
        )

        assertFalse(response.insulinMedicamentsByType.getValue("SHORT").single().isOther)
    }

    @Test
    fun `event insulin response defaults missing isOther to false`() {
        val response = gson.fromJson(
            """
            {
              "id": 10,
              "name": "Regular",
              "deleted": false,
              "insulinType": { "code": "SHORT", "id": 1, "name": "Short" }
            }
            """.trimIndent(),
            InsulinMedicamentDto::class.java
        )

        assertFalse(response.isOther)
        assertFalse(response.toDomain().isOther)
    }
}
