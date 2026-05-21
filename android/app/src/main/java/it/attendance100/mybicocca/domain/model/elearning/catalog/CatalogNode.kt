package it.attendance100.mybicocca.domain.model.elearning.catalog

data class CatalogNode(
    val id: String,
    val name: String,
    val url: String?,
    val children: List<CatalogNode>,
    val courses: List<CatalogCourse>,
)
