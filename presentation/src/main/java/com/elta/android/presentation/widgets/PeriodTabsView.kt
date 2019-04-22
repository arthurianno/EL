package com.elta.android.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.elta.android.presentation.R
import com.jakewharton.rxrelay2.PublishRelay
import com.nullgr.core.ui.extensions.children
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.layout_period_tabs.view.*

class PeriodTabsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val selectedId
        get() = selectedTab?.id

    private val tabClicks = PublishRelay.create<Int>()
    private var selectedTab: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_period_tabs, this, true)
        periodTabsView.children().forEach { child ->
            if (child is TextView) {
                child.setOnClickListener { item ->
                    if (item.id != selectedTab?.id) {
                        selectedTab?.isSelected = false
                        selectedTab = item as TextView
                        selectedTab?.isSelected = true
                        tabClicks.accept(item.id)
                    }
                }
            }
        }
    }

    fun selection(): Consumer<Int> = Consumer { id ->
        select(id)
    }

    fun tabClicks(): Observable<Int> = tabClicks.hide()

    private fun select(tabId: Int) {
        if (tabId == selectedTab?.id) {
            return
        }
        periodTabsView.children().forEach { child ->
            if (child is TextView && tabId == child.id) {
                selectedTab?.isSelected = false
                selectedTab = child
                selectedTab?.isSelected = true
            }
        }
    }
}