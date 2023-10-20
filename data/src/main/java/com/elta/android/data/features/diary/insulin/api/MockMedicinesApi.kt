package com.elta.android.data.features.diary.insulin.api

import com.elta.android.data.features.diary.insulin.dto.MedicinesNetworkResponse
import io.reactivex.Single

class MockMedicinesApi : MedicinesApi {
    override fun getInsulinMedicines(): Single<MedicinesNetworkResponse> {
        return throw Exception()

    }

}




