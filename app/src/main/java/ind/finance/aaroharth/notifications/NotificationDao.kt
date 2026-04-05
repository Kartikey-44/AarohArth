package ind.finance.aaroharth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ind.finance.aaroharth.data.model.Notification_History_Info

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification_History_Info)

    @Query("SELECT * FROM notification_history WHERE expiresAt > :currentTime ORDER BY createdAt DESC")
    suspend fun getActiveNotifications(currentTime: Long): List<Notification_History_Info>

    @Query("DELETE FROM notification_history WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long)

    @Query("SELECT COUNT(*) FROM notification_history WHERE expiresAt > :currentTime")
    suspend fun getActiveNotificationCount(currentTime: Long): Int
}