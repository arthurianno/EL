package com.elta.android.data.di

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.mapper.SocialUserDtoMapper
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
}