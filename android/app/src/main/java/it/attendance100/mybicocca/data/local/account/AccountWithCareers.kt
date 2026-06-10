package it.attendance100.mybicocca.data.local.account

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Projection of an account row joined with all of its career rows via Room's relation support;
 * the unit the DAO reads and the mapper turns into the domain account.
 */
data class AccountWithCareers(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "account_id",
    )
    val careers: List<CareerEntity>,
)
