package com.elta.android.domain.features.user.model

sealed class AdditionalFunction
object WhereBuy : AdditionalFunction()
object MyObservers : AdditionalFunction()
object MyDevices : AdditionalFunction()
object Support : AdditionalFunction()
object ExitFromApp : AdditionalFunction()