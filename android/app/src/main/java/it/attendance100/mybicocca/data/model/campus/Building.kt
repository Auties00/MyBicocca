package it.attendance100.mybicocca.data.model.campus

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey val code: String,
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
