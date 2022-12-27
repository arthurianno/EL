package com.elta.android.presentation.features.profile.support.pm

import com.elta.android.domain.features.devices.interactor.GetGlucometerVersionUseCase
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
    private val getGlucometerVersionUseCase: GetGlucometerVersionUseCase
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.SupportActionClicked>()
            .map { it.action }
            .doOnNext(::handleClick)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .flatMapSingle {
                getGlucometerVersionUseCase.execute()
            }
            .doOnError {
                items.consumer.accept(itemsBuilder.buildItems(""))
            }
            .map(itemsBuilder::buildItems)
            .doOnNext(items.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun handleClick(action: SupportAction) {
        when (action) {
            SupportAction.ConsultantAction -> router.navigateTo(Screens.ConsultantScreen)
            is SupportAction.CallAction -> router.navigateTo(Screens.CallScreen(action.phone))
            is SupportAction.MailAction -> router.navigateTo(Screens.EmailScreen(action.email))
            SupportAction.ServiceCentersAction -> router.startFlow(Screens.ServiceCentersMap)
            SupportAction.TelegramAction -> router.navigateTo(Screens.TelegramScreen(SUPPORT_PHONE))
            SupportAction.ViberAction -> router.navigateTo(Screens.ViberScreen(SUPPORT_PHONE))
            SupportAction.WhatsAppAction -> router.navigateTo(Screens.WhatsAppScreen(SUPPORT_PHONE))
        }
    }
}
