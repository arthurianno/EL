package com.elta.android.presentation.features.auth.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.auth.flow.pm.AuthFlowPm

class AuthFlowFragment : BaseFlowFragment<AuthFlowPm>() {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<AuthFlowPm> = AuthFlowPm::class.java

    companion object {
        fun newInstance(): AuthFlowFragment = AuthFlowFragment()
    }
}
