package com.elta.android.data.features.diary.medicines

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import com.elta.android.data.features.diary.medicines.datasource.cache.MedicamentCacheDataSource
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.elta.android.data.features.diary.medicines.mapper.toDB
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicamentCacheDataSourceTest {

    @Test
    fun `sync keeps lastUsed but refreshes localized network name`() {
        val oldRecent = MedicamentNetworkResponse(
            id = 123L,
            name = "Old name",
            deleted = false,
            other = false,
            touchedAt = 1L
        ).toDB(countryCode = "RU", languageTag = "ru").copy(lastUsed = 100L)
        val freshNetwork = MedicamentNetworkResponse(
            id = 123L,
            name = "New name",
            deleted = false,
            other = false,
            touchedAt = 2L
        ).toDB(countryCode = "RU", languageTag = "ru")
        val cache = InMemoryMedicamentCache(listOf(oldRecent))

        MedicamentCacheDataSource(cache)
            .saveMedicaments(listOf(freshNetwork), countryCode = "RU", languageTag = "ru")
            .blockingAwait()

        val saved = cache.items.single()
        assertEquals("New name", saved.name)
        assertEquals(100L, saved.lastUsed)
        assertEquals(2L, saved.touchedAt)
    }
}

private class InMemoryMedicamentCache(initial: List<MedicamentDBEntity>) : Cache<MedicamentDBEntity> {

    var items: List<MedicamentDBEntity> = initial
        private set

    override fun add(objects: List<MedicamentDBEntity>) {
        items = items + objects
    }

    override fun update(objects: List<MedicamentDBEntity>) {
        val ids = objects.map { it.id }.toSet()
        items = items.filterNot { it.id in ids } + objects
    }

    override fun delete(condition: Condition) {
        if (condition is CommonConditions.ByIds) {
            items = items.filterNot { it.id in condition.ids }
        }
    }

    override fun get(condition: Condition): MedicamentDBEntity? = null

    override fun getAll(condition: Condition): List<MedicamentDBEntity> = items

    override fun contains(condition: Condition): Boolean = false

    override fun count(condition: Condition): Long = items.size.toLong()
}
