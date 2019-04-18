package com.elta.android.presentation.widgets.spinner

import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem

interface SelectItemListener {

    fun onItemSelected(item: SpinnerItem, title: String?)
}