package com.elta.android.data.features.diary.insulin.dto


import com.google.gson.annotations.SerializedName

data class MedicinesNetworkResponse(
    @SerializedName("insulinMedicamentsByType")
    val insulinMedicamentsByType: Map<String, List<Item>>,
    @SerializedName("bolusInsulinTypes")
    val bolusInsulinTypes: List<String>,
    @SerializedName("basalInsulinTypes")
    val basalInsulinTypes: List<String>
) {

    data class Item(
        @SerializedName("id")
        val id: Int,
        @SerializedName("insulinType")
        val insulinType: InsulinType,
        @SerializedName("name")
        val name: String
    )

    data class InsulinType(
        @SerializedName("code")
        val code: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String
    )
}