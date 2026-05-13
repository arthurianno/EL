package com.elta.android.data.features.diary.medicines.dto


import com.google.gson.annotations.SerializedName

data class InsulinMedicamentsNetworkResponse(
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
        val name: String,
        @SerializedName("deleted")
        val deleted: Boolean,
        @SerializedName("isOther")
        val isOther: Boolean = false
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
