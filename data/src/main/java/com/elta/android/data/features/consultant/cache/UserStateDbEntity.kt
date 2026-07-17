package com.elta.android.data.features.consultant.cache

import com.elta.android.domain.features.consultant.model.UserState
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class UserStateDbEntity(
    @Id(assignable = true) var objectBoxId: Long = 0,
    val currentScenarioId: String?,
    val currentNodeId: String?,
    val historyStackCsv: String
) {
    fun toDomain(): UserState {
        return UserState(
            currentScenarioId = currentScenarioId,
            currentNodeId = currentNodeId,
            historyStack = if (historyStackCsv.isEmpty()) emptyList() else historyStackCsv.split(",")
        )
    }

    companion object {
        const val SINGLETON_ID = 1L

        fun fromDomain(domain: UserState): UserStateDbEntity {
            return UserStateDbEntity(
                objectBoxId = SINGLETON_ID,
                currentScenarioId = domain.currentScenarioId,
                currentNodeId = domain.currentNodeId,
                historyStackCsv = domain.historyStack.joinToString(",")
            )
        }
    }
}
