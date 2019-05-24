package com.elta.android.presentation.features.diary.main.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.LayoutInflater
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.getBitmapFromView
import com.nullgr.core.date.toStringWithFormat
import java.util.Date
import java.util.TimeZone

class GlucoseSharingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val valueView: AppCompatTextView?
    private val dateView: AppCompatTextView?
    private val emojiView: AppCompatImageView?

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_share_glucose_event, this, true)

        valueView = findViewById(R.id.valueView)
        dateView = findViewById(R.id.dateView)
        emojiView = findViewById(R.id.emojiView)
    }

    fun generateBitmap(value: String, glucoseLevel: GlucoseLevel?): Bitmap {
        background = glucoseLevel.toBackground()
        valueView?.text = value
        emojiView?.setImageResource(glucoseLevel.toEmoji())

        val splittedDateArray =
            Date()
                .toStringWithFormat(GLUCOSE_SHARE_EVENT_DATE_FORMAT, TimeZone.getDefault())
                .split(" ")
        dateView?.text =
            context.getString(R.string.event_time_in_mask, splittedDateArray[0], splittedDateArray[1])

        return getBitmapFromView(GLUCOSE_SHARE_EVENT_PICTURE_SIZE)
    }

    private fun GlucoseLevel?.toBackground(): Drawable? =
        ContextCompat.getDrawable(
            context,
            when {
                this == null -> R.drawable.bg_gradient_green
                this == GlucoseLevel.HIGH -> R.drawable.bg_gradient_red
                this == GlucoseLevel.LOW -> R.drawable.bg_gradient_blue
                else -> R.drawable.bg_gradient_green
            }
        )

    private fun GlucoseLevel?.toEmoji(): Int =
        when {
            this == null -> R.drawable.ic_emj_ok
            this == GlucoseLevel.HIGH -> R.drawable.ic_emj_boom
            this == GlucoseLevel.LOW -> R.drawable.ic_emj_sad
            else -> R.drawable.ic_emj_ok
        }

    private companion object {
        private const val GLUCOSE_SHARE_EVENT_DATE_FORMAT = "dd.MM HH:mm"
        private const val GLUCOSE_SHARE_EVENT_PICTURE_SIZE = 360f
    }
}