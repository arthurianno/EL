package com.elta.android.common.utils

data class EltaMessageClient(
    @Volatile var isConsultantScreenActive: Boolean,
    @Volatile var isNewsScreenActive: Boolean
)
