package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {

    @Query(
        "SELECT * FROM elearning_forums " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY name"
    )
    fun observeForCourse(accountId: String, courseId: Int): Flow<List<ForumEntity>>

    @Query("SELECT * FROM elearning_forums WHERE account_id = :accountId AND forum_id = :forumId")
    fun observe(accountId: String, forumId: Int): Flow<ForumEntity?>

    @Query(
        "SELECT * FROM elearning_forum_discussions " +
            "WHERE account_id = :accountId AND forum_id = :forumId " +
            "ORDER BY is_pinned DESC, time_modified_ms DESC"
    )
    fun observeDiscussions(accountId: String, forumId: Int): Flow<List<DiscussionEntity>>

    @Query(
        "SELECT * FROM elearning_forum_posts " +
            "WHERE account_id = :accountId AND discussion_id = :discussionId " +
            "ORDER BY created_at_ms"
    )
    fun observePosts(accountId: String, discussionId: Int): Flow<List<PostEntity>>

    @Upsert
    suspend fun upsertForums(rows: List<ForumEntity>)

    @Upsert
    suspend fun upsertDiscussions(rows: List<DiscussionEntity>)

    @Upsert
    suspend fun upsertPosts(rows: List<PostEntity>)

    @Query("DELETE FROM elearning_forums WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteForumsForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_forum_discussions WHERE account_id = :accountId AND forum_id = :forumId")
    suspend fun deleteDiscussionsForForum(accountId: String, forumId: Int)

    @Query("DELETE FROM elearning_forum_posts WHERE account_id = :accountId AND discussion_id = :discussionId")
    suspend fun deletePostsForDiscussion(accountId: String, discussionId: Int)

    @Transaction
    suspend fun replaceForumsForCourse(accountId: String, courseId: Int, rows: List<ForumEntity>) {
        deleteForumsForCourse(accountId, courseId)
        if (rows.isNotEmpty()) upsertForums(rows)
    }

    @Transaction
    suspend fun replaceDiscussionsForForum(accountId: String, forumId: Int, rows: List<DiscussionEntity>) {
        deleteDiscussionsForForum(accountId, forumId)
        if (rows.isNotEmpty()) upsertDiscussions(rows)
    }

    @Transaction
    suspend fun replacePostsForDiscussion(accountId: String, discussionId: Int, rows: List<PostEntity>) {
        deletePostsForDiscussion(accountId, discussionId)
        if (rows.isNotEmpty()) upsertPosts(rows)
    }

    @Query("DELETE FROM elearning_forums WHERE account_id = :accountId")
    suspend fun deleteAllForums(accountId: String)

    @Query("DELETE FROM elearning_forum_discussions WHERE account_id = :accountId")
    suspend fun deleteAllDiscussions(accountId: String)

    @Query("DELETE FROM elearning_forum_posts WHERE account_id = :accountId")
    suspend fun deleteAllPosts(accountId: String)

    @Transaction
    suspend fun clearAllForAccount(accountId: String) {
        deleteAllPosts(accountId)
        deleteAllDiscussions(accountId)
        deleteAllForums(accountId)
    }
}
