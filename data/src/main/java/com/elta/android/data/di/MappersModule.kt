package com.elta.android.data.di

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.mapper.SocialUserDtoMapper
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.mapper.SalePointFromCacheMapper
import com.elta.android.data.features.sale_points.mapper.SalePointToCacheMapper
import com.elta.android.domain.features.auth.model.SocialUser
import dagger.Binds
import dagger.Module

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class MappersModule {

    @Binds
    abstract fun bindSocialUserDtoMapper(
        mapper: SocialUserDtoMapper
    ): Mapper<SocialUserDto, SocialUser>

    @Binds
    abstract fun bindSalePointToCacheMapper(
        mapper: SalePointToCacheMapper
    ): Mapper<SalePointDto, SalePointCacheDto>

    @Binds
    abstract fun bindSalePointFromCacheMapper(
        mapper: SalePointFromCacheMapper
    ): Mapper<SalePointCacheDto, SalePointDto>
}