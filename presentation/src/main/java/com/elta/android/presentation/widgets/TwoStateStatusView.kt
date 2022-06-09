package com.elta.android.presentation.widgets

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ViewConnectionStatusBinding
import io.reactivex.functions.Consumer

class TwoStateStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val hideViewCallback = Runnable {
        binding.statusExpandableLayout.collapse(true)
    }

    private var colorAnimator: ValueAnimator

    private var state1Text: String? = null
    private var state2Text: String? = null

    private var state1Color: Int = Color.BLUE
    private var state2Color: Int = Color.GREEN
    private val binding: ViewConnectionStatusBinding by lazy {
        ViewConnectionStatusBinding.bind(this)
    }

    init {
        inflate(context, R.layout.view_connection_status, this)
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.TwoStateStatusView, 0, 0)

            state1Text = array.getString(R.styleable.TwoStateStatusView_state1_text)
            state2Text = array.getString(R.styleable.TwoStateStatusView_state2_text)

            state1Color = array.getColor(R.styleable.TwoStateStatusView_state1_color, Color.BLUE)
            state2Color = array.getColor(R.styleable.TwoStateStatusView_state2_color, Color.GREEN)

            array.recycle()
        }
        colorAnimator = ObjectAnimator.ofInt(
            binding.statusBackgroundView,
            "backgroundColor",
            state1Color,
            state2Color
        ).apply {
            setEvaluator(ArgbEvaluator())
            duration = COLOR_ANIMATION_DURATION
        }
    }

    fun changeState() = Consumer<Boolean> {
        setState(it)
    }

    fun setState(isChanged: Boolean) {
        if (isChanged) drawState2()
        else drawState1()
    }

    private fun drawState1() = with(binding) {
        if (colorAnimator.isRunning)
            colorAnimator.cancel()
        removeCallbacks(hideViewCallback)
        statusBackgroundView.setBackgroundColor(state1Color)
        statusTextView.text = state1Text
        statusExpandableLayout.expand(true)
    }

    private fun drawState2() = with(binding) {
        statusTextView.text = state2Text
        colorAnimator.start()
        postDelayed(hideViewCallback, HIDE_VIEW_DELAY)
    }

    companion object {
        private const val HIDE_VIEW_DELAY = 3000L // millis
        private const val COLOR_ANIMATION_DURATION = 700L // millis
    }
}
