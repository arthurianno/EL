package com.elta.android.data.features.common.cache

data class IllegalGetConditionError(val condition: Condition)
    : IllegalArgumentException("Passed condition $condition not supported for get operation.")

data class IllegalDeleteConditionError(val condition: Condition)
    : IllegalArgumentException("Passed condition ${condition::class.java.simpleName} not supported for delete operation.")

object AccessDeniedError : RuntimeException("Current user == null")