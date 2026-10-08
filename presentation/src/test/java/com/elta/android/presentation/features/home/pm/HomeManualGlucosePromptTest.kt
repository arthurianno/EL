package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeManualGlucosePromptTest {
    @Test
    fun `dismissed reminder setting opens entry without querying Emias`() {
        var emiasQueries = 0
        val prompt = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(false) },
            isEmiasLinked = {
                emiasQueries++
                Single.just(true)
            },
            disableReminder = { Completable.complete() },
            reportError = {}
        )

        prompt.route().test().assertValue(ManualGlucoseRoute.OPEN_ENTRY).assertComplete()
        assertEquals(0, emiasQueries)
    }

    @Test
    fun `linked Emias shows reminder and failed status opens entry`() {
        val linked = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.just(true) },
            disableReminder = { Completable.complete() },
            reportError = {}
        )
        val failed = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.error(IllegalStateException("offline")) },
            disableReminder = { Completable.complete() },
            reportError = {}
        )

        linked.route().test().assertValue(ManualGlucoseRoute.SHOW_REMINDER).assertComplete()
        failed.route().test().assertValue(ManualGlucoseRoute.OPEN_ENTRY).assertComplete()
    }

    @Test
    fun `neutral choice disables future reminders before entry opens`() {
        val steps = mutableListOf<String>()
        val prompt = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.just(true) },
            disableReminder = {
                Completable.fromAction { steps += "disable" }
            },
            reportError = {}
        )

        prompt.shouldOpenAfterDialog(DialogResult.NEGATIVE).test().assertValue(false)
        prompt.shouldOpenAfterDialog(DialogResult.POSITIVE).test().assertValue(true)
        prompt.shouldOpenAfterDialog(DialogResult.NEURAL)
            .doOnSuccess { steps += "open" }
            .test()
            .assertValue(true)

        assertEquals(listOf("disable", "open"), steps)
    }

    @Test
    fun `failed preference read still opens entry and next action works`() {
        val actions = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val prompt = HomeManualGlucosePrompt(
            shouldShowReminder = {
                if (++attempts == 1) Single.error(IOException("offline"))
                else Single.just(false)
            },
            isEmiasLinked = { Single.just(false) },
            disableReminder = { Completable.complete() },
            reportError = { errors += it }
        )
        val observer = actions.flatMapSingle { prompt.route() }.test()

        actions.onNext(Unit)
        actions.onNext(Unit)
        actions.onComplete()

        observer.assertValues(ManualGlucoseRoute.OPEN_ENTRY, ManualGlucoseRoute.OPEN_ENTRY)
            .assertComplete()
            .assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }

    @Test
    fun `failed reminder update reports error but does not block entry`() {
        val actions = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val prompt = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.just(true) },
            disableReminder = {
                if (++attempts == 1) Completable.error(IOException("offline"))
                else Completable.complete()
            },
            reportError = { errors += it }
        )
        val observer = actions.flatMapSingle {
            prompt.shouldOpenAfterDialog(DialogResult.NEURAL)
        }.test()

        actions.onNext(Unit)
        actions.onNext(Unit)
        actions.onComplete()

        observer.assertValues(true, true)
            .assertComplete()
            .assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }
}
