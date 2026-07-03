package com.elta.android.presentation.features.statistic.report.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.elta.android.presentation.databinding.ItemReportTypeDropdownBinding

data class DropdownItem(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val iconColor: Int
)

class ReportTypeDropdownAdapter(
    context: Context,
    items: List<DropdownItem>
) : ArrayAdapter<DropdownItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemReportTypeDropdownBinding
        val view: View
        
        if (convertView == null) {
            binding = ItemReportTypeDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemReportTypeDropdownBinding
            view = convertView
        }

        getItem(position)?.let { item ->
            binding.titleView.text = item.title
            binding.subtitleView.text = item.subtitle
            binding.iconView.setImageResource(item.iconRes)
            binding.iconView.imageTintList = ColorStateList.valueOf(item.iconColor)
        }

        return view
    }
}
