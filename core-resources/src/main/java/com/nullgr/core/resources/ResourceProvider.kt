package com.nullgr.core.resources

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * Resource provider serves to provide any resource from context.
 * Best practice is to use singleton instance provided with DI
 *
 * @property context [Context]
 * @author il_mov.
 */
class ResourceProvider(private val context: Context) {

    private fun getLocalizedResources(): Resources {
        val prefs = context.getSharedPreferences("language_preference", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", null) ?: return context.resources
        
        val languageCode = when (savedLanguage.lowercase(Locale.ROOT).substringBefore('-')) {
            "en" -> "en"
            "ru" -> "ru"
            else -> "ru"
        }
        
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            context.createConfigurationContext(config).resources
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            Resources(context.assets, context.resources.displayMetrics, config)
        }
    }

    /**
     * @see [android.content.res.Resources.getString]
     */
    fun getString(@StringRes resId: Int): String {
        return getLocalizedResources().getString(resId)
    }

    /**
     * @see [android.content.res.Resources.getString]
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getLocalizedResources().getString(resId, *formatArgs)
    }

    /**
     * @see [android.content.res.Resources.getStringArray]
     */
    fun getStringArray(@ArrayRes resId: Int): Array<String> {
        return getLocalizedResources().getStringArray(resId)
    }

    /**
     * @see [android.content.res.Resources.getQuantityString]
     */
    fun getQuantityString(@PluralsRes resId: Int, count: Int): String {
        return getLocalizedResources().getQuantityString(resId, count)
    }

    /**
     * @see [android.content.res.Resources.getQuantityString]
     */
    fun getQuantityString(@PluralsRes resId: Int, count: Int, vararg formatArgs: Any): String {
        return getLocalizedResources().getQuantityString(resId, count, *formatArgs)
    }

    /**
     * @see [ContextCompat.getColor]
     */
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    /**
     * @see [ContextCompat.getDrawable]
     */
    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    /**
     * Returns an [Int] identifier of [Drawable] res by it [String] name
     * Example:
     * ```
     * val resId = resourceProvider.getDrawableId("ic_launcher")
     * ```
     * @param name [String] name identifier of drawable res.
     * @return [Int] identifier of [Drawable] or **0** if something went wrong
     */
    fun getDrawableId(name: String?): Int {
        return if (name != null && !TextUtils.isEmpty(name)) {
            getLocalizedResources().getIdentifier(name, "drawable", context.packageName)
        } else 0
    }

    /**
     * @see [android.content.res.Resources.getDimensionPixelSize]
     */
    fun getPxSize(@DimenRes resId: Int): Int {
        return getLocalizedResources().getDimensionPixelSize(resId)
    }

    /**
     * @see [android.content.res.Resources.getBoolean]
     */
    fun getBoolean(@BoolRes resId: Int): Boolean {
        return getLocalizedResources().getBoolean(resId)
    }

    /**
     * @see [android.content.res.Resources.getInteger]
     */
    fun getInt(@IntegerRes resId: Int): Int {
        return getLocalizedResources().getInteger(resId)
    }

    /**
     * @see [android.content.res.Resources.getIntArray]
     */
    fun getIntArray(@ArrayRes resId: Int): IntArray {
        return getLocalizedResources().getIntArray(resId)
    }

    /**
     * Returns screen's density.
     * @return [Float] density
     * @see [android.util.DisplayMetrics.density]
     * */
    fun getDensity(): Float {
        return getLocalizedResources().displayMetrics.density
    }
}
