package ind.finance.aaroharth.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import ind.finance.aaroharth.MyApplication

class NotificationCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as MyApplication
            app.notificationRepository.deleteExpiredNotifications()
            NotificationScheduler.scheduleNotificationCleanup(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}