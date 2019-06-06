package com.elta.android.presentation.widgets.spinner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.PopupWindow
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.elta.android.presentation.widgets.spinner.adapter.SpinnerDelegatesFactory
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import com.jakewharton.rxrelay2.PublishRelay
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.RxDiffCalculator
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.schedulers.ComputationToMainSchedulersFacade
import com.nullgr.core.ui.extensions.toggleView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_spinner.view.*

@Suppress("MagicNumber")
class SpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), SelectItemListener {

    private var popupWindow: PopupWindow
    private var arrowAnimator: ObjectAnimator? = null
    private var adapter: DynamicAdapter
    private val popupContainerView: RecyclerView
    private val isArrowVisible: Boolean
    private val spinnerClicks = PublishRelay.create<ListItem>()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_spinner, this, true)
        popupContainerView = LayoutInflater.from(context).inflate(R.layout.layout_popup_list, null) as RecyclerView
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpinnerView)

        val diffCalculator = RxDiffCalculator(ComputationToMainSchedulersFacade())
        val delegatesFactory = SpinnerDelegatesFactory(ResourceProvider(context), this)
        adapter = DynamicAdapter(delegatesFactory, diffCalculator)

        popupWindow = PopupWindow(context)
        popupWindow.contentView = popupContainerView
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = ELEVATION
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.setOnDismissListener {
            animateArrow(false)
        }

        popupContainerView.adapter = adapter
        popupContainerView.layoutManager = FixedLinearLayoutManager(context)

        isArrowVisible = typedArray.getBoolean(R.styleable.SpinnerView_showArrow, true)
        spinnerArrowView.toggleView(isArrowVisible)
        val topicDrawableResId = typedArray.getResourceId(R.styleable.SpinnerView_topicDrawable, R.drawable.ic_calendar)
        spinnerTopicIconView.setImageDrawable(ContextCompat.getDrawable(context, topicDrawableResId))

        typedArray.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (popupWindow.isShowing) dismissDropDown() else showDropDown()
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        arrowAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onItemSelected(item: SpinnerItem, title: String?) {
        if (popupWindow.isShowing) dismissDropDown()
        spinnerClicks.accept(item)
        title?.let { spinnerTitleView.text = it }
    }

    fun spinnerClicks(): Observable<ListItem> = spinnerClicks.hide()

    fun attachDataList(list: List<SpinnerItem>) {
        adapter.updateData(list, false)
    }

    fun setTitle(title: String) {
        spinnerTitleView.text = title
    }

    private fun showDropDown() {
        if (isArrowVisible) animateArrow(true)
        measurePopUpDimension()
        popupWindow.showAsDropDown(spinnerTitleView, 0, -1 * height)
    }

    private fun measurePopUpDimension() {
        val widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.UNSPECIFIED)
        val heightSpec = MeasureSpec.makeMeasureSpec(getPopUpHeight(), MeasureSpec.AT_MOST)
        popupContainerView.measure(widthSpec, heightSpec)
        popupWindow.width = spinnerTitleView.measuredWidth
        popupWindow.height = popupContainerView.measuredHeight
    }

    private fun getPopUpHeight() = context.resources.displayMetrics.heightPixels - measuredHeight

    private fun dismissDropDown() {
        if (isArrowVisible) animateArrow(false)
        popupWindow.dismiss()
    }

    private fun animateArrow(shouldRotateUp: Boolean) {
        val start = if (shouldRotateUp) 0 else MAX_LEVEL
        val end = if (shouldRotateUp) MAX_LEVEL else 0
        arrowAnimator = ObjectAnimator.ofInt(spinnerArrowView.drawable, "level", start, end)
        arrowAnimator?.interpolator = LinearOutSlowInInterpolator()
        arrowAnimator?.start()
    }

    private companion object {
        const val MAX_LEVEL = 10000
        const val ELEVATION = 16F
    }
}