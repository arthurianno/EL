package com.elta.android.presentation.widgets.datePicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.databinding.ItemDatePickerBinding
import com.elta.android.presentation.widgets.datePicker.model.DatePickerItem

class DateAdapter : ListAdapter<DatePickerItem, DateViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding =
            ItemDatePickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DateViewHolder(private val binding: ItemDatePickerBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DatePickerItem) {
        binding.run {
            itemView.isSelected = false
            dayOfWeekTitleView.setText(item.dayOfWeekResId)
            dayOfMonthTitleView.text = item.dayOfMonth.toString()
            dayOfWeekTitleView.isVisible = item.isAvailable
            dayOfMonthTitleView.isVisible = item.isAvailable
        }
    }

    private fun Boolean.toAlpha() =
        if (this) 1f else 0.4f
}

private val diffCallback = object : DiffUtil.ItemCallback<DatePickerItem>() {
    override fun areItemsTheSame(oldItem: DatePickerItem, newItem: DatePickerItem): Boolean =
        oldItem.date == newItem.date

    override fun areContentsTheSame(oldItem: DatePickerItem, newItem: DatePickerItem): Boolean =
        oldItem == newItem
}
