package com.elta.android.data.features.diary.events.dto

import com.elta.android.domain.features.diary.events.model.GlucoseInputType

enum class GlucoseInputTypeDto {
    MANUAL, AUTO, GOOGLE_FIT;
    companion object {
        fun GlucoseInputType.toDto(): GlucoseInputTypeDto{
            return when(this){
                GlucoseInputType.MANUAL -> MANUAL
                GlucoseInputType.AUTO -> AUTO
                GlucoseInputType.GOOGLE_FIT -> AUTO
            }
        }
    }
}
