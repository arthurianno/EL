package com.elta.android.presentation.features.onboaring.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_onboarding_gender.*

class OnBoardingGenderDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_onboarding_gender
    override val itemType: Any = OnBoardingGenderItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                val listener = View.OnClickListener { view ->
                    val newGender = when (view.id) {
                        R.id.genderMaleView -> Gender.MALE
                        else -> Gender.FEMALE
                    }

                    withAdapterPosition<OnBoardingGenderItem> { _, item, _ ->
                        switchGenders(item, newGender)
                        setGendersState(genderMaleView, genderFemaleView, item.gender)
                        bus.event(Events.OnBoardingPageSelected(item))
                    }
                }

                genderMaleView.setOnClickListener(listener)
                genderFemaleView.setOnClickListener(listener)
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as OnBoardingGenderItem

        with(holder as ViewHolder) {
            setGendersState(genderMaleView, genderFemaleView, item.gender)
        }
    }

    private fun setGendersState(genderMale: View, genderFemale: View, gender: Gender?) {
        genderMale.isSelected = Gender.MALE == gender
        genderFemale.isSelected = Gender.FEMALE == gender
    }

    private fun switchGenders(item: OnBoardingGenderItem, newGender: Gender) {
        if (newGender == item.gender) {
            item.gender = null
        } else {
            item.gender = newGender
        }
    }
}
