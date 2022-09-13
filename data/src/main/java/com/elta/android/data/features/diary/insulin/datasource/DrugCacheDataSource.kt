package com.elta.android.data.features.diary.insulin.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.insulin.cache.DrugConditions
import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable
import javax.inject.Inject

class DrugCacheDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<DrugCachedDto, DrugDto>,
    private val cache: Cache<DrugCachedDto>
) : DrugsDataSource {

    override fun getDrugNames(type: InsulinType): Observable<List<DrugDto>> =
        Observable.fromCallable {
            cache.getAll(DrugConditions.ByInsulinType(insulinType = type.name))
        }.map {
            fromCacheMapper.mapFromObjects(it)
        }
}
