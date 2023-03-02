package com.elta.android.presentation.features.statistic.report.pm

import android.net.Uri
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.reports.interactor.GetReportUseCase
import com.elta.android.domain.features.reports.interactor.buildRange
import com.elta.android.domain.features.reports.model.Range
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import org.threeten.bp.LocalDate
import javax.inject.Inject

private const val FORMAT = "d LLLL"

class ReportPeriodChooserPm @Inject constructor(
    private val getReportUseCase: GetReportUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val mainAction = action<Unit>()
    val closeDialogCommand = command<Unit>(bufferSize = 1)
    val selectDateAction = action<LocalDate>()
    val selectedRangeState = state(buildRange())
    val titleState = state<String>()

    override fun onCreate() {
        super.onCreate()
        observeActions()
        observeStates()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is NetworkConnectionError -> {
                showSnackBar(
                    SnackBarMessageData.SimpleTextMessage(
                        error.message ?: resources.getString(
                            R.string.default_error_message
                        )
                    )
                )
            }

            else -> super.handleError(error)
        }
    }

    private fun observeStates() {
        selectedRangeState.observable
            .map(::formatRange)
            .subscribe(titleState.consumer)
            .untilDestroy()
    }

    private fun observeActions() {
        selectDateAction.observable
            .map(::buildRange)
            .subscribe(selectedRangeState.consumer)
            .untilDestroy()
        mainAction.observable
            .map(::createGetReportParams)
            .flatMapSingle {
                getReportUseCase.execute(it)
                    .bindProgress()
                    .doOnError(::handleError)
                    .doOnSuccess(::handleReport)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun formatRange(range: Range): String =
        "${range.start.toStringWithFormat(FORMAT)} - ${range.end.toStringWithFormat(FORMAT)}"

    private fun createGetReportParams(i: Unit) =
        GetReportUseCase.Params(selectedRangeState.value)

    private fun handleReport(uri: Uri) {
        bus.event(Events.ReportLoadedEvent(uri))
        closeDialogCommand.consumer.accept(Unit)
    }
}
