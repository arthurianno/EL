package com.elta.android.presentation.features.registration.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.databinding.LayoutContainerBinding
import com.elta.android.presentation.features.registration.flow.pm.RegistrationFlowPm

@Deprecated("Файл служит для того чтобы перенаправить на следующий экран и ничего больше не делает")
class RegistrationFlowFragment :
    BaseFlowFragment<RegistrationFlowPm, LayoutContainerBinding>(LayoutContainerBinding::inflate) {
    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<RegistrationFlowPm> = RegistrationFlowPm::class.java

    companion object {
        fun newInstance(): RegistrationFlowFragment = RegistrationFlowFragment()
    }
}
