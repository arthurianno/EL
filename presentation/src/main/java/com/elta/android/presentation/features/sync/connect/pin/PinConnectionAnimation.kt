package com.elta.android.presentation.features.sync.connect.pin

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.elta.android.presentation.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

internal data class PinAnimation(@field:RawRes val resource: Int, val repeat: Boolean = false, val play: Boolean = false)

internal fun PinConnectionStage.steadyAnimation(): PinAnimation = when (this) {
    PinConnectionStage.HOW_TO_CONNECT -> PinAnimation(R.raw.glucometer_pincode, play = true)
    PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING -> PinAnimation(R.raw.glucometer_bluetooth, repeat = true)
    PinConnectionStage.FOUND -> PinAnimation(R.raw.glucometer_bluetooth_to_ready)
    PinConnectionStage.ENTER_PIN -> PinAnimation(R.raw.glucometer_ready_to_up)
    PinConnectionStage.CONNECTED -> PinAnimation(R.raw.glucometer_sync, repeat = true)
    PinConnectionStage.SYNC_COMPLETED -> PinAnimation(R.raw.glucometer_sync_to_ready)
    PinConnectionStage.CONNECT_ERROR, PinConnectionStage.NOT_FOUND -> PinAnimation(R.raw.glucometer_bluetooth_to_error)
    PinConnectionStage.SYNC_ERROR -> PinAnimation(R.raw.glucometer_sync_to_error)
}

@RawRes
internal fun pinTransitionAnimation(from: PinConnectionStage?, to: PinConnectionStage): Int? = when {
    from == PinConnectionStage.HOW_TO_CONNECT && to == PinConnectionStage.SEARCH -> R.raw.glucometer_to_bluetooth
    from == PinConnectionStage.SEARCH && to == PinConnectionStage.FOUND -> R.raw.glucometer_bluetooth_to_ready
    from == PinConnectionStage.FOUND && to == PinConnectionStage.ENTER_PIN -> R.raw.glucometer_ready_to_up
    from == PinConnectionStage.ENTER_PIN && to == PinConnectionStage.CONNECTING -> R.raw.glucometer_up_to_bluetooth
    from == PinConnectionStage.CONNECTING && to == PinConnectionStage.CONNECTED -> R.raw.glucometer_bluetooth_to_sync
    from == PinConnectionStage.CONNECTED && to == PinConnectionStage.SYNC_COMPLETED -> R.raw.glucometer_sync_to_ready
    from == PinConnectionStage.CONNECTED && to == PinConnectionStage.SYNC_ERROR -> R.raw.glucometer_sync_to_error
    from in setOf(PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING) &&
        to in setOf(PinConnectionStage.NOT_FOUND, PinConnectionStage.CONNECT_ERROR) -> R.raw.glucometer_bluetooth_to_error
    from in setOf(PinConnectionStage.NOT_FOUND, PinConnectionStage.CONNECT_ERROR) &&
        to in setOf(PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING) -> R.raw.glucometer_error_to_bluetooth
    from == PinConnectionStage.SYNC_ERROR && to == PinConnectionStage.CONNECTED -> R.raw.glucometer_error_to_sync
    else -> null
}

internal val pinAnimationResources = listOf(
    R.raw.glucometer_pincode,
    R.raw.glucometer_bluetooth,
    R.raw.glucometer_sync,
    R.raw.glucometer_to_bluetooth,
    R.raw.glucometer_bluetooth_to_ready,
    R.raw.glucometer_bluetooth_to_error,
    R.raw.glucometer_bluetooth_to_sync,
    R.raw.glucometer_ready_to_up,
    R.raw.glucometer_up_to_bluetooth,
    R.raw.glucometer_sync_to_ready,
    R.raw.glucometer_sync_to_error,
    R.raw.glucometer_error_to_bluetooth,
    R.raw.glucometer_error_to_sync
)

internal fun loadPinComposition(
    context: Context,
    @RawRes resource: Int,
    bitmaps: MutableMap<String, Bitmap>
): LottieComposition {
    val result = LottieCompositionFactory.fromRawResSync(context, resource)
    val composition = result.value ?: throw checkNotNull(result.exception)
    composition.images.values.forEach { asset ->
        // The JSON files contain the same meter images. Decode them off the UI
        // thread and share the bitmaps, rather than uploading a fresh copy at each switch.
        val name = asset.fileName
        val bitmap = bitmaps[name] ?: asset.bitmap ?: run {
            require(name.startsWith("data:") && name.contains("base64,"))
            val bytes = Base64.decode(name.substringAfter("base64,"), Base64.DEFAULT)
            checkNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        }
        bitmaps[name] = bitmap
        asset.bitmap = bitmap
    }
    return composition
}

