package com.elta.android.presentation.features.profile.main.pm

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.AdditionalFunction
import com.elta.android.domain.features.user.model.MyObservers
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.WhereBuy
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem.Type
import com.elta.android.presentation.features.profile.main.ui.builder.MainProfileOptionsItemsBuilder
import com.nullgr.core.resources.ResourceProvider
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class MainProfilePm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val itemsBuilder: MainProfileOptionsItemsBuilder,
    private val resourceProvider: ResourceProvider,
    services: ServiceFacade
) : BaseListPm(services) {

    val userFullNameState = State<String>()
    val profileSettingsAction = Action<Unit>()
    val openDiabetesTypeDialogCommand = Command<Unit>(bufferSize = 1)

    private val getProfileSettingsAction = Action<Unit>()
    private val updateProfileAction = Action<Profile>()

    override fun onCreate() {
        super.onCreate()
        observeClicks()
        observeProfileUpdates()

        getProfileSettingsAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getProfileUseCase.execute()
                    .bindProgress()
                    .handleProfileUseCase()
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getProfileSettingsAction.consumer)
            .untilDestroy()
    }

    private fun observeClicks() {
        bus.clicks<Clicks.ProfileIndicatorClicked>()
            .map { it.item }
            .doOnNext(::navigateIndicatorScreen)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ProfileAdditionalClicked>()
            .map { it.item.type }
            .doOnNext(::navigateAdditionalSettingsScreen)
            .subscribe()
            .untilDestroy()

        profileSettingsAction.observable
            //  todo start settings screen
            .doOnNext { Timber.e("Profile Settings clicked") }
            .subscribe()
            .untilDestroy()
    }

    private fun navigateIndicatorScreen(type: MainProfileIndicatorItem.Type) =
        when (type) {
            Type.GLUCOSE_LEVEL -> Timber.e("GLUCOSE_LEVEL clicked")
            Type.DIABETES -> openDiabetesTypeDialogCommand.consumer.accept(Unit)
            Type.WEIGHT -> Timber.e("WEIGHT clicked")
            Type.HEMOGLOBIN -> Timber.e("HEMOGLOBIN clicked")
        }

    private fun navigateAdditionalSettingsScreen(type: AdditionalFunction) =
        when (type) {
            WhereBuy -> router.startFlow(Screens.ShopsMap)
            MyObservers -> Timber.e("MY_OBSERVERS clicked")
            else -> throw IllegalArgumentException("$type  type doesn't support.")
        }

    private fun setUpFullUserName(profile: Profile) {
        val firstName = profile.firstName
        val secondName = profile.secondName
        val fullName: String = when {
            firstName.isNullOrEmpty() && secondName.isNullOrEmpty() ->
                resourceProvider.getString(R.string.profile_name_placeholder)
            firstName.isNullOrEmpty() -> secondName ?: ""
            secondName.isNullOrEmpty() -> firstName
            else -> "$firstName $secondName"
        }
        userFullNameState.consumer.accept(fullName)
    }

    private fun observeProfileUpdates() {
        bus.events<Events.ProfileChanged>()
            .map { it.profile }
            .doOnNext(updateProfileAction.consumer)
            .subscribe()
            .untilDestroy()

        updateProfileAction.observable
            .map(::createUpdateProfileUseCaseParams)
            .flatMapSingle {
                updateProfileUseCase.execute(it)
                    .andThen(
                        getProfileUseCase.execute(Unit)
                            .handleProfileUseCase()
                    )
                    .bindProgress()
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun Single<Profile>.handleProfileUseCase() =
        this.doOnSuccess(::setUpFullUserName)
            .map { itemsBuilder.buildItems(it) }
            .doOnSuccess { items.consumer.accept(it) }

    private fun createUpdateProfileUseCaseParams(profile: Profile) =
        UpdateProfileUseCase.Params(profile)
}