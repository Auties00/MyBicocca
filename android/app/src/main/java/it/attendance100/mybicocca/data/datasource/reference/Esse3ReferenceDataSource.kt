package it.attendance100.mybicocca.data.datasource.reference

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.model.reference.CourseType
import it.attendance100.mybicocca.data.model.reference.DidacticStructure
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3ReferenceDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getCourseTypes(): List<CourseType> = withContext(ioDispatcher) {
        esse3Api.structure.getCourseTypes().mapNotNull { dto ->
            val code = dto.courseTypeCode ?: return@mapNotNull null
            val desc = dto.courseTypeDescription ?: return@mapNotNull null
            CourseType(code = code, description = desc)
        }
    }

    suspend fun getDidacticStructures(): List<DidacticStructure> = withContext(ioDispatcher) {
        esse3Api.structure.getDidacticStructures().mapNotNull { dto ->
            val id = dto.facultyId ?: return@mapNotNull null
            DidacticStructure(
                facultyId = id,
                code = dto.facultyCode,
                description = dto.facultyDescription,
            )
        }
    }
}
