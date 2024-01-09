package com.elta.android.data.features.user.dto

enum class DiabetesTypeNetworkEntity {
    FIRST, SECOND, SECOND_TABLETS;

    companion object {
        fun getByItemName(itemName: String?): DiabetesTypeNetworkEntity? {
            return values().firstOrNull { itemName == it.name }
        }
    }
}
