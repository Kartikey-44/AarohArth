package ind.finance.aaroharth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Transaction_Dao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction_Info)
    @Query("SELECT * FROM TransactionTable")
    suspend fun getalltransaction(): List<Transaction_Info>
}