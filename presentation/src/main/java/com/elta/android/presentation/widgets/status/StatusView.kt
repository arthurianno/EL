package com.elta.android.presentation.widgets.status

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ViewConnectionStatusBinding
import io.reactivex.functions.Consumer

class StatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val hideViewCallback = Runnable {
        binding.statusExpandableLayout.collapse(true)
        prevStatus = null
        animator = null
    }

    private var animator: ObjectAnimator? = null
    private val evaluator = ArgbEvaluator()

    private var prevStatus: Status? = null
    private val binding: ViewConnectionStatusBinding by lazy {
        ViewConnectionStatusBinding.bind(this)
    }

    init {
        inflate(context, R.layout.view_connection_status, this)
    }

    fun setStatus(status: Status) = with(binding) {
        if (prevStatus == status) return

        if (prevStatus == null) {
            statusTextView.text = status.text
            statusBackgroundView.setBackgroundColor(status.color)
        } else {
            if (animator?.isRunning == true) {
                animator?.cancel()
            }
            prevStatus?.let {
                animator = getAnimator(it, status)
                animator?.start()
            }
            statusTextView.text = status.text
        }
        prevStatus = status
    }

    private fun setVisible(visibility: Visibility) {
        Log.d("MYTAG", "setVisible: $visibility")
        if (visibility.value) show()
        else hide(visibility.delay)
    }

    fun statusChanges(): Consumer<Status> = Consumer { setStatus(it) }

    fun visibleChanges(): Consumer<Visibility> = Consumer { setVisible(it) }

    private fun show() {
        removeCallbacks(hideViewCallback)
        binding.statusExpandableLayout.expand(true)
    }

    private fun hide(delay: Boolean) {
        if (delay) {
            removeCallbacks(hideViewCallback)
            postDelayed(hideViewCallback, HIDE_VIEW_DELAY)
        } else {
            removeCallbacks(hideViewCallback)
            hideViewCallback.run()
        }
    }

    private fun getAnimator(prev: Status, new: Status): ObjectAnimator =
        ObjectAnimator.ofInt(binding.statusBackgroundView, "backgroundColor", prev.color, new.color)
            .apply {
                setEvaluator(evaluator)
                duration = COLOR_ANIMATION_DURATION
            }

    companion object {
        private const val HIDE_VIEW_DELAY = 3000L // millis
        private const val COLOR_ANIMATION_DURATION = 700L // millis
    }
}
