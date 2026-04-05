package ind.finance.aaroharth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class Notification_History_Info(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String,
    val createdAt: Long,
    val expiresAt: Long
)