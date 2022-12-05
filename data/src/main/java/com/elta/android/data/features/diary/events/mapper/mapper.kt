package com.elta.android.data.features.diary.events.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.model.ProductResponse

private const val ELEMENT_SEPARATOR = "~"
private const val FIELD_SEPARATOR = "|"

internal fun String?.toProductsList(): List<ProductResponse>? =
    runCatching {
        this?.let {
            it.split(ELEMENT_SEPARATOR).map { element ->
                element.split(FIELD_SEPARATOR).run {
                    ProductResponse(
                        id = component1(),
                        name = component2(),
                        type = component3(),
                        servingAmount = component4().toDouble(),
                        servingId = component5(),
                        servingName = get(5),
                        breadUnits = get(6).toDouble()
                    )
                }
            }
        }
    }.getOrNull()

internal fun List<ProductResponse>?.toCache(): String? =
    this?.joinToString(separator = ELEMENT_SEPARATOR) {
        it.run {
            id + FIELD_SEPARATOR + name + FIELD_SEPARATOR + type + FIELD_SEPARATOR +
                servingAmount.toString() + FIELD_SEPARATOR + servingId + FIELD_SEPARATOR +
                servingName + FIELD_SEPARATOR + breadUnits
        }
    }
