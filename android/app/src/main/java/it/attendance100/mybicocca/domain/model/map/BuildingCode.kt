package it.attendance100.mybicocca.domain.model.map

/**
 * A campus building code, zero-padded for the numbered buildings (e.g. "U01") with a few
 * non-numbered sites (e.g. "U-MB"). Codes come from the bundled building catalog and double as
 * the key EasyStaff accepts in room and occupation queries.
 */
@JvmInline
value class BuildingCode(val value: String)
