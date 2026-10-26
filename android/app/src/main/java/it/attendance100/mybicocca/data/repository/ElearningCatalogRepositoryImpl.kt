package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebResources
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebCatalogCategory
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catalog repository backed by the catalog the data API ships as a resource
 * ([MoodleWebResources.catalog]) — a pre-built snapshot of the Moodle category tree, so no
 * network or account is involved. It is decoded off the main thread on first load and memoized
 * for the process lifetime, with a mutex collapsing concurrent first loads into one decode.
 */
@Singleton
class ElearningCatalogRepositoryImpl @Inject constructor() : ElearningCatalogRepository {

    @Volatile private var cached: ElearningCatalog? = null
    private val loadMutex = Mutex()

    override suspend fun load(): ElearningCatalog {
        cached?.let { return it }
        loadMutex.withLock {
            cached?.let { return it }
            val built = withContext(Dispatchers.IO) { build() }
            cached = built
            return built
        }
    }

    private fun build(): ElearningCatalog {
        val sections = MoodleWebResources.catalog().areas.map { area ->
            CatalogSection(
                name = area.name,
                nodes = area.categories.map { it.toDomain(parentId = "root") },
            )
        }
        return ElearningCatalog(sections = sections)
    }
}

/**
 * Maps a catalog category to the domain tree, synthesising a stable id by chaining
 * ancestor names: category URLs are not always present in the catalog, and the same
 * name can occur under different ancestors (e.g. "1° anno" under each programme).
 */
private fun MoodleWebCatalogCategory.toDomain(parentId: String): CatalogNode {
    val id = "$parentId|$name"
    return CatalogNode(
        id = id,
        name = name,
        url = url,
        children = categories.map { it.toDomain(id) },
        courses = courses.map { CatalogCourse(CourseId(it.id), it.name, it.code, it.url) },
    )
}
