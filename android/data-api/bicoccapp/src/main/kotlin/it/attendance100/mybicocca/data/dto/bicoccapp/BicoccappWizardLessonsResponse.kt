package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappWizardLessonsResponse(
    @SerializedName("lessons")
    val lessonsByYear: Map<String, List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappWizardLesson>> = emptyMap()
)

