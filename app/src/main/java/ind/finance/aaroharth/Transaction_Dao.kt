package ind.finance.aaroharth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Transaction_Dao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction_Info)
    @Query("DELETE FROM TransactionTable")
    suspend fun deletealltransaction()
}