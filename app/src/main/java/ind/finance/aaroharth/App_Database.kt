package ind.finance.aaroharth

import androidx.room.Database
import androidx.room.RoomDatabase

@Database
    (entities = [Transaction_Info::class],
    [Account_Info::class], version = 1)
abstract class App_Database: RoomDatabase(){
    abstract val Transaction_Dao: Transaction_Dao
    abstract val Account_Dao: Account_Dao
}