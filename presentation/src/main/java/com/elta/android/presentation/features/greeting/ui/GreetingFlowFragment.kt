package com.elta.android.presentation.features.greeting.ui

import android.os.Bundle
import android.view.View
import coil.load
import coil.request.CachePolicy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentGreetingBinding
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo

class GreetingFlowFragment :
    BaseFragment<GreetingPm, FragmentGreetingBinding>(FragmentGreetingBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_greeting
    override val classToken: Class<GreetingPm> = GreetingPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.homeButtonView.hide()
        binding.toolbar.menuButtonView.setText(R.string.greeting_menu_action)
    }

    override fun onBindPresentationModel(pm: GreetingPm) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.menuAction)
        binding.registrationButtonView.clicks().bindTo(pm.registrationAction)

        // 1. Подписка на обновление текстов
        pm.screenConfigState.bindTo { screenEntity ->
            binding.greetingTitleView.text = screenEntity?.title
                ?: getString(R.string.greeting_title)
            binding.greetingMessageView.text = screenEntity?.description
                ?: getString(R.string.greeting_message)
        }

        // 2. Подписка на готовность картинки
        pm.imagePreloadState.bindTo { isReady ->
            if (isReady != true) return@bindTo

            pm.screenConfigState.value?.let { screenEntity ->
                val imageUrl = screenEntity.backgroundImageUrl
                if (imageUrl != null) {
                    // Картинка УЖЕ в кэше, просто загружаем
                    binding.backgroundImageView.load(imageUrl) {
                        crossfade(false)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                } else {
                    // Нет URL — показываем дефолтную картинку
                    binding.backgroundImageView.setImageResource(R.drawable.ic_welcome)
                }
                binding.root.visibility = View.VISIBLE
            }
        }
    }





    companion object {
        fun newInstance(): GreetingFlowFragment = GreetingFlowFragment()
    }
}
