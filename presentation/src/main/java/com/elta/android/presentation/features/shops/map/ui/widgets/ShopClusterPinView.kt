package com.elta.android.presentation.features.shops.map.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.elta.android.presentation.R
import com.nullgr.core.font.getTypeface
import com.nullgr.core.ui.extensions.dpToPx
import com.nullgr.core.ui.extensions.spToPx

class ShopClusterPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val primaryBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val countTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @Px
    private var bgSecondaryMargin = 0
    @Px
    private var textPadding = 0
    @Px
    private var sideSize = 0
    @Px
    private var textHeight = 0
    private var text = ""
    private val bounds = Rect()

    init {
        initPaints()
        initDefaults()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            val center = sideSize / 2f
            val radius = center + bgSecondaryMargin + textPadding
            it.drawCircle(radius, radius, radius, primaryBackgroundPaint)
            it.drawCircle(radius, radius, radius - bgSecondaryMargin, secondaryBackgroundPaint)
            it.drawText(text, radius, radius + textHeight / 2, countTextPaint)
        }
    }

    fun setText(text: String) {
        this.text = text
        computeLargestSide(text)
        makeMeasure()
        invalidate()
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    private fun makeMeasure() {
        val fullViewSize = sideSize + bgSecondaryMargin * 2 + textPadding * 2
        val measureSpec = View.MeasureSpec.makeMeasureSpec(fullViewSize, MeasureSpec.EXACTLY)
        measure(measureSpec, measureSpec)
    }

    private fun computeLargestSide(text: String): Int {
        countTextPaint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = bounds.right - bounds.left
        textHeight = bounds.bottom - bounds.top
        sideSize = Math.max(textHeight, textWidth)
        return sideSize
    }

    private fun initDefaults() {
        bgSecondaryMargin = BG_SECONDARY_MARGIN.dpToPx(context).toInt()
        textPadding = TEXT_PADDING.dpToPx(context).toInt()
    }

    private fun initPaints() {
        primaryBackgroundPaint.apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.cluster_background_primary)
        }
        secondaryBackgroundPaint.apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.cluster_background_secondary)
        }
        countTextPaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = TEXT_SIZE.spToPx(context)
            color = Color.WHITE
            typeface = context.getTypeface(TYPEFACE_MEDIUM)
        }
    }

    private companion object {
        private const val BG_SECONDARY_MARGIN = 3f // dp
        private const val TEXT_PADDING = 8f // dp
        private const val TEXT_SIZE = 15f // sp
        private const val TYPEFACE_MEDIUM = "roboto_medium.ttf"
    }
}