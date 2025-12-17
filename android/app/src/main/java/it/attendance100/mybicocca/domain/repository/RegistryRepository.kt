package it.attendance100.mybicocca.domain.repository

interface RegistryRepository {
    suspend fun syncCourses()
}