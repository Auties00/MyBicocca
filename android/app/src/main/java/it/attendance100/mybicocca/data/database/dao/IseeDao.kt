package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import kotlinx.coroutines.flow.Flow

@Dao
interface IseeDao {
    @Query("SELECT * FROM isee_declarations ORDER BY academicYear DESC LIMIT 1")
    fun observeLatest(): Flow<IseeDeclaration?>

    @Query("SELECT * FROM isee_declarations WHERE academicYear = :academicYear LIMIT 1")
    fun observeByYear(academicYear: Long): Flow<IseeDeclaration?>

    @Upsert
    suspend fun upsert(declaration: IseeDeclaration)

    @Upsert
    suspend fun upsertAll(declarations: List<IseeDeclaration>)

    @Query("DELETE FROM isee_declarations")
    suspend fun deleteAll()
}
