package com.elta.mobile.data

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.api.deserializer.EventDtoDeserializer
import com.elta.android.data.features.diary.dto.ActivityTypeDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.elta.android.data.features.diary.dto.MealTagDto
import com.elta.android.data.features.diary.dto.event.ActivityDataDto
import com.elta.android.data.features.diary.dto.event.BreadDataDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.event.GlucoseDataDto
import com.elta.android.data.features.diary.dto.event.InsulinDataDto
import com.elta.android.data.features.diary.dto.event.MedicamentsDataDto
import com.elta.android.data.features.diary.dto.event.WeightDataDto
import com.google.gson.GsonBuilder
import org.junit.Test

class EventDtoDeserializerTest {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(EventDto::class.java, EventDtoDeserializer())
        .create()

    @Test
    fun test_BreadDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"17f8f842-15b0-4975-b40f-3482dca49d18\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"BREAD\",\n" +
            "          \"value\": 5,\n" +
            "          \"kind\": \"Омлет\"\n" +
            "        },\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0947399+00:00\",\n" +
            "        \"tag\": \"13576272-44c9-42a1-9751-d4c9d699e3a1\",\n" +
            "        \"note\": \"Заметка\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"CREATED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as BreadDataDto

        assert(event.id == "17f8f842-15b0-4975-b40f-3482dca49d18")
        assert(data.type == EventTypeDto.BREAD)
        assert(data.value == 5.0)
        assert(data.kind == "Омлет")
        assert(event.additionTime == "2019-02-20T13:43:55.0947399+00:00")
        assert(event.tagId == "13576272-44c9-42a1-9751-d4c9d699e3a1")
        assert(event.note == "Заметка")
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.CREATED)
    }

    @Test
    fun test_InsulinDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"b5a818db-8255-48de-943c-d80619e4f87c\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"INSULIN\",\n" +
            "          \"value\": 5,\n" +
            "          \"insulinType\": \"INTERMEDIATE\"\n" +
            "        },\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0947549+00:00\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"UPDATED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as InsulinDataDto

        assert(event.id == "b5a818db-8255-48de-943c-d80619e4f87c")
        assert(data.type == EventTypeDto.INSULIN)
        assert(data.value == 5.0)
        assert(event.additionTime == "2019-02-20T13:43:55.0947549+00:00")
        assert(event.tagId == null)
        assert(event.note == null)
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.UPDATED)
    }

    @Test
    fun test_MedicamentsDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"7a4903e3-7b09-485d-a627-d02d8236d80e\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"MEDICAMENTS\",\n" +
            "          \"name\": \"Таблетки 3шт.\"\n" +
            "        },\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0948389+00:00\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"DELETED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as MedicamentsDataDto

        assert(event.id == "7a4903e3-7b09-485d-a627-d02d8236d80e")
        assert(data.type == EventTypeDto.MEDICAMENTS)
        assert(data.name == "Таблетки 3шт.")
        assert(event.additionTime == "2019-02-20T13:43:55.0948389+00:00")
        assert(event.tagId == null)
        assert(event.note == null)
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.DELETED)
    }

    @Test
    fun test_ActivityDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"2fd246d1-397c-495e-ac69-36ca6ebb25d2\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"ACTIVITY\",\n" +
            "          \"duration\": \"03:05\",\n" +
            "          \"activityType\": \"FOOTBALL\"\n" +
            "        },\n" +
            "        \"tag\": \"56a2ef84-273c-4843-927d-e0b6048b8dea\",\n" +
            "        \"note\": \"Заметка об активности\",\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0948484+00:00\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"CREATED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as ActivityDataDto

        assert(event.id == "2fd246d1-397c-495e-ac69-36ca6ebb25d2")
        assert(data.type == EventTypeDto.ACTIVITY)
        assert(data.duration == "03:05")
        assert(data.activityType == ActivityTypeDto.FOOTBALL)
        assert(event.additionTime == "2019-02-20T13:43:55.0948484+00:00")
        assert(event.tagId == "56a2ef84-273c-4843-927d-e0b6048b8dea")
        assert(event.note == "Заметка об активности")
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.CREATED)
    }

    @Test
    fun test_WeightDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"74042eef-bd08-4bad-9c0e-4c8daf378be4\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"WEIGHT\",\n" +
            "          \"value\": 79\n" +
            "        },\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0948535+00:00\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"UPDATED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as WeightDataDto

        assert(event.id == "74042eef-bd08-4bad-9c0e-4c8daf378be4")
        assert(data.type == EventTypeDto.WEIGHT)
        assert(data.value == 79.0)
        assert(event.additionTime == "2019-02-20T13:43:55.0948535+00:00")
        assert(event.tagId == null)
        assert(event.note == null)
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.UPDATED)
    }

    @Test
    fun test_GlucoseDataDto_success() {
        val json = "{\n" +
            "        \"id\": \"f7905de7-c94e-4231-966b-8df28109d3a7\",\n" +
            "        \"data\": {\n" +
            "          \"eventType\": \"GLUCOSE\",\n" +
            "          \"value\": 3,\n" +
            "          \"mealTagging\": \"BEFOREMEAL\"\n" +
            "        },\n" +
            "        \"additionalTime\": \"2019-02-20T13:43:55.0948597+00:00\",\n" +
            "        \"timeStamp\": 1549372140,\n" +
            "        \"state\": \"CREATED\"\n" +
            "      }"

        val event = gson.fromJson(json, EventDto::class.java)
        val data = event.data as GlucoseDataDto

        assert(event.id == "f7905de7-c94e-4231-966b-8df28109d3a7")
        assert(data.type == EventTypeDto.GLUCOSE)
        assert(data.value == 3.0)
        assert(data.mealTag == MealTagDto.BEFOREMEAL)
        assert(event.additionTime == "2019-02-20T13:43:55.0948597+00:00")
        assert(event.tagId == null)
        assert(event.note == null)
        assert(event.modificationTime == 1549372140L)
        assert(event.state == StateDto.CREATED)
    }
}