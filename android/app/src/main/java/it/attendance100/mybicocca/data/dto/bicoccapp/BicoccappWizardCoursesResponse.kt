package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappWizardCoursesResponse(
    @SerializedName("lessons")
    val lessonsByYear: Map<String, List<BicoccappWizardLesson>> = emptyMap()
)

