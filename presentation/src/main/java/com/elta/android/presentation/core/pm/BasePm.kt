@file:Suppress("NOTHING_TO_INLINE", "TooManyFunctions", "MethodOverloading", "LabeledExpression")

package com.elta.android.presentation.core.pm

import androidx.annotation.StringRes
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.analytics.trackEvent
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
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val ACTION_DEBOUNCE_MILLIS = 500L
private const val RELOAD_DELAY_MILLIS = 3000L

@Suppress("SpreadOperator")
abstract class BasePm(
    protected val services: ServiceFacade
) : PresentationModel() {

    lateinit var router: FlowRouter

    val progressState = state(false)
    val progressDialogState = state(false)

    val hideKeyBoardCommand = command<Unit>()
    val showKeyBoardCommand = command<Unit>()
    val showSnackBarCommand = command<SnackBarData>(bufferSize = 1)
    val showToastCommand = command<Int>()

    val retryAction = action<Unit>()

    val networkStateAction = action<Boolean>()
    val networkStateCommand = command<Boolean>(bufferSize = 1)

    val errorControl = stateControl()
    val emptyControl = stateControl()

    internal val resources = services.resources
    internal val network = services.network
    internal val analytics = services.analytics
    internal val bus = services.bus
    internal val errorParser = services.errorParser

    protected val errorHandler: ErrorHandler = errorHandler()

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

    internal fun hideKeyboard() {
        hideKeyBoardCommand.consumer.accept(Unit)
    }

    internal fun setErrorStateData(data: StateData) {
        errorControl.dataState.consumer.accept(data)
    }

    internal fun setErrorViewVisibility(visible: Boolean) {
        errorControl.visibilityState.consumer.accept(visible)
    }

    protected fun showToast(@StringRes messageId: Int) {
        showToastCommand.consumer.accept(messageId)
    }

    protected open fun handleError(error: Throwable) {
        Timber.tag(this::class.java.simpleName).e(error)
        errorHandler.handleError(error)
    }

    protected inline fun <T> Observable<List<T>>.mapFilter(crossinline predicate: (T) -> Boolean): Observable<List<T>> =
        map { it.filter { item -> predicate(item) } }

    protected fun <T> Observable<T>.debounceAction(): Observable<T> =
        this.throttleFirst(ACTION_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)

    protected fun <T> Observable<T>.hideErrorContainer(): Observable<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun <T> Single<T>.hideErrorContainer(): Single<T> =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun Completable.hideErrorContainer(): Completable =
        this.doOnSubscribe { errorControl.visibilityState.consumer.accept(false) }

    protected fun <T> Observable<T>.skipWhileInProgress(): Observable<T> =
        this.skipWhileInProgress(progressState.observable)

    protected fun <T> Observable<T>.bindProgress(): Observable<T> =
        this.bindProgress(progressState.consumer)

    protected fun <T> Single<T>.bindProgress(): Single<T> =
        this.bindProgress(progressState.consumer)

    protected fun Completable.bindProgress(): Completable =
        this.bindProgress(progressState.consumer)

    protected inline fun <T> Single<T>.trackEvent(
        @AnalyticsEventType name: String,
        vararg pairs: Pair<String, String>
    ): Single<T> =
        this.doOnSuccess { this@BasePm.trackEvent(name, *pairs) }

    protected inline fun <T> Observable<T>.trackEvent(@AnalyticsEventType name: String): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(name) }

    protected inline fun <T> Observable<T>.trackEvent(
        crossinline event: (T) -> AnalyticsEvent?
    ): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(event(it)) }

    protected inline fun <T> Observable<T>.trackEvent(
        @AnalyticsEventType name: String,
        vararg pairs: Pair<String, String>
    ): Observable<T> =
        this.doOnNext { this@BasePm.trackEvent(name, *pairs) }

    protected inline fun Completable.trackEvent(@AnalyticsEventType name: String): Completable =
        this.andThen(Completable.fromAction { this@BasePm.trackEvent(name) })

    protected inline fun Completable.trackEvent(
        crossinline event: () -> AnalyticsEvent?
    ): Completable =
        this.doOnComplete { this@BasePm.trackEvent(event()) }
}
