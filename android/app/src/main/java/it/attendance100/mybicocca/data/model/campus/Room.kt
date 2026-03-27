package it.attendance100.mybicocca.data.model.campus

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = Building::class,
            parentColumns = ["code"],
            childColumns = ["buildingCode"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("buildingCode")]
)
data class Room(
    @PrimaryKey val code: String,
    val buildingCode: String,
    val name: String,
    val capacity: Int? = null,
)
