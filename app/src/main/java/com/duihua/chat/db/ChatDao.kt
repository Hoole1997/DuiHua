package com.duihua.chat.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duihua.chat.bean.IMMessage
import com.duihua.chat.bean.OtherUserInfo

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateUserInfo(userInfo: OtherUserInfo)

    @Query("SELECT * FROM OtherUserInfo WHERE id = :id")
    suspend fun getUserById(id: Long): OtherUserInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateMessage(message: IMMessage)

    @Query("""
        SELECT * FROM im_message 
        WHERE (fromUserId = :userId1 AND toUserId = :userId2)
           OR (fromUserId = :userId2 AND toUserId = :userId1)
        ORDER BY timestamp ASC
    """)
    suspend fun getMessagesBetweenUsers(userId1: Long, userId2: Long): List<IMMessage>

    @Query("SELECT * FROM im_message WHERE fromUserId = :fromUserId AND toUserId = :toUserId ORDER BY timestamp ASC")
    fun getMessagesByUserIds(fromUserId: Long, toUserId: Long): List<IMMessage>

    @Query("SELECT * FROM im_message ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<IMMessage>

    @Query("""
        WITH user_message_counts AS (
            SELECT 
                CASE 
                    WHEN fromUserId = :currentUserId THEN toUserId 
                    ELSE fromUserId 
                END as other_user_id,
                COUNT(*) as message_count
            FROM im_message
            WHERE (fromUserId = :currentUserId OR toUserId = :currentUserId)
            GROUP BY other_user_id
        ),
        latest_messages AS (
            SELECT 
                CASE 
                    WHEN fromUserId = :currentUserId THEN toUserId 
                    ELSE fromUserId 
                END as other_user_id,
                MAX(timestamp) as max_timestamp
            FROM im_message
            WHERE (fromUserId = :currentUserId OR toUserId = :currentUserId)
            GROUP BY other_user_id
        )
        SELECT DISTINCT m.* FROM im_message m
        INNER JOIN latest_messages lm ON 
            (m.fromUserId = :currentUserId AND m.toUserId = lm.other_user_id AND m.timestamp = lm.max_timestamp)
            OR 
            (m.toUserId = :currentUserId AND m.fromUserId = lm.other_user_id AND m.timestamp = lm.max_timestamp)
        INNER JOIN user_message_counts umc ON 
            CASE 
                WHEN m.fromUserId = :currentUserId THEN m.toUserId 
                ELSE m.fromUserId 
            END = umc.other_user_id
        WHERE (m.duration = "0" AND m.toUserId = :currentUserId)
           OR (umc.message_count = 1 AND m.fromUserId = :currentUserId)
        ORDER BY m.timestamp DESC
    """)
    suspend fun getLatestMessagesForEachUser(currentUserId: Long): List<IMMessage>
}