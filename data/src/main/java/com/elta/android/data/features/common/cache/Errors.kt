package com.elta.android.data.features.common.cache

data class IllegalGetAllConditionError(val condition: Condition) :
    IllegalArgumentException(
        "Passed condition ${condition::class.java.simpleName} " +
            "not supported for getAll operation."
    )

data class IllegalGetConditionError(val condition: Condition) :
    IllegalArgumentException(
        "Passed condition ${condition::class.java.simpleName} " +
            "not supported for get operation."
    )

data class IllegalDeleteConditionError(val condition: Condition) :
    IllegalArgumentException(
        "Passed condition ${condition::class.java.simpleName} " +
            "not supported for delete operation."
    )

data class IllegalContainsCondition(val condition: Condition) :
    IllegalArgumentException(
        "Passed condition ${condition::class.java.simpleName} " +
            "not supported for contains operation."
    )

data class IllegalCountCondition(val condition: Condition) :
    IllegalArgumentException(
        "Passed condition ${condition::class.java.simpleName} " +
            "not supported for count operation."
    )

object AccessDeniedError : RuntimeException("Current user == null")
