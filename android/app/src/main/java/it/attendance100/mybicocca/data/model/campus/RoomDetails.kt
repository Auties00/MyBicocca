package it.attendance100.mybicocca.data.model.campus

data class RoomDetails(
    val name: String,
    val address: String?,
    val googleMapsLink: String?,
    val interactive360Link: String?,
    val description: String?,
    val capacity: Int?,
    val roomType: String?,
    val floor: Int?,
    val isAccessible: Boolean,
    val equipment: List<String>,
)
