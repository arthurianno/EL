package com.elta.android.data.features.diary.insulin.datasource.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.insulin.cache.DrugConditions
import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable
import javax.inject.Inject

class DrugCacheDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<DrugCachedDto, DrugDto>,
    private val cache: Cache<DrugCachedDto>
) : DrugsCacheSource {

    override fun getDrugNames(type: InsulinType): Observable<List<DrugDto>> =
        Observable.fromCallable {
            cache.getAll(DrugConditions.ByInsulinType(insulinType = type.name.lowercase()))
        }.map {
            fromCacheMapper.mapFromObjects(it)
        }

    override fun getAll(): Observable<List<DrugDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }
            .map { fromCacheMapper.mapFromObjects(it) }
            .map { drugs -> drugs
                    .filter { drug -> drug.name != OTHER }
                    .sortedBy { drug -> drug.name }
            }

    override fun clearDrugs(type: InsulinType?) {
        type?.let { insulinType ->
            val drugIds = cache.getAll(DrugConditions.ByInsulinType(insulinType = insulinType.name))
                .map { it.id }
            cache.delete(CommonConditions.ByIds(drugIds))
        }
            ?: cache.delete(CommonConditions.All)
    }

    override fun saveDrugs(drugs: List<DrugCachedDto>) {
        cache.update(drugs)
    }
}

private const val OTHER = "Другое"
