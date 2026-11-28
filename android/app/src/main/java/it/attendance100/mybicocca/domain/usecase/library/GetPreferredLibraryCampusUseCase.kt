package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryCampus
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.repository.LastKnownLocationSource
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Resolves the student's preferred library campus, or null when no signal applies:
 * last opened/booked library, then nearest catalogued library within 5 km (only if location
 * permission is already granted), then the selected career's programme name.
 */
class GetPreferredLibraryCampusUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val locationSource: LastKnownLocationSource,
    private val observeActiveAccount: ObserveActiveAccountUseCase,
) {
    suspend operator fun invoke(): LibraryCampus? {
        val lastLibraryCampus = libraryRepository.lastUsedLibraryId()
            ?.let(LibraryCatalog::byId)
            ?.campus
        if (lastLibraryCampus != null) return lastLibraryCampus

        val nearbyCampus = locationSource.lastKnownLatLong()
            ?.let { (lat, lon) -> LibraryCatalog.nearestWithinKm(lat, lon)?.campus }
        if (nearbyCampus != null) return nearbyCampus

        val account = observeActiveAccount().firstOrNull()
        val selectedCareer = account?.academic?.let { academic ->
            academic.careers.firstOrNull { it.id == academic.selectedCareerId }
        }
        return LibraryCatalog.campusForProgramme(selectedCareer?.description)
    }
}
