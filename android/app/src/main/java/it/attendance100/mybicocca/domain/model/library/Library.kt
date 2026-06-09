package it.attendance100.mybicocca.domain.model.library

// A bookable university library (an Affluences "site"). The two Bicocca libraries are children
// of the library-system root site; only the ones that accept seat booking are surfaced.
data class Library(
    val id: String,
    val slug: String,
    val name: String,
    val secondaryName: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val pictureUrl: String?,
    val phone: String?,
    val email: String?,
    val websiteUrl: String?,
    val bookable: Boolean,
    // Light status carried by the children listing, enough for the landing cards. The full
    // status (closing text + hourly forecast) is fetched per library via getLiveStatus.
    val liveStatus: LibraryLiveStatus?,
)
