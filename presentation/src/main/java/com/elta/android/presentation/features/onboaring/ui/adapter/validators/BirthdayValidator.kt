package com.elta.android.presentation.features.onboaring.ui.adapter.validators

import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.rengwuxian.materialedittext.MaterialEditText
import com.rengwuxian.materialedittext.validation.METValidator
import java.util.Calendar

class BirthdayValidator(
    field: MaterialEditText,
    resources: ResourceProvider,
    validatorCallback: (Pair<String, Boolean>) -> Unit
) : BaseValidator(resources) {

    override val field: MaterialEditText = field
    override val validatorCallback: (Pair<String, Boolean>) -> Unit = validatorCallback

    private val dayAndMonthLength = listOf(2, 3, 5, 6)

    override val formattedMaxLength: List<Int> = listOf(10)
    override val primaryFormat: String = "[00]{.}[00]{.}[0000]"
    override val affineFormats: List<String>? = null

    override val textChangedListener: MaskedTextChangedListener.ValueListener = object : MaskedTextChangedListener.ValueListener {
        override fun onTextChanged(
            maskFilled: Boolean, extractedValue: String, formattedValue: String
        ) {
            if (maskFilled || extractedValue.isEmpty()) {
                clearError()
            }

            if (extractedValue.length in (dayAndMonthLength) || maskFilled) {
                field.validateWith(dateValidator)
                field.validateWith(birthdayValidator)
            }

            validatorCallback.invoke(formattedValue to maskIsValid(maskFilled))
        }
    }

    override fun validate() {
        with(field) {
            val listener = MaskedTextChangedListener.installOn(
                editText = this,
                primaryFormat = primaryFormat,
                valueListener = textChangedListener,
                autoskip = true,
                autocomplete = false
            )
            addTextChangedListener(listener)
            onFocusChangeListener = listener

            setOnFocusChangeListener { _, hasFocus ->
                val textId = if (hasFocus) {
                    R.string.on_boarding_emias_date_birth_mask_hint
                } else {
                    R.string.on_boarding_emias_date_birth_hint
                }
                hint = resources.getString(textId)

                if (hasFocus) {
                    clearError()
                } else {
                    validateWith(lengthValidator)
                    validateWith(emptyValidator)
                }
            }


            errorColor = resources.getColor(R.color.red)
        }
    }

    private val dateValidator =
        object : METValidator(resources.getString(R.string.incorrect_date_error)) {

            override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {

                val chars = text.toString().toCharArray()

                chars.dayValidate() ?: return false

                if (chars.size < 4) {
                    return true
                }
                chars.monthValidate() ?: return false

                return true
            }
        }

    private val birthdayValidator = object : METValidator(resources.getString(R.string.incorrect_age_error)) {
        override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {
            val chars = text.toString().toCharArray()

            val day = chars.getDay() ?: return true
            val month = chars.getMonth() ?: return true

            return when {
                chars.size < 7 -> true

                chars.size in formattedMaxLength -> {
                    chars.yearValidate(day, month)
                }

                else -> {
                    false
                }
            }
        }
    }


    private fun CharArray.dayValidate(): String? {
        val day = getDay() ?: return null

        return if (day.toInt() in 1..31) {
            day
        } else {
            null
        }
    }

    private fun CharArray.getDay(): String? {
        return try {
            "${this[0]}${this[1]}"
        } catch (ex: Exception) {
            null
        }
    }

    private fun CharArray.monthValidate(): String? {
        return try {
            val month = getMonth() ?: return null

            if (month.toInt() in 1..12) {
                month
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    private fun CharArray.getMonth(): String? {
        return try {
            "${this[3]}${this[4]}"
        } catch (ex: Exception) {
            null
        }
    }

    private fun CharArray.yearValidate(day: String, month: String): Boolean {
        return try {
            val year = "${this[6]}${this[7]}${this[8]}${this[9]}"

            val currentDate = Calendar.getInstance()
            val birthday = Calendar.getInstance().apply {
                set(Calendar.YEAR, year.toInt())
                set(Calendar.MONTH, month.toInt() - 1)
                set(Calendar.DAY_OF_MONTH, day.toInt())
            }

            !birthday.after(currentDate) //FIXME: перенести в другой валидатор
        } catch (ex: Exception) {
            false
        }
    }

}