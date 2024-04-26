package com.elta.android.presentation.features.onboaring.ui.adapter.validators

import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import com.rengwuxian.materialedittext.MaterialEditText

class OMSValidator(
    field: MaterialEditText,
    resources: ResourceProvider,
    validatorCallback: (Pair<String, Boolean>) -> Unit
) : BaseValidator(resources) {

    override val field: MaterialEditText = field

    override val validatorCallback: (Pair<String, Boolean>) -> Unit = validatorCallback

    override val textChangedListener: MaskedTextChangedListener.ValueListener =
        object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(
                maskFilled: Boolean,
                extractedValue: String,
                formattedValue: String
            ) {
                if (maskFilled) {
                    clearError()
                }

                validatorCallback.invoke(formattedValue to maskIsValid(maskFilled))
            }
        }

    override val formattedMaxLength: List<Int> = listOf(10, 12, 19)
    override val primaryFormat: String = TEMP_OMS_MASK
    override val affineFormats: List<String> = listOf(UNIFIED_OMS_MASK, OLD_OMS_MASK)

    override fun validate() {
        with(field) {
            val listener = MaskedTextChangedListener.installOn(
                editText = this,
                primaryFormat = TEMP_OMS_MASK,
                affineFormats = listOf(OLD_OMS_MASK, UNIFIED_OMS_MASK),
                affinityCalculationStrategy = AffinityCalculationStrategy.WHOLE_STRING,
                valueListener = textChangedListener,
                autoskip = true,
                autocomplete = false
            )

            addTextChangedListener(listener)
            onFocusChangeListener = listener

            setOnFocusChangeListener { _, hasFocus ->

                if (hasFocus) {
                    clearError()
                } else {
                    validateWith(lengthValidator)
                    validateWith(emptyValidator)
                }
            }

            hint = resources.getString(R.string.on_boarding_emias_hint)

            errorColor = resources.getColor(R.color.red)
        }
    }


}

private const val TEMP_OMS_MASK = "[0000] [00000]"
private const val UNIFIED_OMS_MASK = "[0000] [0000] [0000] [0000]"
private const val OLD_OMS_MASK = "[0000] [0000] [00]"
