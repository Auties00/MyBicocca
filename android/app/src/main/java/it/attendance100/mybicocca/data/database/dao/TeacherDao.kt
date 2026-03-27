package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.teacher.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY fullName")
    fun observeAll(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE code = :code")
    suspend fun getByCode(code: String): Teacher?

    @Query("SELECT * FROM teachers WHERE fullName LIKE '%' || :query || '%' ORDER BY fullName")
    fun search(query: String): Flow<List<Teacher>>

    @Upsert
    suspend fun upsertAll(teachers: List<Teacher>)

    @Query("DELETE FROM teachers")
    suspend fun deleteAll()
}
