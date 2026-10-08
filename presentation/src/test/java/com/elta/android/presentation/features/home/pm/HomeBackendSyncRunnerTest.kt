package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.nullgr.core.rx.RxBus
import io.reactivex.Completable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackendSyncRunnerTest {
    @Test
    fun `sync requests remain available after construction and asynchronous errors`() {
        val actions = PublishSubject.create<Unit>()
        val bus = RxBus()
        val progress = mutableListOf<Boolean>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val eventObserver = bus.events<Events.Sync.Server>().test()
        val runner = HomeBackendSyncRunner(
            syncLocalChanges = {
                when (++attempts) {
                    1 -> throw IOException("cannot create request")
                    2 -> Completable.error(IOException("offline"))
                    else -> Completable.complete()
                }
            },
            connectIomt = { Completable.complete() },
            bus = bus
        )
        val observer = actions.concatMapCompletable {
            runner.syncAll(Consumer { progress += it }, errors::add)
        }.test()

        repeat(3) { actions.onNext(Unit) }
        actions.onComplete()

        observer.assertComplete().assertNoErrors()
        assertEquals(3, attempts)
        assertEquals(2, errors.size)
        assertEquals(listOf(true, false, true, false, true, false), progress)
        assertEquals(3, eventObserver.values().count { it == Events.Sync.Server.Started })
        assertEquals(1, eventObserver.values().count { it == Events.Sync.Server.Success })
    }
}
