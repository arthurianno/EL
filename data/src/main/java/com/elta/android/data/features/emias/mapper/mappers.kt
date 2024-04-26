package com.elta.android.data.features.emias.mapper

import com.elta.android.common.utils.CommonFormats.FORMAT_STANDARD_DATE
import com.elta.android.data.features.emias.dto.EmiasNetworkEntity
import com.elta.android.data.features.emias.dto.EmiasStatusResponse
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Emias.toNM(): EmiasNetworkEntity = EmiasNetworkEntity(
    oms = oms.filterNot { it.isWhitespace() },
    birthDate = SimpleDateFormat(FORMAT_STANDARD_DATE, Locale.getDefault()).format(birthdayDate)
)

fun EmiasStatusResponse.toDomain(): Pair<EmiasStatus, Emias?> {
    val status = if (linked) EmiasStatus.LINKED else EmiasStatus.UNLINKED
    val emias = credentials?.let {
        Emias(
            oms = credentials.oms,
            birthdayDate = try {
                SimpleDateFormat(
                    FORMAT_STANDARD_DATE,
                    Locale.getDefault()
                ).parse(credentials.birthDate)
            } catch (ex: Exception) {
                Date()
            } ?: Date()
        )
    }
    return status to emias
}
