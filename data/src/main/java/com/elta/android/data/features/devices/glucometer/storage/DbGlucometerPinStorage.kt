package com.elta.android.data.features.devices.glucometer.storage

import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.devices.dto.GlucometerPinWrapper
import io.objectbox.Box
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbGlucometerPinStorage @Inject constructor(
    private val factory: BoxStoreFactory
) : GlucometerPinStorage {

    private val box: Box<GlucometerPinWrapper>
        get() = factory.getBoxStore(BoxScope.PER_USER).boxFor(GlucometerPinWrapper::class.java)

    override fun getPin(address: String): String? = box[address.hashCode().toLong()]?.pin

    override fun setPin(address: String, pinCode: String) {
        box.put(GlucometerPinWrapper(address.hashCode().toLong(), pinCode))
    }
}
