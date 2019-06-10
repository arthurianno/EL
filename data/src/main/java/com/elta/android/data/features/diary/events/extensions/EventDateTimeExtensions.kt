package com.elta.android.data.features.diary.events.extensions

import com.elta.android.common.utils.toIsoDate
import com.elta.android.common.utils.toMillis
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

fun String.toAdditionMillis(): Long = toIsoDate().toLocalDate().toMillis(ZoneOffset.UTC)

fun LocalDateTime.toQueryMillis(): Long = toMillis(ZoneOffset.UTC)