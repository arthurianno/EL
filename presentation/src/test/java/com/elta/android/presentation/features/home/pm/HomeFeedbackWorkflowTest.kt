package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.feedback.model.FeedbackDataModel
import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFeedbackWorkflowTest {
    @Test
    fun `feedback prompt remains available after a failed request`() {
        val actions = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val workflow = HomeFeedbackWorkflow(
            loadFeedback = {
                if (++attempts == 1) Single.error(IOException("offline"))
                else Single.just(FeedbackDataModel(isSendFeedback = true, step = 2))
            },
            markFeedbackSent = { Completable.complete() },
            reportError = errors::add
        )
        val observer = actions.flatMapMaybe { workflow.promptStep() }.test()

        actions.onNext(Unit)
        actions.onNext(Unit)
        actions.onComplete()

        observer.assertValue(2).assertComplete().assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }

    @Test
    fun `market opens only after a successful save and later actions still work`() {
        val actions = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val workflow = HomeFeedbackWorkflow(
            loadFeedback = { Single.just(FeedbackDataModel(false, 0)) },
            markFeedbackSent = {
                if (++attempts == 1) throw IOException("cannot create request")
                Completable.complete()
            },
            reportError = errors::add
        )
        val observer = actions.flatMapMaybe { workflow.confirmPlayMarketRating() }.test()

        actions.onNext(Unit)
        actions.onNext(Unit)
        actions.onComplete()

        observer.assertValue(Unit).assertComplete().assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }

    @Test
    fun `like dialog choices have distinct destinations`() {
        val workflow = HomeFeedbackWorkflow(
            loadFeedback = { Single.just(FeedbackDataModel(false, 0)) },
            markFeedbackSent = { Completable.complete() },
            reportError = {}
        )

        assertEquals(FeedbackRoute.PLAY_MARKET, workflow.routeAfterLikeDialog(DialogResult.POSITIVE))
        assertEquals(FeedbackRoute.FEEDBACK, workflow.routeAfterLikeDialog(DialogResult.NEGATIVE))
        assertEquals(FeedbackRoute.DISMISS, workflow.routeAfterLikeDialog(DialogResult.NEURAL))
        workflow.promptStep().test().assertNoValues().assertComplete()
    }
}
