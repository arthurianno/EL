package com.elta.android.presentation.widgets.charts.daily

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel

class GlucoseDailyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var chartDataModel: ChartDataModel
        get() = dataModel()
        set(value) {
            _chartDataModel = value
            onDataModelChanged()
        }

    private var _chartDataModel: ChartDataModel? = null

    init {
        initDefault()
        initPaints()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        onAfterMeasure()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun initDefault() {

    }

    private fun initPaints() {

    }

    private fun onAfterMeasure() {

    }

    private fun onDataModelChanged() {
        invalidate()
    }

    private fun dataModel(): ChartDataModel =
        checkNotNull(_chartDataModel) { "Property `chartDataModel` did not initialized yet" }
}