package it.attendance100.mybicocca.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 0, // Constant ID = 0 because we only store the current logged-in user
    val name: String,
    val surname: String,
    val matricola: String,
    val course: String,
    val year: String,
    val email: String,
)