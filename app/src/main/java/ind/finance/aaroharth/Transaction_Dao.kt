package ind.finance.aaroharth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface Transaction_Dao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction_Info)
    @Query("SELECT * FROM TransactionTable")
    suspend fun getalltransaction(): List<Transaction_Info>
    @Query("SELECT * FROM TransactionTable WHERE transactionType=:type")
    suspend fun gettransaction(type: String): List<Transaction_Info>


    @Query(
        """
    SELECT * FROM TransactionTable
    WHERE transactionType LIKE '%' || :query || '%'
       OR otherParty LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
       OR transactionMedium LIKE '%' || :query || '%'
       OR transactionWay LIKE '%' || :query || '%'
       OR remark LIKE '%' || :query || '%'
       OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
       OR CAST(dateAndTime AS TEXT) LIKE '%' || :query || '%'
"""
    )
    suspend fun searchTransactionstype(query: String): List<Transaction_Info>


    @Query(
        """
    SELECT * FROM TransactionTable
    WHERE transactionType = :type
      AND (
           otherParty LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR transactionMedium LIKE '%' || :query || '%'
        OR transactionWay LIKE '%' || :query || '%'
        OR remark LIKE '%' || :query || '%'
        OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
        OR CAST(dateAndTime AS TEXT) LIKE '%' || :query || '%'
      )
"""
    )

    suspend fun searchTransactions(query: String,type: String): List<Transaction_Info>

    @Query("SELECT SUM(amount)FROM TransactionTable WHERE dateAndTime BETWEEN  :startDate AND :endDate " +
            "AND transactionType= :type")
    suspend fun monthWise(startDate: Long,endDate: Long,type: String): Long


    @Query("SELECT * FROM TransactionTable WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction_Info


    @Update
    suspend fun updateTransaction(transaction: Transaction_Info)


    @Query("DELETE FROM TransactionTable WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactiontable where transactionWay=:way")
    suspend fun getNoOfTransaction(way: String?): Long

}




