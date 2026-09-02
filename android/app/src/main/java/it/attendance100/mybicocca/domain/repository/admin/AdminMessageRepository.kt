package it.attendance100.mybicocca.domain.repository.admin

import it.attendance100.mybicocca.domain.model.admin.AdminMessage
import kotlinx.coroutines.flow.Flow

interface AdminMessageRepository {
    /** Connects to remote config and exposes the current announcement, or null if none/dismissed. */
    fun observeMessage(): Flow<AdminMessage?>
    
    /** Marks the given message ID as read so it won't be emitted again. */
    suspend fun dismissMessage(id: String)
    
    /** Refreshes the remote config cache. */
    suspend fun fetch()
}
