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


    @Query("SELECT \n" +
            "    date(dateAndTime / 86400000) AS day,\n" +
            "    SUM(amount) AS total\n" +
            "FROM TransactionTable\n" +
            "WHERE transactionType = 'Expense'\n" +
            "GROUP BY day\n" +
            "ORDER BY day ASC\n" +
            "LIMIT 30;\n")
    suspend fun get30Days():List<filterchart>
    @Query("""
    SELECT ( dateAndTime/ 86400000) AS day,
           SUM(amount) AS total
    FROM TransactionTable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
    GROUP BY day
    ORDER BY day ASC
""")
    fun getDailyExpense(from: Long): List<filterchart>

    @Query("""
    SELECT SUM(amount)
    FROM transactiontable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
""")
    fun getTotalExpense(from: Long): Double

    @Query("""
    SELECT category, SUM(amount) AS total
    FROM transactiontable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
    GROUP BY category
    ORDER BY total DESC
""")
    fun getCategoryExpense(from: Long): List<CategoryExpense>

}


    //Categories Transaction List (getTransactionsByCategory)
    @Query("""
    SELECT * FROM TransactionTable 
    WHERE UPPER(category) = UPPER(:category) 
    AND (:type = 'ALL' OR transactionType = :type)
    ORDER BY dateAndTime DESC
""")
    suspend fun getTransactionsByCategory(category: String, type: String): List<Transaction_Info>


