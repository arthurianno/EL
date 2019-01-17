package com.elta.android.data.di

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.feature1.dto.TestDto
import com.elta.android.data.features.feature1.mapper.TestDtoMapper
import com.elta.android.domain.features.feature1.model.TestModel
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class MappersModule {

    @Binds
    @Singleton
    abstract fun bindTestDtoMapper(mapper: TestDtoMapper): Mapper<TestDto, TestModel>
}