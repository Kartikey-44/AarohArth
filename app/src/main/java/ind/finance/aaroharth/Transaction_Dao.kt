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
    @Query("SELECT * FROM TransactionTable WHERE transactionType=:type")
    suspend fun gettransaction(type: String): List<Transaction_Info>


    @Query("""
    SELECT * FROM TransactionTable
    WHERE transactionType LIKE '%' || :query || '%'
       OR otherParty LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
       OR transacctionMedium LIKE '%' || :query || '%'
       OR transactionWay LIKE '%' || :query || '%'
       OR remark LIKE '%' || :query || '%'
       OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
       OR CAST(dateAndTime AS TEXT) LIKE '%' || :query || '%'
""")
    suspend fun searchTransactionstype(query: String): List<Transaction_Info>


    @Query("""
    SELECT * FROM TransactionTable
    WHERE transactionType = :type
      AND (
           otherParty LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR transacctionMedium LIKE '%' || :query || '%'
        OR transactionWay LIKE '%' || :query || '%'
        OR remark LIKE '%' || :query || '%'
        OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
        OR CAST(dateAndTime AS TEXT) LIKE '%' || :query || '%'
      )
""")

    suspend fun searchTransactions(query: String,type: String): List<Transaction_Info>

    @Query("SELECT SUM(amount)FROM TransactionTable WHERE dateAndTime BETWEEN  :startDate AND :endDate " +
            "AND transactionType= :type")
    suspend fun monthWise(startDate: Long,endDate: Long,type: String): Long


    //Categories Transaction List (getTransactionsByCategory)
    @Query("""
    SELECT * FROM TransactionTable 
    WHERE UPPER(category) = UPPER(:category) 
    AND (:type = 'ALL' OR transactionType = :type)
    ORDER BY dateAndTime DESC
""")
    suspend fun getTransactionsByCategory(category: String, type: String): List<Transaction_Info>


}