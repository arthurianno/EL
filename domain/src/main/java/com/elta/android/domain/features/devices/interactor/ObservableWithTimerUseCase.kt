package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.COMMAND_TIMEOUT
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeoutException

abstract class ObservableWithTimerUseCase<T, in Params> protected constructor(
    schedulersFacade: SchedulersFacade,
    private val crashlyticsReport: CrashlyticsReport
) : ObservableUseCase<T, Params>(schedulersFacade) {

    private var timer: Timer? = null

    /**
     * По истечении срока указанного в COMMAND_TIMEOUT отменяет скоуп, переданный в аргументах
     * @param scope скоуп корутин, отменяющийся по истечении таймера
     */
    protected fun resetAndLaunchTimer(scope: ProducerScope<T>) {
        cancelTimer()

        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    val error = GlucometerSyncError(TimeoutException("timeout"))
                    crashlyticsReport.writeException(error)
                    scope.cancel("timeout", error)
                }
            }, COMMAND_TIMEOUT)
        }
    }

    protected fun cancelTimer() {
        timer?.cancel()
    }

}
