package com.elta.android.presentation.features.version.optional

class OptionalUpdatePromptState {
    var foreground = false
    var online = false
    var startupComplete = false
    var serverVersionChecked = false
    var mandatoryUpdate = false
    var glucometerSync = false
    var serverSync = false
    var backendSync = false
    var dialogVisible = false
    private var requested = false

    fun requestPrompt(hasUpdate: Boolean, lastShownAtMillis: Long, nowMillis: Long): Boolean {
        if (!hasUpdate || !foreground || !online || !startupComplete || !serverVersionChecked ||
            mandatoryUpdate || glucometerSync || serverSync || backendSync || dialogVisible || requested
        ) return false
        if (lastShownAtMillis > 0 && nowMillis >= lastShownAtMillis &&
            nowMillis - lastShownAtMillis < PROMPT_INTERVAL_MILLIS
        ) return false
        requested = true
        return true
    }

    fun onDialogVisibilityChanged(visible: Boolean) {
        requested = false
        dialogVisible = visible
    }

    companion object {
        const val PROMPT_INTERVAL_MILLIS = 3L * 24 * 60 * 60 * 1000
    }
}
