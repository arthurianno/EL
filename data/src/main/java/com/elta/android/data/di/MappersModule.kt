package com.elta.android.data.di

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.mapper.SocialUserDtoMapper
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.mapper.EventFromCacheMapper
import com.elta.android.data.features.diary.events.mapper.EventToCacheMapper
import com.elta.android.data.features.diary.events.mapper.EventToDomainMapper
import com.elta.android.data.features.diary.events.mapper.EventToDtoMapper
import com.elta.android.data.features.diary.events.mapper.EventToSimpleMapper
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.mapper.TagFromCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToDomainMapper
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.CoordinatesDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.mapper.CoordinatesToDomainMapper
import com.elta.android.data.features.sale_points.mapper.SalePointFromCacheMapper
import com.elta.android.data.features.sale_points.mapper.SalePointToCacheMapper
import com.elta.android.data.features.sale_points.mapper.SalePointToDomainMapper
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.mapper.SettingsFromCacheMapper
import com.elta.android.data.features.user.mapper.SettingsToCacheMapper
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.sale_points.model.Coordinates
import com.elta.android.domain.features.sale_points.model.SalePoint
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

    @Binds
    abstract fun bindSalePointToDomainMapper(
        mapper: SalePointToDomainMapper
    ): Mapper<SalePointDto, SalePoint>

    @Binds
    abstract fun bindCoordinatesToDomainMapper(
        mapper: CoordinatesToDomainMapper
    ): Mapper<CoordinatesDto, Coordinates>

    @Binds
    abstract fun bindEventToCacheMapper(
        mapper: EventToCacheMapper
    ): Mapper<EventDto, EventCachedDto>

    @Binds
    abstract fun bindEventToDomainMapper(
        mapper: EventToDomainMapper
    ): Mapper<EventDto, Event>

    @Binds
    abstract fun bindEventFromCacheMapper(
        mapper: EventFromCacheMapper
    ): Mapper<EventCachedDto, EventDto>

    @Binds
    abstract fun bindEventToSimpleMapper(
        mapper: EventToSimpleMapper
    ): Mapper<EventDto, SimpleEventDto>

    @Binds
    abstract fun bindEventToDtoMapper(
        mapper: EventToDtoMapper
    ): Mapper<Event, EventDto>

    @Binds
    abstract fun bindTagToCacheMapper(
        mapper: TagToCacheMapper
    ): Mapper<TagDto, TagCachedDto>

    @Binds
    abstract fun bindTagToDomainMapper(
        mapper: TagToDomainMapper
    ): Mapper<TagDto, Tag>

    @Binds
    abstract fun bindTagFromCacheMapper(
        mapper: TagFromCacheMapper
    ): Mapper<TagCachedDto, TagDto>

    @Binds
    abstract fun bindSettingsFromCacheMapper(
        mapper: SettingsFromCacheMapper
    ): Mapper<SettingsCacheDto, ProfileDto>

    @Binds
    abstract fun bindSettingsToCacheMapper(
        mapper: SettingsToCacheMapper
    ): Mapper<ProfileDto, SettingsCacheDto>
}