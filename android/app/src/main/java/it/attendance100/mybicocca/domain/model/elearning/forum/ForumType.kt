package it.attendance100.mybicocca.domain.model.elearning.forum

enum class ForumType(val raw: String) {
    General("general"),
    News("news"),
    QandA("qanda"),
    EachUser("eachuser"),
    SingleSimple("single"),
    BlogLike("blog"),
    Other("other");

    companion object {
        fun fromRaw(raw: String?): ForumType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
