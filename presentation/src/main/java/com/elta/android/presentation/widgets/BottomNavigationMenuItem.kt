package com.elta.android.presentation.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elta.android.presentation.R
import kotlinx.android.synthetic.main.view_bottom_navigation_menu_item.view.*

class BottomNavigationMenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var icon: Drawable? = null
    private var title: String? = null
    private var selectedColor: Int? = null
    private var normalColor: Int? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_bottom_navigation_menu_item, this, true)
        orientation = LinearLayout.VERTICAL
        readAttrs(attrs)
        bindItem()
    }

    fun setItemSelected(isSelected: Boolean) {
        bindColors(when (isSelected) {
            true -> checkNotNull(selectedColor)
            else -> checkNotNull(normalColor)
        })
    }

    private fun readAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenuItem, 0, 0)
            icon = array.getDrawable(R.styleable.BottomNavigationMenuItem_bnm_icon)
            title = array.getString(R.styleable.BottomNavigationMenuItem_bnm_title)
            selectedColor = array.getColor(R.styleable.BottomNavigationMenuItem_bnm_selected_color, -1)
            normalColor = array.getColor(R.styleable.BottomNavigationMenuItem_bnm_normal_color, -1)
            array.recycle()
        }
    }

    private fun bindItem() {
        menuIconView.setImageDrawable(checkNotNull(icon))
        menuTitleView.text = checkNotNull(title)
        bindColors(normalColor)
    }

    private fun bindColors(color: Int?) {
        menuTitleView.setTextColor(checkNotNull(color))
        menuIconView.imageTintList = ColorStateList.valueOf(checkNotNull(color))
    }
}