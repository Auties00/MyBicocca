package it.attendance100.mybicocca.data.database

import androidx.room.TypeConverter
import it.attendance100.mybicocca.data.model.calendar.EventSource
import java.time.LocalDate
import java.time.LocalTime

class MyBicoccaTypeConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(",")

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.takeIf { it.isNotEmpty() }?.split(",")

    @TypeConverter
    fun fromEventSource(value: EventSource?): String? = value?.name

    @TypeConverter
    fun toEventSource(value: String?): EventSource? = value?.let { EventSource.valueOf(it) }
}
