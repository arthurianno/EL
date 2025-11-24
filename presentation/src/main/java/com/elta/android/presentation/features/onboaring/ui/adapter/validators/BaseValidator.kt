package com.elta.android.presentation.features.onboaring.ui.adapter.validators

import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.rengwuxian.materialedittext.MaterialEditText
import com.rengwuxian.materialedittext.validation.METValidator

abstract class BaseValidator(private val resources: ResourceProvider) {

    protected val emptyValidator =
        object : METValidator(resources.getString(R.string.required_field_error)) {
            override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {
                return !isEmpty
            }
        }

    protected val lengthValidator =
        object : METValidator(resources.getString(R.string.incorrect_length_error)) {
            override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {
                return if (isEmpty) {
                    true
                } else {
                    text.length in formattedMaxLength
                }
            }
        }

    protected abstract val field: MaterialEditText
    protected abstract val validatorCallback: (Pair<String, Boolean>) -> Unit

    protected abstract val textChangedListener: MaskedTextChangedListener.ValueListener

    protected abstract val formattedMaxLength: List<Int>
    protected abstract val primaryFormat: String
    protected abstract val affineFormats: List<String>?

    abstract fun validate()

    protected fun clearError() {
        field.error = null
    }

    protected fun maskIsValid(maskFilled: Boolean) = field.error.isNullOrBlank() && maskFilled

}