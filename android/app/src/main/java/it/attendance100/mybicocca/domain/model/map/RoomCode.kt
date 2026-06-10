package it.attendance100.mybicocca.domain.model.map

/** An EasyStaff room code (e.g. "U6-22"); unique within its building, not campus-wide. */
@JvmInline
value class RoomCode(val value: String)
