@file:Suppress("NOTHING_TO_INLINE", "TooManyFunctions")

package com.elta.android.presentation.core.pm

import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.core.pm.listeners.Trackable
import com.elta.android.presentation.core.pm.widgets.ErrorHandler
import com.elta.android.presentation.core.pm.widgets.errorHandler
import com.elta.android.presentation.core.pm.widgets.networkControl
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.core.ui.state_view.StateData
import com.nullgr.core.rx.bindProgress
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.skipWhileInProgress
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class BasePm(
    protected val services: ServiceFacade
) : PresentationModel() {

    lateinit var router: Router

    val progressState = State(false)
    val progressDialogState = State(false)

    val hideKeyBoardCommand = Command<Unit>()
    val showKeyBoardCommand = Command<Unit>()
    val showSnackBarCommand = Command<SnackBarData>(bufferSize = 1)

    val retryAction = Action<Unit>()
    val networkStateAction = Action<Boolean>()
    val networkStateCommand = Command<Boolean>(bufferSize = 1)

    val errorControl = stateControl()
    val emptyControl = stateControl()

    internal val resources = services.resources
    internal val network = services.network
    internal val analytics = services.analytics
    internal val bus = services.bus
    internal val errorParser = services.errorParser

    protected val errorHandler: ErrorHandler = errorHandler()
    internal val flowRouter: FlowRouter? by lazy { router as? FlowRouter }

    private val networkControl by lazy { networkControl(network) }

    open val isEmptyScreen: Boolean = false

    override fun onCreate() {
        super.onCreate()

        if (this is Trackable) {
            analytics.trackEvent(analyticsEvent)
        }

        if (this is ConnectionListener) {
            networkStateAction.observable
                .doOnNext { networkStateCommand.consumer.accept(it) }
                .subscribe()
                .untilDestroy()

            networkControl.observable
                .subscribe()
                .untilDestroy()
        }
    }

    internal fun showSnackBar(data: SnackBarData) {
        showSnackBarCommand.consumer.accept(data)
    }

    internal fun setErrorStateData(data: StateData) {
        errorControl.dataState.consumer.accept(data)
    }

    internal fun setErrorViewVisibility(visible: Boolean) {
        errorControl.visibilityState.consumer.accept(visible)
    }

    protected open fun handleError(error: Throwable) {
        Timber.e(error)
        errorHandler.handleError(error)
    }

    protected inline fun <T> Observable<T>.debounceAction(): Observable<T> =
        this.throttleFirst(ACTION_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)

    protected inline fun <T> Observable<T>.hideErrorContainer(): Observable<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected inline fun <T> Single<T>.hideErrorContainer(): Single<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected inline fun Completable.hideErrorContainer(): Completable =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected inline fun <T> Observable<T>.skipWhileInProgress(): Observable<T> =
        this.skipWhileInProgress(progressState.observable)

    protected inline fun <T> Observable<T>.bindProgress(): Observable<T> =
        this.bindProgress(progressState.consumer)

    protected inline fun <T> Single<T>.bindProgress(): Single<T> =
        this.bindProgress(progressState.consumer)

    protected inline fun Completable.bindProgress(): Completable =
        this.bindProgress(progressState.consumer)

    companion object {
        const val ACTION_DEBOUNCE_MILLIS = 500L
        const val RELOAD_DELAY_MILLIS = 3000L
    }
}