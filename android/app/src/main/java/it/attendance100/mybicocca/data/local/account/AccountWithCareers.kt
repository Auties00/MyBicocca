package it.attendance100.mybicocca.data.local.account

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithCareers(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "account_id",
    )
    val careers: List<CareerEntity>,
)
