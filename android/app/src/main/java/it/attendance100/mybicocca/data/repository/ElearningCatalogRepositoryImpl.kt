package it.attendance100.mybicocca.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit
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

@Singleton
class ElearningCatalogRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ElearningCatalogRepository {

    @Volatile private var cached: Cached? = null
    private val loadMutex = Mutex()

    override suspend fun load(): ElearningCatalog = ensureLoaded().catalog

    override suspend fun search(query: String, limit: Int): List<CatalogSearchHit> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val state = ensureLoaded()
        val needle = trimmed.lowercase()
        return withContext(Dispatchers.Default) {
            val results = ArrayList<CatalogSearchHit>(minOf(limit, 256))
            for (entry in state.searchIndex) {
                if (entry.haystack.contains(needle)) {
                    results.add(CatalogSearchHit(entry.course, entry.path))
                    if (results.size >= limit) break
                }
            }
            results
        }
    }

    private suspend fun ensureLoaded(): Cached {
        cached?.let { return it }
        loadMutex.withLock {
            cached?.let { return it }
            val built = withContext(Dispatchers.IO) { build() }
            cached = built
            return built
        }
    }

    private fun build(): Cached {
        val parsed = context.assets.open(ASSET_PATH).use { stream ->
            JSON.decodeFromString(RawIndex.serializer(), stream.bufferedReader().readText())
        }
        val sections = parsed.areas.map { area ->
            CatalogSection(
                name = area.name,
                nodes = area.categories.map { it.toDomain(parentId = "root") },
            )
        }
        val catalog = ElearningCatalog(sections = sections)
        val index = buildSearchIndex(sections)
        return Cached(catalog = catalog, searchIndex = index)
    }

    private fun buildSearchIndex(sections: List<CatalogSection>): List<SearchEntry> {
        val out = ArrayList<SearchEntry>(20_000)
        val crumbs = ArrayDeque<String>()
        sections.forEach { section ->
            section.nodes.forEach { node ->
                crumbs.addLast(node.name)
                collectInto(out, node, crumbs)
                crumbs.removeLast()
            }
        }
        return out
    }

    // Cross-listed courses legitimately appear under multiple breadcrumbs; emit
    // one entry per occurrence so the search shows each provenance distinctly.
    private fun collectInto(
        out: MutableList<SearchEntry>,
        node: CatalogNode,
        crumbs: ArrayDeque<String>,
    ) {
        if (node.courses.isNotEmpty()) {
            val path = crumbs.toList()
            for (course in node.courses) {
                val haystack = buildString(course.name.length + course.code.length + 1) {
                    append(course.name)
                    append(' ')
                    append(course.code)
                }.lowercase()
                out.add(SearchEntry(course = course, path = path, haystack = haystack))
            }
        }
        node.children.forEach { child ->
            crumbs.addLast(child.name)
            collectInto(out, child, crumbs)
            crumbs.removeLast()
        }
    }

    private data class Cached(
        val catalog: ElearningCatalog,
        val searchIndex: List<SearchEntry>,
    )

    private data class SearchEntry(
        val course: CatalogCourse,
        val path: List<String>,
        val haystack: String,
    )

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

// Synthesise a stable id by chaining ancestor names: category URLs are not
// always present in the index, and the same name can occur under different
// ancestors (e.g. "1° anno" under each programme).
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
