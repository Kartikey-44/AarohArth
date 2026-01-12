package ind.finance.aaroharth

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Transaction_Info::class,
        Account_Info::class
    ],
    version = 4,
    exportSchema = false
)
abstract class App_Database : RoomDatabase() {
    abstract fun transactionDao(): Transaction_Dao
    abstract fun accountDao(): Account_Dao

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
