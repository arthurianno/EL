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
    private val markFeedbackSent: () -> Completable
) {
    fun promptStep(): Maybe<Int> =
        loadFeedback()
            .filter { it.isSendFeedback }
            .map { it.step }

    fun routeAfterLikeDialog(result: DialogResult): FeedbackRoute =
        when (result) {
            DialogResult.POSITIVE -> FeedbackRoute.PLAY_MARKET
            DialogResult.NEGATIVE -> FeedbackRoute.FEEDBACK
            DialogResult.NEURAL -> FeedbackRoute.DISMISS
        }

    fun confirmPlayMarketRating(): Completable = markFeedbackSent()
}
