package com.elta.android.presentation.features.profile.support.pm

import android.util.Log
import com.elta.android.domain.features.devices.interactor.GetGlucometerVersionUseCase
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.elta.android.presentation.features.profile.support.ui.builder.SupportItemsBuilder
import javax.inject.Inject

private const val SUPPORT_PHONE = "+79152767676"

class SupportPm @Inject constructor(
    services: ServiceFacade,
    private val itemsBuilder: SupportItemsBuilder,
    private val getGlucometerVersionUseCase: GetGlucometerVersionUseCase,
    private val userProfile: GetUpdatedProfileUseCase
) : BaseListPm(services) {

    private var userId: String = ""
    private var userName: String = ""

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.SupportActionClicked>()
            .map { it.action }
            .doOnNext(::handleClick)
            .subscribe({ /* onNext */ }, { error ->
                Log.e("SupportPm", "Error handling click: $error")
            })
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext {
                userProfile.execute()
                    .subscribe(
                        {
                            userId = it.email.orEmpty()
                            userName = "${it.firstName.orEmpty()} ${it.secondName.orEmpty()}".trim()
                        },
                        { error -> Log.e("SupportPm", "Error fetching profile: $error") }
                    )
                    .untilDestroy()
            }
            .flatMapSingle {
                getGlucometerVersionUseCase.execute()
                    .onErrorReturn { "" }
            }
            .doOnError {
                Log.e("SupportPm", "Error fetching glucometer version: $it")
                items.consumer.accept(itemsBuilder.buildItems(""))
            }
            .map(itemsBuilder::buildItems)
            .doOnNext(items.consumer)
            .subscribe({ /* onNext */ }, { error ->
                Log.e("SupportPm", "Error in chain: $error")
            })
            .untilDestroy()
    }

    private fun handleClick(action: SupportAction) {
        when (action) {
            SupportAction.ConsultantAction -> router.navigateTo(Screens.ConsultantScreen(userId, userName))
            is SupportAction.CallAction -> router.navigateTo(Screens.CallScreen(action.phone))
            is SupportAction.MailAction -> router.navigateTo(Screens.EmailScreen(action.email))
            SupportAction.ServiceCentersAction -> router.startFlow(Screens.ServiceCentersMap)
            SupportAction.TelegramAction -> router.navigateTo(Screens.TelegramScreen(SUPPORT_PHONE))
            SupportAction.ViberAction -> router.navigateTo(Screens.ViberScreen(SUPPORT_PHONE))
            SupportAction.WhatsAppAction -> router.navigateTo(Screens.WhatsAppScreen(SUPPORT_PHONE))
            SupportAction.AppVersionAction -> {
                Log.d("SupportPm", "AppVersionAction clicked")
                itemsBuilder.toggleExtraVersions()
                getGlucometerVersionUseCase.execute()
                    .onErrorReturn { "" }
                    .map { glucometerVersion ->
                        itemsBuilder.buildItems(glucometerVersion)
                    }
                    .doOnSuccess {
                        Log.d("SupportPm", "Updating items: $it")
                        items.consumer.accept(it)
                    }
                    .subscribe({ /* onSuccess */ }, { error ->
                        Log.e("SupportPm", "Error updating versions: $error")
                    })
                    .untilDestroy()
            }
        }
    }
}