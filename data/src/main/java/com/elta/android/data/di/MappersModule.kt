package com.elta.android.data.di

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.mapper.SocialUserDtoMapper
import com.elta.android.data.features.auth.model.SocialUserDto
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.mapper.GlucometerFromCacheMapper
import com.elta.android.data.features.devices.mapper.GlucometerInfoFromCacheMapper
import com.elta.android.data.features.devices.mapper.GlucometerInfoToCacheMapper
import com.elta.android.data.features.devices.mapper.GlucometerInfoToDomainMapper
import com.elta.android.data.features.devices.mapper.GlucometerToCacheMapper
import com.elta.android.data.features.devices.mapper.GlucometerToDomainMapper
import com.elta.android.data.features.devices.mapper.GlucometerToDtoMapper
import com.elta.android.data.features.devices.mapper.ScanResultToGlucometerDtoMapper
import com.elta.android.data.features.diary.events.cache.dto.v1.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.v1.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.mapper.v1.EventFromCacheMapper
import com.elta.android.data.features.diary.events.mapper.v1.EventFromGlucometerMapper
import com.elta.android.data.features.diary.events.mapper.v1.EventToCacheMapper
import com.elta.android.data.features.diary.events.mapper.v1.EventToDomainMapper
import com.elta.android.data.features.diary.events.mapper.v1.EventToDtoMapper
import com.elta.android.data.features.diary.events.mapper.v1.EventToSimpleMapper
import com.elta.android.data.features.diary.events.mapper.v2.EventV2FromCacheMapper
import com.elta.android.data.features.diary.events.mapper.v2.EventV2FromGlucometerMapper
import com.elta.android.data.features.diary.events.mapper.v2.EventV2ToCacheMapper
import com.elta.android.data.features.diary.events.mapper.v2.EventV2ToDtoMapper
import com.elta.android.data.features.diary.events.mapper.v2.EventV2ToSimpleMapper
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.mapper.TagFromCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToDomainMapper
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.data.features.googlefit.mapper.FitnessActivityToActivityTypeMapper
import com.elta.android.data.features.googlefit.mapper.SessionToActivityDtoMapper
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.data.features.reminder.mapper.ReminderFromCacheMapper
import com.elta.android.data.features.reminder.mapper.ReminderToCacheMapper
import com.elta.android.data.features.reminder.mapper.ReminderToDomainMapper
import com.elta.android.data.features.reminder.mapper.ReminderToDtoMapper
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.CoordinatesDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.mapper.CoordinatesToDomainMapper
import com.elta.android.data.features.sale_points.mapper.SalePointFromCacheMapper
import com.elta.android.data.features.sale_points.mapper.SalePointToCacheMapper
import com.elta.android.data.features.sale_points.mapper.SalePointToDomainMapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.mapper.HealthAppFromCacheMapper
import com.elta.android.data.features.user.mapper.HealthAppToCacheMapper
import com.elta.android.data.features.user.mapper.NetworkFromCacheMapper
import com.elta.android.data.features.user.mapper.NetworkToCacheMapper
import com.elta.android.data.features.user.mapper.NetworkToDomainMapper
import com.elta.android.data.features.user.mapper.ProfileFromCacheMapper
import com.elta.android.data.features.user.mapper.ProfileToCacheMapper
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.sale_points.model.Coordinates
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.user.model.SocialNetwork
import com.google.android.gms.fitness.data.Session
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
    abstract fun bindEventV2ToCacheMapper(
        mapper: EventV2ToCacheMapper
    ): Mapper<EventV2Dto, EventV2CachedDto>

    @Binds
    abstract fun bindEventToDomainMapper(
        mapper: EventToDomainMapper
    ): Mapper<EventDto, Event>

    @Binds
    abstract fun bindEventFromCacheMapper(
        mapper: EventFromCacheMapper
    ): Mapper<EventCachedDto, EventDto>

    @Binds
    abstract fun bindEventV2FromCacheMapper(
        mapper: EventV2FromCacheMapper
    ): Mapper<EventV2CachedDto, EventV2Dto>

    @Binds
    abstract fun bindEventFromGlucometerMapper(
        mapper: EventFromGlucometerMapper
    ): Mapper<GlucometerEventDto, Event>

    @Binds
    abstract fun bindEventV2FromGlucometerMapper(
        mapper: EventV2FromGlucometerMapper
    ): Mapper<GlucometerEvent, EventV2>

    @Binds
    abstract fun bindEventToSimpleMapper(
        mapper: EventToSimpleMapper
    ): Mapper<EventDto, SimpleEventDto>

    @Binds
    abstract fun bindEventV2ToSimpleMapper(
        mapper: EventV2ToSimpleMapper
    ): Mapper<EventV2Dto, SimpleEventDto>

    @Binds
    abstract fun bindEventToDtoMapper(
        mapper: EventToDtoMapper
    ): Mapper<Event, EventDto>

    @Binds
    abstract fun bindEventV2ToDtoMapper(
        mapper: EventV2ToDtoMapper
    ): Mapper<EventV2, EventV2Dto>

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
    abstract fun bindProfileFromCacheMapper(
        mapper: ProfileFromCacheMapper
    ): Mapper<ProfileCacheDto, ProfileNetworkResponse>

    @Binds
    abstract fun bindNetworkFromCacheMapper(
        mapper: NetworkFromCacheMapper
    ): Mapper<NetworkCacheDto, SocialNetworkDto>

    @Binds
    abstract fun bindProfileToCacheMapper(
        mapper: ProfileToCacheMapper
    ): Mapper<ProfileNetworkResponse, ProfileCacheDto>

    @Binds
    abstract fun bindNetworkToCacheMapper(
        mapper: NetworkToCacheMapper
    ): Mapper<SocialNetworkDto, NetworkCacheDto>

    @Binds
    abstract fun bindNetworkToDomainMapper(
        mapper: NetworkToDomainMapper
    ): Mapper<SocialNetworkDto, SocialNetwork>

    @Binds
    abstract fun bindGlucometerToDomainMapper(
        mapper: GlucometerToDomainMapper
    ): Mapper<GlucometerDto, Glucometer>

    @Binds
    abstract fun bindGlucometerToDtoMapper(
        mapper: GlucometerToDtoMapper
    ): Mapper<Glucometer, GlucometerDto>

    @Binds
    abstract fun bindGlucometerToCacheMapper(
        mapper: GlucometerToCacheMapper
    ): Mapper<GlucometerDto, GlucometerCachedDto>

    @Binds
    abstract fun bindGlucometerFromCacheMapper(
        mapper: GlucometerFromCacheMapper
    ): Mapper<GlucometerCachedDto, GlucometerDto>

    @Binds
    abstract fun bindGlucometerInfoToDomainMapper(
        mapper: GlucometerInfoToDomainMapper
    ): Mapper<GlucometerInfoDto, GlucometerInfo>

    @Binds
    abstract fun bindGlucometerInfoToCacheMapper(
        mapper: GlucometerInfoToCacheMapper
    ): Mapper<GlucometerInfoDto, GlucometerInfoCachedDto>

    @Binds
    abstract fun bindGlucometerInfoFromCacheMapper(
        mapper: GlucometerInfoFromCacheMapper
    ): Mapper<GlucometerInfoCachedDto, GlucometerInfoDto>

    @Binds
    abstract fun bindScanResultToGlucometerDtoMapper(
        mapper: ScanResultToGlucometerDtoMapper
    ): Mapper<ScanResult, GlucometerDto>

    @Binds
    abstract fun bindReminderFromCacheMapper(
        mapper: ReminderFromCacheMapper
    ): Mapper<ReminderCacheDto, ReminderDto>

    @Binds
    abstract fun bindReminderToCacheMapper(
        mapper: ReminderToCacheMapper
    ): Mapper<ReminderDto, ReminderCacheDto>

    @Binds
    abstract fun bindReminderToDtoMapper(
        mapper: ReminderToDtoMapper
    ): Mapper<Reminder, ReminderDto>

    @Binds
    abstract fun bindReminderToDomainMapper(
        mapper: ReminderToDomainMapper
    ): Mapper<ReminderDto, Reminder>

    @Binds
    abstract fun bindHealthAppFromCacheMapper(
        mapper: HealthAppFromCacheMapper
    ): Mapper<HealthAppCacheDto, HealthAppNetworkEntity>

    @Binds
    abstract fun bindHealthAppToCacheMapper(
        mapper: HealthAppToCacheMapper
    ): Mapper<HealthAppNetworkEntity, HealthAppCacheDto>

    @Binds
    abstract fun bindSessionsToActivityDtoMapper(
        mapper: SessionToActivityDtoMapper
    ): Mapper<Session, ActivityDto>

    @Binds
    abstract fun bindFitnessActivityToActivityTypeMapper(
        mapper: FitnessActivityToActivityTypeMapper
    ): Mapper<String, ActivityType>
}
