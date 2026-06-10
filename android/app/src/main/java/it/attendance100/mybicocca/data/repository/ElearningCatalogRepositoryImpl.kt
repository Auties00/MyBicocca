package it.attendance100.mybicocca.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catalog repository backed by the elearning_index.json asset bundled with the app —
 * a pre-built snapshot of the Moodle category tree, so no network or account is
 * involved. The asset is parsed off the main thread on first load and memoized for
 * the process lifetime, with a mutex collapsing concurrent first loads into one parse.
 */
@Singleton
class ElearningCatalogRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ElearningCatalogRepository {

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
        val parsed = context.assets.open(ASSET_PATH).use { stream ->
            JSON.decodeFromString(RawIndex.serializer(), stream.bufferedReader().readText())
        }
        val sections = parsed.areas.map { area ->
            CatalogSection(
                name = area.name,
                nodes = area.categories.map { it.toDomain(parentId = "root") },
            )
        }
        return ElearningCatalog(sections = sections)
    }

    private companion object {
        const val ASSET_PATH = "elearning_index.json"
        val JSON = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class RawIndex(
    val areas: List<RawArea>,
)

@Serializable
private data class RawArea(
    val name: String,
    val categories: List<RawNode>,
)

@Serializable
private data class RawNode(
    val name: String,
    val url: String? = null,
    val categories: List<RawNode> = emptyList(),
    val courses: List<RawCourse> = emptyList(),
)

@Serializable
private data class RawCourse(
    val id: Int,
    val name: String,
    val code: String,
    val url: String,
)

/**
 * Maps an index node to the domain tree, synthesising a stable id by chaining
 * ancestor names: category URLs are not always present in the index, and the same
 * name can occur under different ancestors (e.g. "1° anno" under each programme).
 */
private fun RawNode.toDomain(parentId: String): CatalogNode {
    val id = "$parentId|$name"
    return CatalogNode(
        id = id,
        name = name,
        url = url,
        children = categories.map { it.toDomain(id) },
        courses = courses.map { CatalogCourse(CourseId(it.id), it.name, it.code, it.url) },
    )
}
