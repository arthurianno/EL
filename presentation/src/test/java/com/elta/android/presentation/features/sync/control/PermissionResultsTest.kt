package com.elta.android.presentation.features.sync.control

import com.tbruyelle.rxpermissions2.Permission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PermissionResultsTest {
    @Test
    fun `fully granted batch resumes the operation exactly once`() {
        val results = listOf(
            Permission("scan", true, false),
            Permission("connect", true, false),
            Permission("location", true, false)
        )
        assertEquals(listOf(results.first()), results.completedPermissionResults())
    }

    @Test
    fun `partially granted batch reports every denial without starting the operation`() {
        val scan = Permission("scan", false, true)
        val connect = Permission("connect", true, false)
        val location = Permission("location", false, false)
        val dispatched = listOf(scan, connect, location).completedPermissionResults()
        assertEquals(listOf(scan, location), dispatched)
        assertFalse(dispatched.any { it.granted })
    }

    @Test
    fun `empty batch does not resume an operation`() {
        assertEquals(emptyList<Permission>(), emptyList<Permission>().completedPermissionResults())
    }
}
