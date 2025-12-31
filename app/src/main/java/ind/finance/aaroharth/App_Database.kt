package ind.finance.aaroharth

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Transaction_Info::class,
        Account_Info::class
    ],
    version = 1,
    exportSchema = false
)
abstract class App_Database : RoomDatabase() {
    abstract fun transactionDao(): Transaction_Dao
    abstract fun accountDao(): Account_Dao
}
