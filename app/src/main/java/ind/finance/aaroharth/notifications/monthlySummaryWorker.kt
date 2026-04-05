package ind.finance.aaroharth.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result

class MonthlySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.createNotificationChannel(applicationContext)

        NotificationHelper.showMonthlySummary(
            applicationContext,
            "See where your money went this month."
        )

        NotificationScheduler.rescheduleMonthlySummary(applicationContext)

        return Result.success()
    }
}