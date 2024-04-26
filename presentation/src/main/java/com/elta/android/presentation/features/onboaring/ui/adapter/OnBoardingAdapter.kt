package com.elta.android.presentation.features.onboaring.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemOnboardingDiabetesTypesBinding
import com.elta.android.presentation.databinding.ItemOnboardingEmiasProfileBinding
import com.elta.android.presentation.databinding.ItemOnboardingGenderBinding
import com.elta.android.presentation.databinding.ItemOnboardingGlucoseFormatBinding
import com.elta.android.presentation.databinding.ItemOnboardingWeightBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.holder.OnBoardingDiabetesViewHolder
import com.elta.android.presentation.features.onboaring.ui.adapter.holder.OnBoardingEmiasProfileViewHolder
import com.elta.android.presentation.features.onboaring.ui.adapter.holder.OnBoardingGenderViewHolder
import com.elta.android.presentation.features.onboaring.ui.adapter.holder.OnBoardingGlucoseFormatViewHolder
import com.elta.android.presentation.features.onboaring.ui.adapter.holder.OnBoardingWeightViewHolder
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingEmiasProfileItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGlucoseFormatItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class OnBoardingAdapter @Inject constructor(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            OnBoardingEmiasProfileItem::class.java.hashCode() -> OnBoardingEmiasProfileViewHolder(
                binding = ItemOnboardingEmiasProfileBinding.inflate(inflater, parent, false),
                bus = bus,
                resources = resources
            )

            OnBoardingGenderItem::class.java.hashCode() -> OnBoardingGenderViewHolder(
                binding = ItemOnboardingGenderBinding.inflate(inflater, parent, false),
                bus = bus
            )

            OnBoardingWeightItem::class.java.hashCode() -> OnBoardingWeightViewHolder(
                binding = ItemOnboardingWeightBinding.inflate(inflater, parent, false),
                bus = bus
            )

            OnBoardingDiabetesItem::class.java.hashCode() -> OnBoardingDiabetesViewHolder(
                binding = ItemOnboardingDiabetesTypesBinding.inflate(inflater, parent, false),
                bus = bus,
                context = parent.context
            )

            OnBoardingGlucoseFormatItem::class.java.hashCode() -> OnBoardingGlucoseFormatViewHolder(
                binding = ItemOnboardingGlucoseFormatBinding.inflate(inflater, parent, false),
                bus = bus
            )

            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
