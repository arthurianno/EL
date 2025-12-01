package com.elta.android.data.features.googlefit.builder

import androidx.health.connect.client.records.BloodGlucoseRecord
import com.elta.android.common.utils.toZonedDateTime
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.State
import java.util.UUID
import javax.inject.Inject

/**
 * Builder for converting Health Connect blood glucose records to EventV2
 */
class GlucoseEventsBuilder @Inject constructor() {

    /**
     * Builds glucose events from Health Connect blood glucose records
     *
     * @param glucoseRecords List of blood glucose records from Health Connect
     * @param profileEmail User's email for generating unique event IDs
     * @return List of EventV2 with type GLUCOSE
     */
    fun buildGlucoseEvents(
        glucoseRecords: List<BloodGlucoseRecord>,
        profileEmail: String
    ): List<EventV2> {
        return glucoseRecords.map { record ->
            mapGlucoseToEvent(record, profileEmail)
        }
    }

    private fun mapGlucoseToEvent(record: BloodGlucoseRecord, email: String): EventV2 {
        // Generate unique ID based on record metadata and user email
        val uniqueId = UUID.nameUUIDFromBytes(
            "${record.time.toEpochMilli()}_${record.level.inMillimolesPerLiter}_$email".toByteArray()
        ).toString()

        // Build note with meal type info if available
        val note = buildNoteFromRecord(record)

        return EventV2(
            id = uniqueId,
            additionTime = record.time.toEpochMilli().toZonedDateTime(),
            tagId = null,
            tag = null,
            note = note,
            modificationTime = null,
            value = record.level.inMillimolesPerLiter, // Glucose level in mmol/L
            name = null,
            kind = null,
            temperature = null,
            duration = null,
            activityType = null,
            insulinMedicament = null,
            medicament = null,
            tabletsNumber = null,
            type = EventType.Glucose(GlucoseInputType.GOOGLE_FIT), // GOOGLE_FIT because data comes from Health Connect / Google Fit
            mealTag = null,
            state = State.CREATED,
            glucometerSerialNumber = "Health Connect", // Mark source
            dishes = emptyList(),
            glucoseInputType = GlucoseInputType.GOOGLE_FIT
        )
    }

    private fun buildNoteFromRecord(record: BloodGlucoseRecord): String {
        val parts = mutableListOf<String>()

        // Add source info
        parts.add("Health Connect")

        // Add specimen source if available
        val specimenSource = when (record.specimenSource) {
            BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD -> "Капиллярная кровь"
            BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID -> "Интерстициальная жидкость"
            BloodGlucoseRecord.SPECIMEN_SOURCE_PLASMA -> "Плазма"
            BloodGlucoseRecord.SPECIMEN_SOURCE_SERUM -> "Сыворотка"
            BloodGlucoseRecord.SPECIMEN_SOURCE_TEARS -> "Слёзы"
            BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD -> "Цельная кровь"
            else -> null
        }
        specimenSource?.let { parts.add(it) }

        // Add meal type if available
        val mealType = when (record.mealType) {
            BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL -> "До еды"
            BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL -> "После еды"
            BloodGlucoseRecord.RELATION_TO_MEAL_FASTING -> "Натощак"
            else -> null
        }
        mealType?.let { parts.add(it) }

        return parts.joinToString(" • ")
    }
}

