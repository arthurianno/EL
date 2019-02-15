package com.elta.android.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.elta.android.presentation.R
import com.jakewharton.rxrelay2.PublishRelay
import com.nullgr.core.ui.extensions.children
import kotlinx.android.synthetic.main.layout_bottom_navigation_view.view.*
import java.util.function.Consumer

class BottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tabClicks = PublishRelay.create<Int>()
    private var selectedTab: BottomNavigationMenuItem? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_bottom_navigation_view, this, true)
        bottomNavigationView.children().forEach { child ->
            if (child is BottomNavigationMenuItem) {
                child.setOnClickListener { item ->
                    if (item.id != selectedTab?.id) {
                        selectedTab?.setItemSelected(false)
                        selectedTab = item as BottomNavigationMenuItem
                        selectedTab?.setItemSelected(true)
                        tabClicks.accept(item.id)
                    }
                }
            }
        }
    }

    fun select(tabId: Int) {
        if (tabId == selectedTab?.id) {
            return
        }
        bottomNavigationView.children().forEach { child ->
            if (child is BottomNavigationMenuItem && tabId == child.id) {
                selectedTab?.setItemSelected(false)
                selectedTab = child
                selectedTab?.setItemSelected(true)
            }
        }
    }

    fun selection(): Consumer<Int> = Consumer { id ->
        select(id)
    }

    fun tabClicks() = tabClicks.hide()
}