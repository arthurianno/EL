package com.elta.android.data.features.user.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Suppress("UseDataClass")
@Entity
class HealthAppCacheDto {
    lateinit var profile: ToOne<ProfileCacheDto>
    lateinit var type: String

    @Id(assignable = true)
    var id: Long = 0
    var isActive: Boolean = false

    constructor()

    constructor(id: Long, type: String, isActive: Boolean) {
        this.id = id
        this.type = type
        this.isActive = isActive
    }
}