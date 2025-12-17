package it.attendance100.mybicocca.data.datasource.remote

import it.attendance100.mybicocca.data.mapper.*
import it.attendance100.mybicocca.data.remote.api.bicoccapp.*
import it.attendance100.mybicocca.domain.datasource.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*

class RemoteLocationDataSource @Inject constructor(
  private val api: BicoccappCampusApi,
) : LocationDataSource {

  override suspend fun getLocations(): List<Location> {
    val response = api.getPointsOfInterest()
    if (response.isSuccessful && response.body() != null) {
      val mapLocations = response.body()!!.maps?.mapLocations
      return mapLocations.toLocations()
    }
    return emptyList()
  }

  override suspend fun syncLocations() {
    getLocations()
  }
}
