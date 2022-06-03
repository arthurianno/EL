package com.elta.android.presentation.core.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import java.util.UUID

class FragmentOutlast<T : Outlasting?>(
    fragment: Fragment,
    creator: Outlasting.Creator<T>?,
    savedInstanceState: Bundle?
) : Outlast<T>(
    creator!!, savedInstanceState
) {
    private val fragment: Fragment
    override fun isPrincipalFinishing(wasInstanceStateSaved: Boolean): Boolean {
        return (
            fragment.requireActivity().isFinishing ||
                !wasInstanceStateSaved && // See http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
                    (fragment.isRemoving || isAnyParentOfFragmentRemoving)
            )
    }

    // See https://github.com/Arello-Mobile/Moxy/issues/24
    private val isAnyParentOfFragmentRemoving: Boolean
        get() {
            var isAnyParentRemoving = false
            var parent: Fragment? = fragment.getParentFragment()
            while (!isAnyParentRemoving && parent != null) {
                isAnyParentRemoving = parent.isRemoving
                parent = parent.parentFragment
            }
            return isAnyParentRemoving
        }

    init {
        this.fragment = fragment
    }
}

interface Outlasting {
    /**
     * Outlasting creation interface.
     */
    interface Creator<T : Outlasting?> {
        /**
         * Creates the Outlasting
         */
        fun createOutlasting(): T
    }

    /**
     * Called when this Outlasting is created
     */
    fun onCreate()

    /**
     * Called before this Outlasting is about to be destroyed
     */
    fun onDestroy()
}

abstract class Outlast<T : Outlasting?> @JvmOverloads constructor(
    private val creator: Outlasting.Creator<T>,
    savedInstanceState: Bundle?,
    outlastId: Int = 0
) {
    private val outlastingTag: String?
    private val savedOutlastingTagKey: String
    private var wasInstanceStateSaved = false
    private fun obtainOutlastingTag(savedInstanceState: Bundle?): String? {
        return if (savedInstanceState == null) {
            createOutlastingTag()
        } else {
            savedInstanceState.getString(savedOutlastingTagKey)
        }
    }

    /**
     * Creates unique tag that is using to store the [Outlasting] in the store
     */
    protected fun createOutlastingTag(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Get the [Outlasting] stored by this Outlast delegate.
     */
    val outlasting: T
        get() = Store.INSTANCE.getOutlasting(creator, outlastingTag!!)!!

    /**
     * Delegated callback
     */
    fun onStart() {
        wasInstanceStateSaved = false // reset because we started after save and stop
    }

    /**
     * Delegated callback
     */
    fun onResume() {
        wasInstanceStateSaved = false // reset because we resumed after save when paused
    }

    /**
     * Delegated callback
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(savedOutlastingTagKey, outlastingTag)
        wasInstanceStateSaved = true
    }

    /**
     * Delegated callback
     */
    fun onDestroy() {
        if (isPrincipalFinishing(wasInstanceStateSaved)) {
            Store.INSTANCE.removeOutlasting(outlastingTag!!)
        }
    }

    protected abstract fun isPrincipalFinishing(wasInstanceStateSaved: Boolean): Boolean

    companion object {
        private const val SAVED_OUTLASTING_TAG = "outlasting_tag_"
    }

    init {
        savedOutlastingTagKey = SAVED_OUTLASTING_TAG + outlastId
        outlastingTag = obtainOutlastingTag(savedInstanceState)
    }
}

internal class Store private constructor() {
    private val outlastingsMap: MutableMap<String, Outlasting> = HashMap()

    /**
     * Returns stored or newly created Outlasting
     *
     * @param creator creator to instantiate the Outlasting if the map doesn't contain it.
     * @param tag     store tag. The same outlasting will be returned for the same tag,
     * so tag must be unique for different callers.
     */
    fun <T : Outlasting?> getOutlasting(
        creator: Outlasting.Creator<T>,
        tag: String
    ): T? {
        if (!outlastingsMap.containsKey(tag)) {
            val outlasting = creator.createOutlasting()
            outlasting!!.onCreate()
            outlastingsMap[tag] = outlasting
        }
        return outlastingsMap[tag] as T?
    }

    /**
     * Removes the Outlasting for the passed tag from the store, allowing it to be destroyed.
     *
     * @param tag store tag to remove the Outlasting.
     */
    fun removeOutlasting(tag: String) {
        val outlasting = outlastingsMap[tag]
        if (outlasting != null) {
            outlasting.onDestroy()
            outlastingsMap.remove(tag)
        }
    }

    companion object {
        val INSTANCE: Store = Store()
    }
}
