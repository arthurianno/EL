package com.elta.android.data.di

import com.elta.android.data.features.auth.repository.AuthDataRepository
import com.elta.android.data.features.auth.repository.SocialDataRepository
import com.elta.android.data.features.calculator.repository.CalculatorDataRepository
import com.elta.android.data.features.devices.repository.DeviceDataRepository
import com.elta.android.data.features.diary.events.repository.EventsDataRepository
import com.elta.android.data.features.diary.insulin.repository.DrugNameDataRepository
import com.elta.android.data.features.diary.tags.repository.TagsDataRepository
import com.elta.android.data.features.feedback.repository.FeedbackDataRepository
import com.elta.android.data.features.firmware.repository.FirmwareDataRepository
import com.elta.android.data.features.googlefit.repository.GoogleFitDataRepository
import com.elta.android.data.features.observers.repository.ObserverDataRepository
import com.elta.android.data.features.reminder.repository.ReminderDataRepository
import com.elta.android.data.features.reports.repository.ReportsDataRepository
import com.elta.android.data.features.sale_points.repository.SalePointsDataRepository
import com.elta.android.data.features.user.repository.ProfileDataRepository
import com.elta.android.data.features.userinfo.repository.UserInfoDataRepository
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.insulin.DrugNameRepository
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.elta.android.domain.features.reports.repository.ReportsRepository
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Suppress("TooManyFunctions")
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repo: AuthDataRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthSocialRepository(repo: SocialDataRepository): SocialRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(repo: ProfileDataRepository): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSalePointsRepository(repo: SalePointsDataRepository): SalePointsRepository

    @Binds
    @Singleton
    abstract fun bindObserverRepository(repo: ObserverDataRepository): ObserverRepository

    @Binds
    @Singleton
    abstract fun bindEventsRepository(repo: EventsDataRepository): EventsRepository

    @Binds
    @Singleton
    abstract fun bindTagsRepository(repo: TagsDataRepository): TagsRepository

    @Binds
    @Singleton
    abstract fun bindInsulinDrugNameRepository(repo: DrugNameDataRepository): DrugNameRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(repo: DeviceDataRepository): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindRemindersRepository(repo: ReminderDataRepository): RemindersRepository

    @Binds
    @Singleton
    abstract fun bindFirmwareRepository(repo: FirmwareDataRepository): FirmwareRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(repo: FeedbackDataRepository): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindUserInfoRepository(repo: UserInfoDataRepository): UserInfoRepository

    @Binds
    @Singleton
    abstract fun bindGoogleFitRepository(repo: GoogleFitDataRepository): GoogleFitRepository

    @Binds
    @Singleton
    abstract fun bindReportsRepository(repo: ReportsDataRepository): ReportsRepository

    @Binds
    @Singleton
    abstract fun bindCalculatorRepository(repo: CalculatorDataRepository): CalculatorRepository
}
