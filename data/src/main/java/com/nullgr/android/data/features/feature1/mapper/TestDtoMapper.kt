package com.nullgr.android.data.features.feature1.mapper

import com.nullgr.android.common.mapper.Mapper
import com.nullgr.android.data.features.feature1.dto.TestDto
import com.nullgr.android.domain.features.feature1.model.TestModel
import javax.inject.Inject

class TestDtoMapper @Inject constructor() : Mapper<TestDto, TestModel> {
    override fun mapFromObject(source: TestDto): TestModel =
        TestModel(source.id)
}