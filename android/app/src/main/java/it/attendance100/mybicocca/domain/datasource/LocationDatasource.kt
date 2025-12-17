package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.domain.model.*

interface LocationDataSource {
  /**
   * Retrieves a list of locations
   */
  suspend fun getLocations(): List<Location>

  /**
   * Syncs locations from the server
   */
  suspend fun syncLocations()
}
