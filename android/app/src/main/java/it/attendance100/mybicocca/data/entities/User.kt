package it.attendance100.mybicocca.data.entities

import androidx.room.*
import it.attendance100.mybicocca.domain.model.*

@Entity(tableName = "user_profile")
data class UserEntity(
  @PrimaryKey val id: Int = 0, // Constant ID = 0 because we only store the current logged-in user
  val name: String,
  val surname: String,
  val matricola: String,
  val course: String,
  val year: String,
  val email: String,
) {
  fun toDomain() = User(
    name = name,
    surname = surname,
    matricola = matricola,
    course = course,
    year = year,
    email = email
  )
}

fun User.toEntity() = UserEntity(
  name = name,
  surname = surname,
  matricola = matricola,
  course = course,
  year = year,
  email = email
)