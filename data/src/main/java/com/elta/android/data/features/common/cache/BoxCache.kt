package com.elta.android.data.features.common.cache

import io.objectbox.Box

abstract class BoxCache<T>(
    protected val factory: BoxStoreFactory
) : Cache<T> {

    abstract val classToken: Class<T>

    protected open val scope: BoxScope = BoxScope.PER_USER

    protected open val box: Box<T>
        get() = factory.getBoxStore(scope).boxFor(classToken)

    override fun add(objects: List<T>) {
        box.put(objects)
    }

    override fun update(objects: List<T>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        when (condition) {
            is CommonConditions.All -> box.removeAll()
            is CommonConditions.ById -> box.remove(condition.id)
            is CommonConditions.ByIds -> box.removeByKeys(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }
    }

    override fun get(condition: Condition): T? =
        when (condition) {
            is CommonConditions.ById -> box[condition.id]
            else -> throw IllegalGetConditionError(condition)
        }

    override fun getAll(condition: Condition): List<T> =
        when (condition) {
            is CommonConditions.All -> box.all
            is CommonConditions.ByIds -> box[condition.ids]
            else -> throw IllegalGetAllConditionError(condition)
        }

    override fun contains(condition: Condition): Boolean =
        when (condition) {
            is CommonConditions.All -> box.count() > 0
            else -> throw IllegalContainsCondition(condition)
        }

    override fun count(condition: Condition): Long =
        when (condition) {
            is CommonConditions.All -> box.count()
            else -> throw IllegalCountCondition(condition)
        }
}