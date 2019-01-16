package com.nullgr.android.presentation.widgets

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.LinearLayout
import com.nullgr.android.presentation.R
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.view_connection_status.view.*

class ConnectionStatusView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val hideViewCallback = Runnable {
        connectionStatusExpandableLayout.collapse(true)
    }

    private var connectedStateColorAnimator: ValueAnimator
    private val colorDisconnected by lazy { ContextCompat.getColor(context, R.color.color_background_disconnected) }
    private val colorConnected by lazy { ContextCompat.getColor(context, R.color.color_background_connected) }

    init {
        inflate(context, R.layout.view_connection_status, this)
        connectedStateColorAnimator = ObjectAnimator.ofInt(
            connectionStatusBackgroundView,
            "backgroundColor",
            colorDisconnected,
            colorConnected
        ).apply {
            setEvaluator(ArgbEvaluator())
            duration = COLOR_ANIMATION_DURATION
        }
    }

    fun connectionState() = Consumer<Boolean> {
        setConnected(it)
    }

    fun setConnected(isConnected: Boolean) {
        if (isConnected) drawConnected()
        else drawDisconnected()
    }

    private fun drawDisconnected() {
        if (connectedStateColorAnimator.isRunning)
            connectedStateColorAnimator.cancel()
        removeCallbacks(hideViewCallback)
        connectionStatusBackgroundView.setBackgroundColor(
            ContextCompat.getColor(context, R.color.color_background_disconnected)
        )
        connectionStatusTextView.text = resources.getString(R.string.title_connection_lost)
        connectionStatusExpandableLayout.expand(true)
    }

    private fun drawConnected() {
        connectionStatusTextView.text = resources.getString(R.string.title_connection_found)
        connectedStateColorAnimator.start()
        postDelayed(hideViewCallback, HIDE_VIEW_DELAY)
    }

    companion object {
        private const val HIDE_VIEW_DELAY = 3000L //millis
        private const val COLOR_ANIMATION_DURATION = 700L //millis
    }
}
