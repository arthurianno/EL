package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.feedback.model.FeedbackDataModel
import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

internal enum class FeedbackRoute {
    PLAY_MARKET,
    FEEDBACK,
    DISMISS
}

internal class HomeFeedbackWorkflow(
    private val loadFeedback: () -> Single<FeedbackDataModel>,
    private val markFeedbackSent: () -> Completable,
    private val reportError: (Throwable) -> Unit
) {
    fun promptStep(): Maybe<Int> =
        Single.defer { loadFeedback() }
            .filter { it.isSendFeedback }
            .map { it.step }
            .doOnError(reportError)
            .onErrorComplete()

    fun routeAfterLikeDialog(result: DialogResult): FeedbackRoute =
        when (result) {
            DialogResult.POSITIVE -> FeedbackRoute.PLAY_MARKET
            DialogResult.NEGATIVE -> FeedbackRoute.FEEDBACK
            DialogResult.NEURAL -> FeedbackRoute.DISMISS
        }

    fun confirmPlayMarketRating(): Maybe<Unit> =
        Completable.defer { markFeedbackSent() }
            .andThen(Maybe.just(Unit))
            .doOnError(reportError)
            .onErrorComplete()
}
