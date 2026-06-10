package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * Behavioural type of a Moodle forum, parsed from the `type` value carried by the
 * mod_forum_get_forums_by_courses web service. The course detail screen singles out the [News]
 * forum as the course's announcements board.
 *
 * @property raw Moodle type value this entry matches.
 */
enum class ForumType(val raw: String) {
    /** Standard open forum where anyone may start discussions. */
    General("general"),
    /** Announcements forum ("Annunci"), where teachers post course-wide news. */
    News("news"),
    /** Question-and-answer forum: students must post before seeing other replies. */
    QandA("qanda"),
    /** Forum where each user may start exactly one discussion. */
    EachUser("eachuser"),
    /** Forum built around a single discussion. */
    SingleSimple("single"),
    /** Forum whose discussions are presented as blog entries. */
    BlogLike("blog"),
    /** Fallback for unrecognized type values. */
    Other("other");

    companion object {
        /** Resolves a raw Moodle type value case-insensitively, falling back to [Other]. */
        fun fromRaw(raw: String?): ForumType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
