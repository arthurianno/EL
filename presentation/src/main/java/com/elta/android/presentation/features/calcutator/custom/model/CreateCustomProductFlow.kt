package com.elta.android.presentation.features.calcutator.custom.model

enum class CreateCustomProductFlow {
    CREATING, EDITING, VIEWING;

    companion object {
        fun CreateCustomProductFlow.isCreating(): Boolean {
            return this in listOf(CREATING, EDITING)
        }
    }

}