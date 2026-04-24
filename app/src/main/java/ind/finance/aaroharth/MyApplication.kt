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
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
        registerConnectivityListener() // ← call it here
    }

    // ✅ Moved outside onCreate() as a proper class method
    private fun registerConnectivityListener() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    transactionRepository.syncUnsynced(userId)
                    accountRepository.syncUnsynced(userId)
                }
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        AppLockManager.lastBackgroundTime = System.currentTimeMillis()
        AppLockManager.isAppLocked = true
    }
}