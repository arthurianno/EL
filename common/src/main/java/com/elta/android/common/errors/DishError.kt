package com.elta.android.common.errors

sealed class DishError : Exception() {
    object NotFound : DishError()
    object NotMatchType : DishError()

}
