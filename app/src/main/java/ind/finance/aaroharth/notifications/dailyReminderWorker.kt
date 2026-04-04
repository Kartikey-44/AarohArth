package ind.finance.aaroharth.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.createNotificationChannel(applicationContext)

        val title = inputData.getString(NotificationScheduler.KEY_TITLE) ?: "Reminder"
        val message = inputData.getString(NotificationScheduler.KEY_MESSAGE)
            ?: "Add your transaction."
        val type = inputData.getString(NotificationScheduler.KEY_TYPE) ?: ""

        NotificationHelper.showDailyReminder(applicationContext, title, message)

        when (type) {
            "daily_entry_morning_reminder" -> {
                NotificationScheduler.rescheduleDailyReminder(
                    applicationContext,
                    "daily_entry_morning_reminder",
                    8,
                    0,
                    "Good morning 👋",
                    "Add today’s first transaction and stay on track."
                )
            }

            "daily_entry_afternoon_reminder" -> {
                NotificationScheduler.rescheduleDailyReminder(
                    applicationContext,
                    "daily_entry_afternoon_reminder",
                    13,
                    0,
                    "Don’t let receipts disappear",
                    "Add your spending before you forget."
                )
            }

            "daily_entry_evening_reminder" -> {
                NotificationScheduler.rescheduleDailyReminder(
                    applicationContext,
                    "daily_entry_evening_reminder",
                    18,
                    0,
                    "Day almost done 🌙",
                    "Don’t forget to add today’s transactions."
                )
            }
        }

        return Result.success()
    }
}