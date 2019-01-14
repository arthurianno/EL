package com.nullgr.android.common.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import java.util.IllegalFormatConversionException

/**
 * Old good crutch for fixing an error
 * <br/>
 * ```
 * Fatal Exception: java.util.IllegalFormatConversionException: %d can't format java.lang.String arguments
 * ```
 * Used only with [android.app.DatePickerDialog]
 */
class FixedContextWrapper(context: Context?) : ContextWrapper(context) {

    private var wrappedResources: Resources? = null

    @Suppress("DEPRECATION", "TooGenericExceptionCaught")
    override fun getResources(): Resources? {
        if (wrappedResources == null) {
            val res = super.getResources()
            wrappedResources = object : Resources(res.assets, res.displayMetrics, res.configuration) {
                @Throws(NotFoundException::class)
                override fun getString(id: Int, vararg formatArgs: Any): String {
                    return try {
                        super.getString(id, formatArgs)
                    } catch (error: IllegalFormatConversionException) {
                        try {
                            var template = super.getString(id)
                            template = template.replace(("%" + error.conversion).toRegex(), "%s")
                            String.format(configuration.locale, template, formatArgs)
                        } catch (ignore: Throwable) {
                            ""
                            // return empty string to avoid any crash if something went wrong
                            // while we tried to fix IllegalFormatConversionException
                        }
                    }
                }
            }
        }
        return wrappedResources
    }
}