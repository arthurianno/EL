package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.core.ui.dialog.DialogResult
import io.reactivex.Completable
import io.reactivex.Single
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
            disableReminder = { Completable.complete() }
        )

        prompt.route().test().assertValue(ManualGlucoseRoute.OPEN_ENTRY).assertComplete()
        assertEquals(0, emiasQueries)
    }

    @Test
    fun `linked Emias shows reminder and failed status opens entry`() {
        val linked = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.just(true) },
            disableReminder = { Completable.complete() }
        )
        val failed = HomeManualGlucosePrompt(
            shouldShowReminder = { Single.just(true) },
            isEmiasLinked = { Single.error(IllegalStateException("offline")) },
            disableReminder = { Completable.complete() }
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
            }
        )

        prompt.shouldOpenAfterDialog(DialogResult.NEGATIVE).test().assertValue(false)
        prompt.shouldOpenAfterDialog(DialogResult.POSITIVE).test().assertValue(true)
        prompt.shouldOpenAfterDialog(DialogResult.NEURAL)
            .doOnSuccess { steps += "open" }
            .test()
            .assertValue(true)

        assertEquals(listOf("disable", "open"), steps)
    }
}
