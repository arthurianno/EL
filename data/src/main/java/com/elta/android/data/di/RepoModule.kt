package com.elta.android.data.di

import com.elta.android.data.common.repository.ClipboardDataRepository
import com.elta.android.data.common.repository.DownloadDataRepository
import com.elta.android.data.features.files.repository.FileInfoDataRepository
import com.elta.android.data.features.appsettings.AppSettingsDataRepository
import com.elta.android.data.features.auth.repository.AuthDataRepository
import com.elta.android.data.features.auth.repository.AuthDataRepositoryVariantA
import com.elta.android.data.features.auth.repository.SocialDataRepository
import com.elta.android.data.features.calculator.repository.CalculatorDataRepository
import com.elta.android.data.features.calculator.repository.CustomProductDataRepository
import com.elta.android.data.features.consultant.repository.AudioPlayerDataRepository
import com.elta.android.data.features.consultant.repository.AudioRecorderDataRepository
import com.elta.android.data.features.consultant.repository.ConsultantDataRepository
import com.elta.android.data.features.consultant.repository.MediaDataRepository
import com.elta.android.data.features.devices.repository.BluetoothStateDataRepository
import com.elta.android.data.features.devices.repository.BluetoothStateDataRepositoryVariantA
import com.elta.android.data.features.devices.repository.DeviceDataRepository
import com.elta.android.data.features.devices.repository.DeviceInfoDataRepository
import com.elta.android.data.features.devices.repository.PinDataRepository
import com.elta.android.data.features.devices.repository.UpdateRepositoryImpl
import com.elta.android.data.features.diary.events.repository.EventsDataRepository
import com.elta.android.data.features.diary.medicines.repository.InsulinMedicamentDataRepository
import com.elta.android.data.features.diary.medicines.repository.MedicamentDataRepository
import com.elta.android.data.features.diary.tags.repository.TagsDataRepository
import com.elta.android.data.features.emias.repository.EmiasDataRepository
import com.elta.android.data.features.feedback.repository.FeedbackDataRepository
import com.elta.android.data.features.firmware.repository.FirmwareDataRepository
import com.elta.android.data.features.glucometers.repository.GlucometersDataRepository
import com.elta.android.data.features.googlefit.repository.GoogleFitDataRepository
import com.elta.android.data.features.multiLang.repositories.ScreenConfigRepositoryImpl
import com.elta.android.data.features.newsChannel.repository.NewsDataRepository
import com.elta.android.data.features.observers.repository.ObserverDataRepository
import com.elta.android.data.features.reminder.repository.ReminderDataRepository
import com.elta.android.data.features.remoteconfig.repository.RemoteConfigDataRepository
import com.elta.android.data.features.reports.repository.ReportsDataRepository
import com.elta.android.data.features.rostech.IomtDataRepository
import com.elta.android.data.features.sale_points.repository.SalePointsDataRepository
import com.elta.android.data.features.user.repository.ProfileDataRepository
import com.elta.android.data.features.userinfo.repository.UserInfoDataRepository
import com.elta.android.data.features.version.repository.VersionDataRepository
import com.elta.android.domain.common.repository.ClipboardRepository
import com.elta.android.domain.common.repository.DownloadRepository
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.auth.repository.AuthRepositoryVariantA
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.calculator.repository.CustomProductRepository
import com.elta.android.domain.features.consultant.repository.AudioPlayerRepository
import com.elta.android.domain.features.consultant.repository.AudioRecorderRepository
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.BluetoothStateRepositoryVariantA
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.medicines.repository.InsulinMedicamentRepository
import com.elta.android.domain.features.diary.medicines.repository.MedicamentRepository
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.emias.repository.EmiasRepository
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.elta.android.domain.features.glucometers.repository.GlucometersRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.multiLang.repositories.ScreenConfigRepository
import com.elta.android.domain.features.newsChannel.repository.NewsRepository
import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.elta.android.domain.features.reports.repository.ReportsRepository
import com.elta.android.domain.features.rostech.repository.IomtRepository
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import com.elta.android.domain.features.version.repository.VersionRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Suppress("TooManyFunctions")
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repo: AuthDataRepository): AuthRepository

    // fixme Variant A : recovery_account
    @Binds
    @Singleton
    abstract fun bindAuthRepositoryVariantA(repo: AuthDataRepositoryVariantA): AuthRepositoryVariantA

    @Binds
    @Singleton
    abstract fun bindNewsRepository(repo: NewsDataRepository): NewsRepository

    @Binds
    @Singleton
    abstract fun bindAuthSocialRepository(repo: SocialDataRepository): SocialRepository

    @Binds
    @Singleton
    abstract fun bindScreenConfigRepository(repo: ScreenConfigRepositoryImpl): ScreenConfigRepository

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
    abstract fun bindDeviceRepository(repo: DeviceDataRepository): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(repo: UpdateRepositoryImpl): UpdateRepository

    @Binds
    @Singleton
    abstract fun bindDeviceInfoRepository(repo: DeviceInfoDataRepository): DeviceInfoRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(repo: PinDataRepository): PinRepository

    @Binds
    @Singleton
    abstract fun bindBluetoothStateRepository(repo: BluetoothStateDataRepository): BluetoothStateRepository

    // fixme Variant A : improved_enabling_location
    @Binds
    @Singleton
    abstract fun bindBluetoothStateRepositoryVariantA(repo: BluetoothStateDataRepositoryVariantA): BluetoothStateRepositoryVariantA

    @Binds
    @Singleton
    abstract fun bindRemindersRepository(repo: ReminderDataRepository): RemindersRepository

    @Binds
    @Singleton
    abstract fun bindFirmwareRepository(repo: FirmwareDataRepository): FirmwareRepository

    @Binds
    @Singleton
    abstract fun bindIomtRepository(repo: IomtDataRepository): IomtRepository

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

    @Binds
    @Singleton
    abstract fun bindCustomProductRepository(repo: CustomProductDataRepository): CustomProductRepository

    @Binds
    @Singleton
    abstract fun bindConsultantRepository(repo: ConsultantDataRepository): ConsultantRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(repo: MediaDataRepository): MediaRepository

    @Binds
    @Singleton
    abstract fun bindInsulinMedicamentRepository(repo: InsulinMedicamentDataRepository): InsulinMedicamentRepository

    @Binds
    @Singleton
    abstract fun bindMedicamentRepository(repo: MedicamentDataRepository): MedicamentRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsDataRepository(source: AppSettingsDataRepository): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindGlucometersRepository(repo: GlucometersDataRepository): GlucometersRepository

    @Binds
    @Singleton
    abstract fun bindEmiasRepository(repo: EmiasDataRepository): EmiasRepository

    @Binds
    @Singleton
    abstract fun bindVersionRepository(source: VersionDataRepository): VersionRepository

    @Binds
    @Singleton
    abstract fun bindAudioRecorderRepository(source: AudioRecorderDataRepository): AudioRecorderRepository

    @Binds
    @Singleton
    abstract fun bindAudioPlayerRepository(source: AudioPlayerDataRepository): AudioPlayerRepository

    @Binds
    @Singleton
    abstract fun bindClipboardRepository(source: ClipboardDataRepository): ClipboardRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(source: DownloadDataRepository): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindFileInfoRepository(source: FileInfoDataRepository): FileInfoRepository

    @Binds
    @Singleton
    abstract fun bindRemoteConfigRepository(source: RemoteConfigDataRepository): RemoteConfigRepository

}
