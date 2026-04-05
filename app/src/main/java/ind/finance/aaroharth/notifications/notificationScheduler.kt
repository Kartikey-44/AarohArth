package ind.finance.aaroharth.notifications

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val MORNING_WORK_NAME = "daily_entry_morning_reminder"
    private const val AFTERNOON_WORK_NAME = "daily_entry_afternoon_reminder"
    private const val EVENING_WORK_NAME = "daily_entry_evening_reminder"
    private const val MONTHLY_WORK_NAME = "monthly_summary_reminder"

    private const val CLEANUP_WORK_NAME = "notification_cleanup_work"

    const val KEY_TITLE = "title"
    const val KEY_MESSAGE = "message"
    const val KEY_TYPE = "type"

    fun enableNotifications(context: Context) {
        scheduleDailyReminder(
            context = context,
            uniqueWorkName = MORNING_WORK_NAME,
            hour = 8,
            minute = 0,
            title = "Good morning 👋",
            message = "Add today’s first transaction and stay on track."
        )

        scheduleDailyReminder(
            context = context,
            uniqueWorkName = AFTERNOON_WORK_NAME,
            hour = 13,
            minute = 0,
            title = "Don’t let receipts disappear",
            message = "Add your spending before you forget."
        )

        scheduleDailyReminder(
            context = context,
            uniqueWorkName = EVENING_WORK_NAME,
            hour = 18,
            minute = 0,
            title = "Day almost done 🌙",
            message = "Don’t forget to add today’s transactions."
        )

        scheduleMonthlySummary(context)
    }

    fun disableNotifications(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(MORNING_WORK_NAME)
        wm.cancelUniqueWork(AFTERNOON_WORK_NAME)
        wm.cancelUniqueWork(EVENING_WORK_NAME)
        wm.cancelUniqueWork(MONTHLY_WORK_NAME)
    }

    fun testNotificationIn10Seconds(context: Context) {
        val inputData = Data.Builder()
            .putString(KEY_TITLE, "Test Notification")
            .putString(KEY_MESSAGE, "Notifications are working correctly.")
            .putString(KEY_TYPE, "test")
            .build()

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun rescheduleDailyReminder(
        context: Context,
        uniqueWorkName: String,
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        scheduleDailyReminder(context, uniqueWorkName, hour, minute, title, message)
    }

    fun rescheduleMonthlySummary(context: Context) {
        scheduleMonthlySummary(context)
    }

    private fun scheduleDailyReminder(
        context: Context,
        uniqueWorkName: String,
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        val delay = getDelayUntil(hour, minute)

        val inputData = Data.Builder()
            .putString(KEY_TITLE, title)
            .putString(KEY_MESSAGE, message)
            .putString(KEY_TYPE, uniqueWorkName)
            .build()

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            request
        )

        Log.d("NOTIFICATION_DEBUG", "Scheduled $uniqueWorkName in ${delay / 1000} seconds")
    }

    private fun scheduleMonthlySummary(context: Context) {
        val delay = getDelayUntilEndOfMonth(10, 0) // Last day of month at 10:00 AM

        val request = OneTimeWorkRequestBuilder<MonthlySummaryWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MONTHLY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )

        Log.d("NOTIFICATION_DEBUG", "Scheduled monthly summary in ${delay / 1000} seconds")
    }

    private fun getDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun getDelayUntilEndOfMonth(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }
        }
        return target.timeInMillis - now.timeInMillis
    }

    fun scheduleNotificationCleanup(context: Context) {
        val request = OneTimeWorkRequestBuilder<NotificationCleanupWorker>()
            .setInitialDelay(6, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            CLEANUP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}