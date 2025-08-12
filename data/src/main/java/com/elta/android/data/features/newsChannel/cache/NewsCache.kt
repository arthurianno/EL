package com.elta.android.data.features.newsChannel.cache

import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.domain.features.newsChannel.model.News
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsCache @Inject constructor(
    private val boxStoreFactory: BoxStoreFactory
) : Cache<NewsEntity> {
    private val newsBox: Box<NewsEntity> by lazy {
        boxStoreFactory.getBoxStore(BoxScope.PER_APP).boxFor(NewsEntity::class)
    }

    override fun add(objects: List<NewsEntity>) {
        newsBox.put(objects)
    }

    override fun update(objects: List<NewsEntity>) {
        newsBox.put(objects)
    }

    override fun delete(condition: Condition) {
        val objectsToRemove = buildQuery(condition).build().find()
        newsBox.remove(objectsToRemove)
    }


    override fun get(condition: Condition): NewsEntity? {
        return buildQuery(condition).build().findFirst()
    }

    override fun getAll(condition: Condition): List<NewsEntity> {
        return buildQuery(condition).build().find()
    }

    override fun contains(condition: Condition): Boolean {
        return buildQuery(condition).build().count() > 0
    }

    override fun count(condition: Condition): Long {
        return buildQuery(condition).build().count()
    }

    private fun buildQuery(condition: Condition): QueryBuilder<NewsEntity> {
        val queryBuilder = newsBox.query()
        when (condition) {
            is CommonConditions.ById -> {
                queryBuilder.equal(NewsEntity_.id, condition.id)
            }
            is CommonConditions.ByIds -> {
                queryBuilder.`in`(NewsEntity_.id, condition.ids.toLongArray())
            }
            is CommonConditions.All -> {
                // Без фильтров
            }
            else -> throw IllegalArgumentException("Unsupported condition: $condition")
        }
        return queryBuilder
    }

    fun put(news: News) {
        newsBox.put(NewsEntity.fromDomain(news))
    }

    fun putAll(news: List<News>) {
        newsBox.put(news.map { NewsEntity.fromDomain(it) })
    }

    fun get(id: UUID): News? {
        return newsBox.query()
            .equal(NewsEntity_.id, id.toString().toLong())
            .build()
            .findFirst()
            ?.toDomain()
    }

    fun getAll(): List<News> {
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L // 7 дней в миллисекундах
        val currentTime = System.currentTimeMillis()
        val cachedNews = newsBox.query()
            .greater(NewsEntity_.cachedDateTime, currentTime - sevenDaysInMillis)
            .orderDesc(NewsEntity_.orderNumber)
            .build()
            .find()
            .map { it.toDomain() }
        return cachedNews
    }
    fun removeExpired() {
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        val expiredNews = newsBox.query()
            .less(NewsEntity_.cachedDateTime, currentTime - sevenDaysInMillis)
            .build()
            .find()
        newsBox.remove(expiredNews)
    }

    fun clear() {
        newsBox.removeAll()
    }
}