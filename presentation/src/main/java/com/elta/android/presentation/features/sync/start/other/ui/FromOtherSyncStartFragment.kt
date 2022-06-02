package com.elta.android.presentation.features.sync.start.other.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.sync.start.base.ui.SyncStartFragment
import com.elta.android.presentation.features.sync.start.other.pm.FromOtherSyncStartPm
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import kotlinx.android.synthetic.main.fragment_sync_start.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class FromOtherSyncStartFragment : SyncStartFragment<FromOtherSyncStartPm>() {

    override val classToken: Class<FromOtherSyncStartPm> = FromOtherSyncStartPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
        homeButtonView.show()
        menuButtonView.hide()
        startMessageView.text = getString(R.string.sync_start_other_subtitle)
    }

    companion object {
        fun newInstance() = FromOtherSyncStartFragment()
    }
}
