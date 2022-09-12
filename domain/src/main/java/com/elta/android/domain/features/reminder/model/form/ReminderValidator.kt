package com.elta.android.domain.features.reminder.model.form

object ReminderValidator {

    fun isValid(inputValue: String?) = !inputValue.isNullOrEmpty()
}
