package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardLessonsResponse(
    @SerialName("lessons")
    val lessonsByYear: Map<String, List<BicoccappWizardLesson>> = emptyMap()
)

