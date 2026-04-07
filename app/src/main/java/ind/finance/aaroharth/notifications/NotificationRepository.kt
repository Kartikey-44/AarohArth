package ind.finance.aaroharth.repositories

import ind.finance.aaroharth.data.local.NotificationDao
import ind.finance.aaroharth.data.model.Notification_History_Info

class NotificationRepository(
    private val notificationDao: NotificationDao
) {

    suspend fun saveNotification(title: String, message: String, type: String) {
        val now = System.currentTimeMillis()
        val expiry = now + 24 * 60 * 60 * 1000L

        notificationDao.insertNotification(
            Notification_History_Info(
                title = title,
                message = message,
                type = type,
                createdAt = now,
                expiresAt = expiry
            )
        )
    }

    suspend fun getActiveNotifications(): List<Notification_History_Info> {
        return notificationDao.getActiveNotifications(System.currentTimeMillis())
    }

    suspend fun deleteExpiredNotifications() {
        notificationDao.deleteExpiredNotifications(System.currentTimeMillis())
    }

    suspend fun getActiveNotificationCount(): Int {
        return notificationDao.getActiveNotificationCount(System.currentTimeMillis())
    }
}