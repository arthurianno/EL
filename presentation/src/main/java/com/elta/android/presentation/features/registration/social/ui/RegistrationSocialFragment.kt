package com.elta.android.presentation.features.registration.social.ui

import android.os.Bundle
import android.view.View
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.R
import com.elta.android.presentation.features.registration.main.ui.BaseRegistrationFragment
import com.elta.android.presentation.features.registration.social.pm.RegistrationSocialPm
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo

class RegistrationSocialFragment : BaseRegistrationFragment<RegistrationSocialPm>() {

    override val continueButtonText: Int = R.string.registration_main_button_continue
    override val authTitleText: Int = R.string.registration_social_title_no_name
    override val authSubtitleText: Int = R.string.registration_social_subtitle
    override val classToken: Class<RegistrationSocialPm> = RegistrationSocialPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val network = arguments?.getSerializable(EXTRA_NETWORK) as? SocialNetworkType
        network?.let { presentationModel.setSocialNetwork(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.socialNetworksView.hide()
    }

    override fun onBindPresentationModel(pm: RegistrationSocialPm) {
        super.onBindPresentationModel(pm)
        pm.authTitleState.bindTo(binding.authTitleView.text())
    }

    companion object {
        private const val EXTRA_NETWORK = "extra_network"
        fun newInstance(network: SocialNetworkType): RegistrationSocialFragment =
            RegistrationSocialFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_NETWORK, network)
                }
            }
    }
}
