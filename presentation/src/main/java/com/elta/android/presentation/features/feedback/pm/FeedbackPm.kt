package com.elta.android.presentation.features.feedback.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class FeedbackPm @Inject constructor(
    serviceFacade: ServiceFacade
) : BasePm(serviceFacade)