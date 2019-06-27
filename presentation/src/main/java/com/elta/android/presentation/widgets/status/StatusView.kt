package com.elta.android.presentation.widgets.status

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.elta.android.presentation.R
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.view_connection_status.view.*

class StatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val hideViewCallback = Runnable {
        statusExpandableLayout.collapse(true)
        prevStatus = null
        animator = null
    }

    private var animator: ObjectAnimator? = null
    private val evaluator = ArgbEvaluator()

    private var prevStatus: Status? = null

    init {
        inflate(context, R.layout.view_connection_status, this)
    }

    fun setStatus(status: Status) {
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

    fun setVisible(visible: Boolean) {
        if (visible) show()
        else hide()
    }

    fun statusChanges(): Consumer<Status> = Consumer { setStatus(it) }

    fun visibleChanges(): Consumer<Boolean> = Consumer { setVisible(it) }

    private inline fun show() {
        removeCallbacks(hideViewCallback)
        statusExpandableLayout.expand(true)
    }

    private inline fun hide() {
        removeCallbacks(hideViewCallback)
        postDelayed(hideViewCallback, HIDE_VIEW_DELAY)
    }

    private inline fun getAnimator(prev: Status, new: Status): ObjectAnimator =
        ObjectAnimator.ofInt(statusBackgroundView, "backgroundColor", prev.color, new.color).apply {
            setEvaluator(evaluator)
            duration = COLOR_ANIMATION_DURATION
        }

    companion object {
        private const val HIDE_VIEW_DELAY = 3000L // millis
        private const val COLOR_ANIMATION_DURATION = 700L // millis
    }
}
