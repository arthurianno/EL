package com.elta.android.presentation.features.onboaring.ui.adapter.items

data class OnBoardingEmiasProfileItem (
    override val title: String
) : OnBoardingItem {

    override val data: Any?
        get() = EmiasUi(
            oms = oms,
            omsIsValid = omsIsValid,
            birthday = birthday,
            birthdayIsValid = birthdayIsValid
        )

    var oms: String? = null
    var omsIsValid: Boolean = false

    var birthday: String? = null
    var birthdayIsValid: Boolean = false

}

data class EmiasUi(
    val oms: String? = null,
    val omsIsValid: Boolean = false,
    val birthday: String? = null,
    val birthdayIsValid: Boolean = false
)
