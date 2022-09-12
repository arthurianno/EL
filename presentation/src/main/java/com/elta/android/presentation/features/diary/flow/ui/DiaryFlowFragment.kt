package com.elta.android.presentation.features.diary.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.databinding.LayoutContainerBinding
import com.elta.android.presentation.features.diary.flow.pm.DiaryFlowPm

class DiaryFlowFragment :
    BaseFlowFragment<DiaryFlowPm, LayoutContainerBinding>(LayoutContainerBinding::inflate) {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<DiaryFlowPm> = DiaryFlowPm::class.java

    companion object {
        fun newInstance() = DiaryFlowFragment()
    }
}
