package com.elta.android.data.di

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.mapper.SocialUserDtoMapper
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
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.mapper.EventFromCacheMapper
import com.elta.android.data.features.diary.events.mapper.EventFromGlucometerMapper
import com.elta.android.data.features.diary.events.mapper.EventToCacheMapper
import com.elta.android.data.features.diary.events.mapper.EventToDomainMapper
import com.elta.android.data.features.diary.events.mapper.EventToDtoMapper
import com.elta.android.data.features.diary.events.mapper.EventToSimpleMapper
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.tags.dto.TagDto
import com.elta.android.data.features.diary.tags.mapper.TagFromCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToCacheMapper
import com.elta.android.data.features.diary.tags.mapper.TagToDomainMapper
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.data.features.firmware.mapper.FirmwareFileToDomainMapper
import com.elta.android.data.features.firmware.mapper.FirmwareToDomainMapper
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.observers.mapper.ObserverFromCacheMapper
import com.elta.android.data.features.observers.mapper.ObserverToCacheMapper
import com.elta.android.data.features.observers.mapper.ObserverToDomainMapper
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
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.mapper.NetworkFromCacheMapper
import com.elta.android.data.features.user.mapper.NetworkToCacheMapper
import com.elta.android.data.features.user.mapper.NetworkToDomainMapper
import com.elta.android.data.features.user.mapper.ProfileFromCacheMapper
import com.elta.android.data.features.user.mapper.ProfileToCacheMapper
import com.elta.android.data.features.user.mapper.ProfileToDomainMapper
import com.elta.android.data.features.user.mapper.ProfileToDtoMapper
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import com.elta.android.data.features.userinfo.mapper.UserInfoFromCacheMapper
import com.elta.android.data.features.userinfo.mapper.UserInfoToCacheMapper
import com.elta.android.data.features.userinfo.mapper.UserInfoToDomainMapper
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.sale_points.model.Coordinates
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetwork
import com.elta.android.domain.features.userinfo.model.UserInfo
import dagger.Binds
import dagger.Module
import no.nordicsemi.android.support.v18.scanner.ScanResult

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
    abstract fun bindEventFromGlucometerMapper(
        mapper: EventFromGlucometerMapper
    ): Mapper<GlucometerEventDto, Event>

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
    abstract fun bindObserverToCacheMapper(
        mapper: ObserverToCacheMapper
    ): Mapper<ObserverDto, ObserverCacheDto>

    @Binds
    abstract fun bindObserverFromCacheMapper(
        mapper: ObserverFromCacheMapper
    ): Mapper<ObserverCacheDto, ObserverDto>

    @Binds
    abstract fun bindObserverToDomainMapper(
        mapper: ObserverToDomainMapper
    ): Mapper<ObserverDto, Observer>

    @Binds
    abstract fun bindProfileFromCacheMapper(
        mapper: ProfileFromCacheMapper
    ): Mapper<ProfileCacheDto, ProfileDto>

    @Binds
    abstract fun bindNetworkFromCacheMapper(
        mapper: NetworkFromCacheMapper
    ): Mapper<NetworkCacheDto, SocialNetworkDto>

    @Binds
    abstract fun bindProfileToCacheMapper(
        mapper: ProfileToCacheMapper
    ): Mapper<ProfileDto, ProfileCacheDto>

    @Binds
    abstract fun bindNetworkToCacheMapper(
        mapper: NetworkToCacheMapper
    ): Mapper<SocialNetworkDto, NetworkCacheDto>

    @Binds
    abstract fun bindProfileToDtoMapper(
        mapper: ProfileToDtoMapper
    ): Mapper<Profile, ProfileDto>

    @Binds
    abstract fun bindProfileToDomainMapper(
        mapper: ProfileToDomainMapper
    ): Mapper<ProfileDto, Profile>

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
    abstract fun bindFirmwareToDomainMapper(
        mapper: FirmwareToDomainMapper
    ): Mapper<FirmwareDto, Firmware>

    @Binds
    abstract fun bindFirmwareFileToDomainMapper(
        mapper: FirmwareFileToDomainMapper
    ): Mapper<FirmwareFileDto, FirmwareFile>

    @Binds
    abstract fun bindUserInfoToCacheMapper(
        mapper: UserInfoToCacheMapper
    ): Mapper<UserInfoDto, UserInfoCacheDto>

    @Binds
    abstract fun bindUserInfoFromCacheMapper(
        mapper: UserInfoFromCacheMapper
    ): Mapper<UserInfoCacheDto, UserInfoDto>

    @Binds
    abstract fun bindUserInfoToDomainMapper(
        mapper: UserInfoToDomainMapper
    ): Mapper<UserInfoDto, UserInfo>
}