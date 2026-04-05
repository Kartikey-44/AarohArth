package ind.finance.aaroharth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ind.finance.aaroharth.data.model.Account_Info
import ind.finance.aaroharth.data.model.Budget_Info
import ind.finance.aaroharth.data.model.Notification_History_Info
import ind.finance.aaroharth.data.model.Transaction_Info

@Database(
    entities = [
        Transaction_Info::class,
        Account_Info::class,
        Budget_Info::class,
        Notification_History_Info::class
    ],
    version = 7,
    exportSchema = false
)
abstract class App_Database : RoomDatabase() {
    abstract fun transactionDao(): Transaction_Dao
    abstract fun accountDao(): Account_Dao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: App_Database? = null

        fun getInstance(context: Context): App_Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    App_Database::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
