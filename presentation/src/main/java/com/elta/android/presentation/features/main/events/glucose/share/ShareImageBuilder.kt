package com.elta.android.presentation.features.main.events.glucose.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.getBitmapFromView
import kotlinx.android.synthetic.main.layout_share_glucose_event.view.*
import javax.inject.Inject

class ShareImageBuilder @Inject constructor(val context: Context) {

    private val inflatedView by lazy {
        LayoutInflater.from(context).inflate(R.layout.layout_share_glucose_event, null, false)
    }

    fun createBitmap(event: Event, glucoseLevelSettings: GlucoseLevelSettings): Bitmap {
        with(inflatedView) {
            val glucoseLevel = event.glucoseLevel(glucoseLevelSettings)
            background = glucoseLevel.toBackground()
            valueView?.text = NumberFormatter.format(event.value ?: 0.0)
            emojiView?.setImageResource(glucoseLevel.toEmoji())
            dateView?.text = event.additionTime.toStringWithFormat(GLUCOSE_SHARE_EVENT_DATE_FORMAT)
        }
        return inflatedView.getBitmapFromView(GLUCOSE_SHARE_EVENT_PICTURE_SIZE)
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
        private const val GLUCOSE_SHARE_EVENT_DATE_FORMAT = "dd.MM в HH:mm"
        private const val GLUCOSE_SHARE_EVENT_PICTURE_SIZE = 360f
    }
}
