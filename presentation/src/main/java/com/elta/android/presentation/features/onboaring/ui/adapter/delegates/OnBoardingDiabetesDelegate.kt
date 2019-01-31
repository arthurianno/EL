package com.elta.android.presentation.features.onboaring.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.utils.toString
import com.elta.android.presentation.utils.withAdapterPosition
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.children
import kotlinx.android.synthetic.main.item_onboarding_diabetes_types.*

class OnBoardingDiabetesDelegate(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_onboarding_diabetes_types
    override val itemType: Any = OnBoardingDiabetesItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                val listener = View.OnClickListener { view ->
                    val newType = view.getTag(TYPE_TAG) as Diabetes
                    withAdapterPosition<OnBoardingDiabetesItem> { _, item, _ ->
                        switchDiabetesType(item, newType)
                        view.isSelected = newType == item.type
                        bus.click(Clicks.DiabetesTypeSelected(item.type))
                    }
                }

                typesView.children().forEach { child ->
                    child.setOnClickListener(listener)
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as OnBoardingDiabetesItem

        with(holder as ViewHolder) {
            item.types.forEachIndexed { index, type ->
                val child = (typesView.getChildAt(index) as TextView)
                child.text = type.toString(resources)
                child.setTag(TYPE_TAG, type)
                child.isSelected = type == item.type
            }
        }
    }

    private fun switchDiabetesType(item: OnBoardingDiabetesItem, newType: Diabetes) {
        if (newType == item.type) {
            item.type = null
        } else {
            item.type = newType
        }
    }

    private companion object {
        const val TYPE_TAG = 100
    }
}