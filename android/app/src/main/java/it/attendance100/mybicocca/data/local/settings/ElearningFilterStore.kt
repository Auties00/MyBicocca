package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningFilterStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val filter: Flow<CourseFilter> = dataStore.data.map { prefs ->
        prefs[FILTER_KEY]?.let(::deserialize) ?: CourseFilter.All
    }

    suspend fun setFilter(filter: CourseFilter) {
        dataStore.edit { prefs ->
            prefs[FILTER_KEY] = serialize(filter)
        }
    }

    private fun serialize(filter: CourseFilter): String = when (filter) {
        CourseFilter.All -> TOKEN_ALL
        CourseFilter.Favourites -> TOKEN_FAVOURITES
        is CourseFilter.ByYear -> "$TOKEN_YEAR_PREFIX${filter.year.value}"
    }

    private fun deserialize(token: String): CourseFilter? = when {
        token == TOKEN_ALL -> CourseFilter.All
        token == TOKEN_FAVOURITES -> CourseFilter.Favourites
        token.startsWith(TOKEN_YEAR_PREFIX) ->
            token.removePrefix(TOKEN_YEAR_PREFIX).toIntOrNull()
                ?.takeIf { it >= 0 }
                ?.let { CourseFilter.ByYear(StudyYear(it)) }
        else -> null
    }

    private companion object {
        val FILTER_KEY = stringPreferencesKey("elearning_course_filter")
        const val TOKEN_ALL = "all"
        const val TOKEN_FAVOURITES = "favourites"
        const val TOKEN_YEAR_PREFIX = "year:"
    }
}