internal class PinAnimationPlayer(
    val view: LottieAnimationView,
    private val onPinReady: (Boolean) -> Unit = {}
) {
    private val compositions = mutableMapOf<Int, LottieComposition>()
    private var requestedStage: PinConnectionStage? = null
    private var displayedStage: PinConnectionStage? = null
    private var transitionTarget: PinConnectionStage? = null
    private var transitionSource: PinConnectionStage? = null
    private var currentAnimation: PinAnimation? = null
    private var preview = false
    private var started = false
    private var animationPending = false
    private var suppressEnd = false
    private var disposed = false

    private val listener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            if (suppressEnd || disposed) return
            animationPending = false
            transitionTarget?.let {
                displayedStage = it
                transitionTarget = null
                transitionSource = null
                advance()
            }
        }
    }

    init {
        view.addAnimatorListener(listener)
    }

    fun prepare(@RawRes resource: Int, composition: LottieComposition) {
        if (disposed) return
        compositions[resource] = composition
        advance()
    }

    fun show(next: PinConnectionStage, preview: Boolean) {
        if (disposed || (requestedStage == next && this.preview == preview)) return
        requestedStage = next
        this.preview = preview
        onPinReady(false)
        // Back/reset should take effect immediately. Operation results otherwise
        // replace only the pending target, allowing an active transition to finish.
        if (preview || next == PinConnectionStage.HOW_TO_CONNECT) {
            cancelPlayback()
            transitionTarget = null
            transitionSource = null
            displayedStage = null
        } else if (transitionTarget != null && transitionSource == next) {
            reverseTransition()
        }
        advance()
    }

    private fun advance() {
        if (disposed || transitionTarget != null) return
        val target = requestedStage ?: return
        val transition = if (preview || displayedStage == target) null else pinTransitionAnimation(displayedStage, target)
        val animation = if (transition == null) target.steadyAnimation() else PinAnimation(transition, play = true)
        val composition = compositions[animation.resource] ?: return
        if (transition == null && displayedStage == target && currentAnimation == animation) return
        if (transition != null) {
            transitionSource = displayedStage
            transitionTarget = target
        } else {
            displayedStage = target
        }
        applyComposition(composition, animation)
        if (transition == null) onPinReady(target == PinConnectionStage.ENTER_PIN)
    }

    private fun reverseTransition() {
        val source = transitionSource ?: return
        val atSource = if (view.speed >= 0f) view.progress <= 0.001f else view.progress >= 0.999f
        if (atSource) {
            cancelPlayback()
            displayedStage = source
            transitionSource = null
            transitionTarget = null
            return
        }
        // Returning during a zoom (back or incorrect PIN) reverses the same
        // composition at its current frame instead of snapping to another pose.
        transitionSource = transitionTarget
        transitionTarget = source
        view.speed = -view.speed
        if (started) view.resumeAnimation()
    }

    private fun applyComposition(composition: LottieComposition, animation: PinAnimation) {
        cancelPlayback()
        suppressEnd = true
        currentAnimation = animation
        view.speed = abs(view.speed)
        // setAnimation(resource) first clears the visible composition and starts
        // an asynchronous load. A prepared composition can be swapped atomically.
        if (view.composition !== composition) view.setComposition(composition)
        view.repeatCount = if (!preview && animation.repeat) LottieDrawable.INFINITE else 0
        animationPending = !preview && (animation.play || animation.repeat)
        if (animationPending) {
            view.progress = 0f
        } else {
            view.progress = if (animation.repeat) 0.5f else 1f
        }
        suppressEnd = false
        if (animationPending && started) view.playAnimation()
    }

    private fun cancelPlayback() {
        suppressEnd = true
        view.cancelAnimation()
        suppressEnd = false
        animationPending = false
    }

    fun pause() {
        started = false
        view.pauseAnimation()
    }

    fun resume() {
        started = true
        if (animationPending && !view.isAnimating) view.resumeAnimation()
    }

    fun close() {
        disposed = true
        view.removeAnimatorListener(listener)
        view.cancelAnimation()
        compositions.clear()
        onPinReady(false)
    }
}

@Composable
internal fun PinConnectionAnimation(
    stage: PinConnectionStage,
    modifier: Modifier = Modifier,
    onPinReady: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val readiness = rememberUpdatedState(onPinReady)
    val latestStage = rememberUpdatedState(stage)
    val player = remember(context) { PinAnimationPlayer(LottieAnimationView(context)) { readiness.value(it) } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val preview = LocalInspectionMode.current
    LaunchedEffect(player) {
        val resources = listOf(latestStage.value.steadyAnimation().resource) + pinAnimationResources
        withContext(Dispatchers.Default) {
            val bitmaps = mutableMapOf<String, Bitmap>()
            for (resource in resources.distinct()) {
                ensureActive()
                val composition = try {
                    loadPinComposition(context, resource, bitmaps)
                } catch (error: Exception) {
                    Timber.e(error, "Cannot prepare glucometer animation %s", resource)
                    continue
                }
                withContext(Dispatchers.Main.immediate) { player.prepare(resource, composition) }
            }
        }
    }
    DisposableEffect(player, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> player.pause()
                Lifecycle.Event.ON_START -> player.resume()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) player.resume()
        onDispose {
            lifecycle.removeObserver(observer)
            player.close()
        }
    }
    AndroidView(factory = { player.view }, update = { player.show(stage, preview) }, modifier = modifier)
}
