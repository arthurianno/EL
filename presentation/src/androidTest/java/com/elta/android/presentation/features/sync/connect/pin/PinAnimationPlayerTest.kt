package com.elta.android.presentation.features.sync.connect.pin

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.elta.android.presentation.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PinAnimationPlayerTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val compositions = mutableMapOf<Int, LottieComposition>()
    private lateinit var player: PinAnimationPlayer
    private lateinit var view: LottieAnimationView
    private var frames = 0
    private var emptyFrames = 0
    private var pinReady = false

    @Before
    fun prepareAnimations() {
        val bitmaps = mutableMapOf<String, Bitmap>()
        pinAnimationResources.forEach { compositions[it] = loadPinComposition(compose.activity, it, bitmaps) }
        compose.runOnUiThread {
            view = LottieAnimationView(compose.activity)
            view.speed = 10f
            player = PinAnimationPlayer(view) { pinReady = it }
            compositions.forEach { (resource, composition) -> player.prepare(resource, composition) }
            player.resume()
            player.show(PinConnectionStage.SEARCH, preview = false)
        }
        compose.setContent { AndroidView(factory = { view }, modifier = Modifier.size(375.dp, 562.dp)) }
        compose.runOnUiThread {
            view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    frames++
                    if (view.composition == null || view.composition?.images?.values?.any { it.bitmap == null } == true) emptyFrames++
                    return true
                }
            })
        }
    }

    @After
    fun closePlayer() { compose.runOnUiThread { player.close() } }

    @Test
    fun preparedAssetsShareMeterBitmapsAndNeverRequireDecodingOnDraw() {
        compositions.values.forEach { composition ->
            composition.images.values.forEach { assertNotNull(it.bitmap) }
        }
        val loopImages = compositions.getValue(R.raw.glucometer_bluetooth).images.values.associateBy { it.fileName }
        compositions.getValue(R.raw.glucometer_bluetooth_to_ready).images.values.forEach { asset ->
            loopImages[asset.fileName]?.let { assertSame(it.bitmap, asset.bitmap) }
        }
    }

    @Test
    fun arrivingDeviceDoesNotInterruptSearchEntryTransition() {
        compose.runOnUiThread {
            player.show(PinConnectionStage.HOW_TO_CONNECT, preview = false)
            player.show(PinConnectionStage.SEARCH, preview = false)
            val entry = view.composition
            assertSame(compositions[R.raw.glucometer_to_bluetooth], entry)
            player.show(PinConnectionStage.FOUND, preview = false)
            assertSame(entry, view.composition)
        }
        compose.waitUntil(5000) {
            var settled = false
            compose.runOnUiThread {
                settled = view.composition === compositions[R.raw.glucometer_bluetooth_to_ready] && !view.isAnimating && view.progress > 0.99f
            }
            settled
        }
        compose.runOnUiThread {
            assertEquals(0, emptyFrames)
            assertTrue(frames > 0)
        }
    }

    @Test
    fun rapidResultsCoalesceToLatestStageWithoutBlankFrames() {
        compose.runOnUiThread {
            player.show(PinConnectionStage.FOUND, preview = false)
            val active = view.composition
            player.show(PinConnectionStage.ENTER_PIN, preview = false)
            player.show(PinConnectionStage.CONNECTING, preview = false)
            player.show(PinConnectionStage.CONNECTED, preview = false)
            player.show(PinConnectionStage.SYNC_COMPLETED, preview = false)
            assertSame(active, view.composition)
        }
        compose.waitUntil(5000) {
            var settled = false
            compose.runOnUiThread {
                settled = view.composition === compositions[R.raw.glucometer_sync_to_ready] && !view.isAnimating && view.progress > 0.99f
            }
            settled
        }
        compose.runOnUiThread {
            assertFalse(pinReady)
            assertEquals(0, emptyFrames)
            assertEquals(0, view.repeatCount)
        }
    }

    @Test
    fun returningDuringTransitionReversesFromCurrentFrame() {
        compose.runOnUiThread {
            view.speed = 1f
            player.show(PinConnectionStage.FOUND, preview = false)
        }
        compose.waitUntil(5000) {
            var progressed = false
            compose.runOnUiThread { progressed = view.progress in 0.15f..0.7f }
            progressed
        }
        compose.runOnUiThread {
            val composition = view.composition
            val progress = view.progress
            player.show(PinConnectionStage.SEARCH, preview = false)
            assertSame(composition, view.composition)
            assertEquals(progress, view.progress, 0.001f)
            assertTrue(view.speed < 0f)
        }
        compose.waitUntil(5000) {
            var returned = false
            compose.runOnUiThread { returned = view.composition === compositions[R.raw.glucometer_bluetooth] && view.isAnimating }
            returned
        }
        compose.runOnUiThread {
            assertTrue(view.speed > 0f)
            assertEquals(0, emptyFrames)
        }
    }

    @Test
    fun waitingLoopPausesAndResumesWithoutRestartingProgress() {
        compose.waitUntil(5000) {
            var progressed = false
            compose.runOnUiThread { progressed = view.isAnimating && view.progress > 0.1f }
            progressed
        }
        var pausedProgress = 0f
        compose.runOnUiThread {
            player.pause()
            pausedProgress = view.progress
            assertFalse(view.isAnimating)
        }
        SystemClock.sleep(100)
        compose.runOnUiThread {
            assertEquals(pausedProgress, view.progress, 0.001f)
            player.resume()
            assertTrue(view.isAnimating)
        }
    }

    @Test
    fun PINInputBecomesReadyAfterZoomAndZoomDoesNotLoop() {
        compose.runOnUiThread { player.show(PinConnectionStage.FOUND, preview = true) }
        compose.runOnUiThread {
            player.show(PinConnectionStage.ENTER_PIN, preview = false)
            assertFalse(pinReady)
        }
        compose.waitUntil(5000) { var ready = false; compose.runOnUiThread { ready = pinReady }; ready }
        compose.runOnUiThread {
            assertSame(compositions[R.raw.glucometer_ready_to_up], view.composition)
            assertFalse(view.isAnimating)
            assertEquals(0, view.repeatCount)
        }
    }
}
