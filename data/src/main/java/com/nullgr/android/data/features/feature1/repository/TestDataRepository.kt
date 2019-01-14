package com.nullgr.android.data.features.feature1.repository

import com.nullgr.android.common.mapper.Mapper
import com.nullgr.android.data.features.feature1.datasource.TestDataSource
import com.nullgr.android.data.features.feature1.dto.TestDto
import com.nullgr.android.domain.features.feature1.model.TestModel
import com.nullgr.android.domain.features.feature1.repository.TestRepository
import io.reactivex.Observable
import javax.inject.Inject

class TestDataRepository @Inject constructor(
    private val source: TestDataSource,
    private val mapper: Mapper<TestDto, TestModel>
) : TestRepository {

    override fun getTestModel(): Observable<TestModel> =
        source.getTestModel().map(mapper::mapFromObject)
}