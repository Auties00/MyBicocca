package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserRegistrationsCareer(
    @SerializedName("registrations")
    val registrations: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCareerRegistration> = emptyList()
)