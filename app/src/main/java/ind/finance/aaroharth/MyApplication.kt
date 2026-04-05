package ind.finance.aaroharth

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import ind.finance.aaroharth.data.local.App_Database
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.BudgetRepository
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.repositories.NotificationRepository

class MyApplication : Application(), LifecycleObserver {

    private val database by lazy { App_Database.getInstance(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val accountRepository by lazy {
        AccountRepository(
            database.accountDao(),
            database.transactionDao()
        )
    }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        AppLockManager.lastBackgroundTime = System.currentTimeMillis()
        AppLockManager.isAppLocked = true
    }
}
