package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.user.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun observeUser(): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): User?

    @Upsert
    suspend fun upsert(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
