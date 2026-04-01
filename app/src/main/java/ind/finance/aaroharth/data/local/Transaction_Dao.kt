package ind.finance.aaroharth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.Co2CategoryItem
import ind.finance.aaroharth.data.model.Co2LineItem
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.data.model.filterchart

@Dao
interface Transaction_Dao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction_Info)

    @Query("SELECT * FROM TransactionTable ORDER BY dateAndTime DESC")
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
    suspend fun searchTransactions(query: String, type: String): List<Transaction_Info>

    @Query(
        "SELECT SUM(amount)FROM TransactionTable WHERE dateAndTime BETWEEN  :startDate AND :endDate " +
                "AND transactionType= :type"
    )
    suspend fun monthWise(startDate: Long, endDate: Long, type: String): Long

    @Query("SELECT * FROM TransactionTable WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction_Info

    @Update
    suspend fun updateTransaction(transaction: Transaction_Info)

    @Query("DELETE FROM TransactionTable WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactiontable where transactionWay=:way")
    suspend fun getNoOfTransaction(way: String?): Long

    @Query(
        "SELECT \n" +
                "    date(dateAndTime / 86400000) AS day,\n" +
                "    SUM(amount) AS total\n" +
                "FROM TransactionTable\n" +
                "WHERE transactionType = 'Expense'\n" +
                "GROUP BY day\n" +
                "ORDER BY day ASC\n" +
                "LIMIT 30;\n"
    )
    suspend fun get30Days(): List<filterchart>

    @Query(
        """
    SELECT ( dateAndTime/ 86400000) AS day,
           SUM(amount) AS total
    FROM TransactionTable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
    GROUP BY day
    ORDER BY day ASC
"""
    )
    fun getDailyExpense(from: Long): List<filterchart>

    @Query(
        """
    SELECT SUM(amount)
    FROM transactiontable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
"""
    )
    fun getTotalExpense(from: Long): Double

    @Query(
        """
    SELECT category, SUM(amount) AS total
    FROM transactiontable
    WHERE transactionType='Expense'
      AND dateAndTime >= :from
    GROUP BY category
    ORDER BY total DESC
"""
    )
    fun getCategoryExpense(from: Long): List<CategoryExpense>

    //Categories Transaction List (getTransactionsByCategory)
    @Query(
        """
    SELECT * FROM TransactionTable 
    WHERE UPPER(category) = UPPER(:category) 
    AND (:type = 'ALL' OR transactionType = :type)
    ORDER BY dateAndTime DESC
"""
    )
    suspend fun getTransactionsByCategory(category: String, type: String): List<Transaction_Info>

    @Query(
        """
    SELECT category, SUM(carbonImpact) as totalCO2, COUNT(*) as count
    FROM TransactionTable 
    WHERE dateAndTime BETWEEN :startDate AND :endDate
    AND transactionType = 'Expense'  
    GROUP BY category 
    ORDER BY totalCO2 DESC LIMIT 5
"""
    )
    suspend fun getCo2ByCategoryPeriod(startDate: Long, endDate: Long): List<Co2CategoryItem>

    // (Line Chart)
    @Query(
        """
    SELECT (dateAndTime/86400000) as day, SUM(carbonImpact) as totalCO2
    FROM TransactionTable
    WHERE dateAndTime BETWEEN :startDate AND :endDate
    AND transactionType = 'Expense'
    GROUP BY day ORDER BY day ASC
"""
    )
    suspend fun getCo2LineChart(startDate: Long, endDate: Long): List<Co2LineItem>

    // Recent transactions list
    @Query(
        """
    SELECT * FROM TransactionTable 
    WHERE dateAndTime BETWEEN :startDate AND :endDate
    AND transactionType = 'Expense' AND carbonImpact > 0
    ORDER BY dateAndTime DESC LIMIT 5
"""
    )
    suspend fun getRecentCo2Transactions(startDate: Long, endDate: Long): List<Transaction_Info>

    @Query(
        """
    SELECT category, SUM(amount) AS total
    FROM transactiontable
    WHERE transactionType = :type
      AND dateAndTime >= :from
    GROUP BY category
    ORDER BY total DESC
"""
    )
    fun getCategoryExpenseFiltered(from: Long, type: String): List<CategoryExpense>

    @Query(
        """
    SELECT category, SUM(amount) AS total
    FROM transactiontable
    WHERE dateAndTime >= :from
    GROUP BY category
    ORDER BY total DESC
"""
    )
    fun getCategoryAll(from: Long): List<CategoryExpense>
}
