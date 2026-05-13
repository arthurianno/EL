package com.elta.android.data.features.diary.medicines

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.network.CountryCodeProvider
import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.diary.medicines.cache.conditions.InsulinMedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import com.elta.android.data.features.diary.medicines.datasource.cache.InsulinMedicamentCacheDataSource
import com.elta.android.data.features.diary.medicines.datasource.cache.InsulinMedicamentCacheSource
import com.elta.android.data.features.diary.medicines.datasource.remote.InsulinMedicamentRemoteDataSource
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.elta.android.data.features.diary.medicines.mapper.normalizedCountryCode
import com.elta.android.data.features.diary.medicines.repository.InsulinMedicamentDataRepository
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsulinCountryCodeFollowUpTest {

    @Test
    fun `remote datasource passes countryCode from resolver to api`() {
        val api = CapturingMedicinesApi()
        val countryCodeProvider = MutableCountryCodeProvider("IN")
        val remoteDataSource = InsulinMedicamentRemoteDataSource(api, countryCodeProvider)

        remoteDataSource.getInsulinMedicines().test().assertComplete()

        assertEquals("IN", api.countryCodes.single())
    }

    @Test
    fun `repository clears insulin cache and syncs new list when country changes`() {
        val api = CapturingMedicinesApi()
        val countryCodeProvider = MutableCountryCodeProvider("RU")
        val remoteDataSource = InsulinMedicamentRemoteDataSource(api, countryCodeProvider)
        val cacheSource = InMemoryInsulinCacheSource()
        val repository = InsulinMedicamentDataRepository(
            insulinMedicamentRemoteDataSource = remoteDataSource,
            insulinMedicamentCacheSource = cacheSource,
            countryCodeProvider = countryCodeProvider
        )

        repository.getInsulinTypes()
            .test()
            .assertValue { types -> types.single().name == "Short RU" }
        val clearCountAfterInitialSync = cacheSource.clearCount

        countryCodeProvider.countryCode = "IN"

        repository.getInsulinTypes()
            .test()
            .assertValue { types -> types.single().name == "Short IN" }

        assertEquals(listOf("RU", "IN"), api.countryCodes)
        assertTrue(cacheSource.clearCount > clearCountAfterInitialSync)
    }

    @Test
    fun `cache reads insulin medicaments in backend order`() {
        val insulinType = insulinType()
        val cacheDataSource = InsulinMedicamentCacheDataSource(
            insulinMedicamentCache = InMemoryInsulinMedicamentCache(
                listOf(
                    insulinMedicament(id = 12, name = "Other", sortOrder = 2, isOther = true),
                    insulinMedicament(id = 11, name = "Alpha", sortOrder = 1),
                    insulinMedicament(id = 10, name = "Zeta", sortOrder = 0)
                )
            ),
            insulinTypeCache = InMemoryInsulinTypeCache(listOf(insulinType)),
            statisticCache = InMemoryInsulinStatisticCache(emptyList())
        )

        val actual = cacheDataSource.getInsulinMedicaments(insulinType, "IN").blockingFirst()

        assertEquals(listOf("Zeta", "Alpha", "Other"), actual.map { it.name })
    }

    @Test
    fun `cache keeps isOther insulin medicament last`() {
        val insulinType = insulinType()
        val cacheDataSource = InsulinMedicamentCacheDataSource(
            insulinMedicamentCache = InMemoryInsulinMedicamentCache(
                listOf(
                    insulinMedicament(id = 50, name = "Other", sortOrder = 0, isOther = true),
                    insulinMedicament(id = 10, name = "Regular", sortOrder = 1),
                    insulinMedicament(id = 11, name = "NPH", sortOrder = 2)
                )
            ),
            insulinTypeCache = InMemoryInsulinTypeCache(listOf(insulinType)),
            statisticCache = InMemoryInsulinStatisticCache(emptyList())
        )

        val actual = cacheDataSource.getInsulinMedicaments(insulinType, "IN").blockingFirst()

        assertEquals(listOf("Regular", "NPH", "Other"), actual.map { it.name })
        assertTrue(actual.last().isOther)
    }

    @Test
    fun `repository keeps cached insulin medicaments in backend order`() {
        val insulinType = insulinType()
        val countryCodeProvider = MutableCountryCodeProvider("IN")
        val cacheDataSource = InsulinMedicamentCacheDataSource(
            insulinMedicamentCache = InMemoryInsulinMedicamentCache(
                listOf(
                    insulinMedicament(id = 12, name = "Other", sortOrder = 2, isOther = true),
                    insulinMedicament(id = 11, name = "Alpha", sortOrder = 1),
                    insulinMedicament(id = 10, name = "Zeta", sortOrder = 0)
                )
            ),
            insulinTypeCache = InMemoryInsulinTypeCache(listOf(insulinType)),
            statisticCache = InMemoryInsulinStatisticCache(
                listOf(
                    InsulinStatisticDbEntity(
                        bolusInsulinTypes = listOf("SHORT"),
                        basalInsulinTypes = emptyList(),
                        countryCode = "IN"
                    )
                )
            )
        )
        val repository = InsulinMedicamentDataRepository(
            insulinMedicamentRemoteDataSource = InsulinMedicamentRemoteDataSource(
                api = CapturingMedicinesApi(),
                countryCodeProvider = countryCodeProvider
            ),
            insulinMedicamentCacheSource = cacheDataSource,
            countryCodeProvider = countryCodeProvider
        )

        val actual = repository.getInsulinMedicaments(
            MedicamentInsulinType(
                id = insulinType.id.toInt(),
                code = insulinType.code,
                name = insulinType.name
            )
        ).blockingFirst()

        assertEquals(listOf("Zeta", "Alpha", "Other"), actual.map { it.name })
        assertTrue(actual.last().isOther)
    }

    private class CapturingMedicinesApi : MedicinesApi {

        val countryCodes = mutableListOf<String?>()

        override fun getMedicaments(
            touchedAfter: Long?,
            languageTag: String,
            countryCode: String
        ): Single<List<MedicamentNetworkResponse>> = Single.just(emptyList())

        override fun getInsulinMedicines(
            languageTag: String?,
            countryCode: String?
        ): Single<InsulinMedicamentsNetworkResponse> {
            countryCodes += countryCode
            return Single.just(insulinResponse(countryCode.orEmpty()))
        }
    }

    private class MutableCountryCodeProvider(
        var countryCode: String
    ) : CountryCodeProvider {
        override fun countryCode(): String = countryCode
    }

    private class InMemoryInsulinCacheSource : InsulinMedicamentCacheSource {

        var clearCount = 0
            private set

        private var medicaments: List<InsulinMedicamentDbEntity> = emptyList()
        private var insulinTypes: List<InsulinTypeDbEntity> = emptyList()
        private var statistic: InsulinStatisticDbEntity? = null

        override fun saveInsulinMedicaments(medicines: List<InsulinMedicamentDbEntity>): Completable =
            Completable.fromAction {
                medicaments = medicines
            }

        override fun getInsulinMedicaments(countryCode: String): Observable<List<InsulinMedicamentDbEntity>> =
            Observable.just(medicaments.filterByCountry(countryCode))

        override fun getInsulinMedicaments(
            type: InsulinTypeDbEntity,
            countryCode: String
        ): Observable<List<InsulinMedicamentDbEntity>> =
            Observable.just(
                medicaments
                    .filter { it.insulinType.code == type.code }
                    .filterByCountry(countryCode)
            )

        override fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable =
            Completable.fromAction {
                this.insulinTypes = insulinTypes
            }

        override fun getInsulinTypes(countryCode: String): Observable<List<InsulinTypeDbEntity>> =
            Observable.just(
                insulinTypes
                    .filter { it.matchesCountry(countryCode) }
                    .sortedBy { it.sortOrder }
            )

        override fun saveInsulinStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable =
            Completable.fromAction {
                statistic = insulinStatisticDbEntity
            }

        override fun getInsulinStatisticType(countryCode: String): Observable<InsulinStatisticDbEntity> =
            Observable.just(
                statistic
                    ?.takeIf { it.matchesCountry(countryCode) }
                    ?: InsulinStatisticDbEntity.empty(countryCode.normalizedCountryCode())
            )

        override fun hasInsulinCache(countryCode: String): Single<Boolean> =
            Single.fromCallable {
                statistic?.matchesCountry(countryCode) == true &&
                    medicaments.any { it.matchesCountry(countryCode) }
            }

        override fun clearInsulinCache(): Completable =
            Completable.fromAction {
                clearCount++
                medicaments = emptyList()
                insulinTypes = emptyList()
                statistic = null
            }
    }

    private class InMemoryInsulinMedicamentCache(
        initialItems: List<InsulinMedicamentDbEntity>
    ) : Cache<InsulinMedicamentDbEntity> {

        private var items: List<InsulinMedicamentDbEntity> = initialItems

        override fun add(objects: List<InsulinMedicamentDbEntity>) {
            items = items + objects
        }

        override fun update(objects: List<InsulinMedicamentDbEntity>) {
            val ids = objects.map { it.id }.toSet()
            items = items.filterNot { it.id in ids } + objects
        }

        override fun delete(condition: Condition) {
            if (condition is CommonConditions.All) {
                items = emptyList()
            }
        }

        override fun get(condition: Condition): InsulinMedicamentDbEntity? = getAll(condition).firstOrNull()

        override fun getAll(condition: Condition): List<InsulinMedicamentDbEntity> =
            when (condition) {
                is InsulinMedicamentConditions.ByInsulinType ->
                    items.filter { it.insulinType.code == condition.insulinType.code }

                else -> items
            }

        override fun contains(condition: Condition): Boolean = get(condition) != null

        override fun count(condition: Condition): Long = getAll(condition).size.toLong()
    }

    private class InMemoryInsulinTypeCache(
        private var items: List<InsulinTypeDbEntity>
    ) : Cache<InsulinTypeDbEntity> {

        override fun add(objects: List<InsulinTypeDbEntity>) {
            items = items + objects
        }

        override fun update(objects: List<InsulinTypeDbEntity>) {
            val ids = objects.map { it.id }.toSet()
            items = items.filterNot { it.id in ids } + objects
        }

        override fun delete(condition: Condition) {
            if (condition is CommonConditions.All) {
                items = emptyList()
            }
        }

        override fun get(condition: Condition): InsulinTypeDbEntity? = items.firstOrNull()

        override fun getAll(condition: Condition): List<InsulinTypeDbEntity> = items

        override fun contains(condition: Condition): Boolean = items.isNotEmpty()

        override fun count(condition: Condition): Long = items.size.toLong()
    }

    private class InMemoryInsulinStatisticCache(
        private var items: List<InsulinStatisticDbEntity>
    ) : Cache<InsulinStatisticDbEntity> {

        override fun add(objects: List<InsulinStatisticDbEntity>) {
            items = items + objects
        }

        override fun update(objects: List<InsulinStatisticDbEntity>) {
            items = objects
        }

        override fun delete(condition: Condition) {
            if (condition is CommonConditions.All) {
                items = emptyList()
            }
        }

        override fun get(condition: Condition): InsulinStatisticDbEntity? = items.firstOrNull()

        override fun getAll(condition: Condition): List<InsulinStatisticDbEntity> = items

        override fun contains(condition: Condition): Boolean = items.isNotEmpty()

        override fun count(condition: Condition): Long = items.size.toLong()
    }

    private companion object {
        fun insulinResponse(countryCode: String): InsulinMedicamentsNetworkResponse {
            val normalizedCountryCode = countryCode.ifBlank { "RU" }.normalizedCountryCode()
            val insulinType = InsulinMedicamentsNetworkResponse.InsulinType(
                code = "SHORT",
                id = 1,
                name = "Short $normalizedCountryCode"
            )
            return InsulinMedicamentsNetworkResponse(
                insulinMedicamentsByType = mapOf(
                    "SHORT" to listOf(
                        InsulinMedicamentsNetworkResponse.Item(
                            id = if (normalizedCountryCode == "IN") 101 else 51,
                            insulinType = insulinType,
                            name = "Regular $normalizedCountryCode",
                            deleted = false,
                            isOther = false
                        )
                    )
                ),
                bolusInsulinTypes = listOf("SHORT"),
                basalInsulinTypes = emptyList()
            )
        }

        fun insulinType(): InsulinTypeDbEntity =
            InsulinTypeDbEntity(
                id = 1,
                code = "SHORT",
                name = "Short",
                countryCode = "IN",
                sortOrder = 0
            )

        fun insulinMedicament(
            id: Long,
            name: String,
            sortOrder: Int,
            isOther: Boolean = false
        ): InsulinMedicamentDbEntity =
            InsulinMedicamentDbEntity(
                id = id,
                name = name,
                insulinType = insulinType(),
                deleted = false,
                isOther = isOther,
                countryCode = "IN",
                sortOrder = sortOrder
            )
    }
}

private fun List<InsulinMedicamentDbEntity>.filterByCountry(countryCode: String): List<InsulinMedicamentDbEntity> =
    filter { it.matchesCountry(countryCode) }

private fun InsulinMedicamentDbEntity.matchesCountry(countryCode: String): Boolean =
    this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()

private fun InsulinTypeDbEntity.matchesCountry(countryCode: String): Boolean =
    this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()

private fun InsulinStatisticDbEntity.matchesCountry(countryCode: String): Boolean =
    this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()
