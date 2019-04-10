package com.elta.android.presentation.features.profile.settings.global.pm

import com.elta.android.domain.features.auth.interactor.LinkSocialNetworkUseCase
import com.elta.android.domain.features.auth.interactor.UnLinkSocialNetworkUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.builder.ProfileSettingsItemsBuilder
import io.reactivex.Single
import me.dmdev.rxpm.widget.dialogControl
import javax.inject.Inject

class ProfileSettingsPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val linkSocialNetworkUseCase: LinkSocialNetworkUseCase,
    private val unlinkSocialNetworkUseCase: UnLinkSocialNetworkUseCase,
    private val itemsBuilder: ProfileSettingsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val unlinkNetworkDialogControl = dialogControl<DialogData, DialogResult>()
    val openPrivacyPolicyCommand = Command<Unit>(bufferSize = 1)

    private val socialNetworkState = State<SocialNetworkType>()
    private val getProfileSettingsAction = Action<Unit>()
    private val linkSocialUserAction = Action<Unit>()
    private val unlinkSocialUserAction = Action<Unit>()

    private val unlinkNetworkDialogData: DialogData by lazy { Dialogs.EventUnlinkNetwork(resources) }

    override fun onCreate() {
        super.onCreate()
        observeClicks()
        observeNetworksActions()

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
        bus.clicks<Clicks.ProfileSettingsItemClicked>()
            .map { it.type }
            .doOnNext { type ->
                when (type) {
                    ProfileSettingsItem.Type.LEGAL_INFO -> openPrivacyPolicyCommand.consumer.accept(Unit)
                    else -> {
                    }
                }
            }
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ProfileSettingsSocialItemClicked>()
            .map { it.item }
            .doOnNext { socialNetworkState.consumer.accept(it.type) }
            .subscribe {
                if (it.isLinked) unlinkSocialUserAction.consumer.accept(Unit)
                else linkSocialUserAction.consumer.accept(Unit)
            }
            .untilDestroy()
    }

    private fun observeNetworksActions() {
        linkSocialUserAction.observable
            .map { socialNetworkState.value }
            .map(::createLinkSocialUserParams)
            .flatMapCompletable { params ->
                linkSocialNetworkUseCase.execute(params)
                    .doOnComplete { getProfileSettingsAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        unlinkSocialUserAction.observable
            .switchMapMaybe {
                unlinkNetworkDialogControl.showForResult(unlinkNetworkDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .map { socialNetworkState.value }
            .map(::createUnlinkSocialUserParams)
            .flatMapCompletable { params ->
                unlinkSocialNetworkUseCase.execute(params)
                    .doOnComplete { getProfileSettingsAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun Single<Profile>.handleProfileUseCase() =
        map { itemsBuilder.buildItems(it) }
            .doOnSuccess { items.consumer.accept(it) }

    private fun createLinkSocialUserParams(network: SocialNetworkType) =
        LinkSocialNetworkUseCase.Params(network)

    private fun createUnlinkSocialUserParams(network: SocialNetworkType) =
        UnLinkSocialNetworkUseCase.Params(network)

    enum class DialogResult {
        NEGATIVE, POSITIVE
    }
}