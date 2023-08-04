package com.elta.android.presentation.features.main.events.chooser.models

import android.os.Parcelable
import com.elta.android.domain.features.diary.events.model.InsulinType
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChooserInsulin(
    val previousName: String,
    val drug: String,
    val type: InsulinType
) : Parcelable
