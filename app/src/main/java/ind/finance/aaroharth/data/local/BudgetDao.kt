package ind.finance.aaroharth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info

@Dao
interface BudgetDao {

    /* ---------- INSERT ---------- */

    @Insert
    suspend fun insertInfo(budget: Budget_Info): Long

    /* ---------- READ ---------- */

    @Query("SELECT * FROM BudgetTable WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget_Info

    @Query("""
    SELECT 
        b.id AS id,
        b.category AS category,
        b.amount AS budgetLimit,
        IFNULL(SUM(t.amount), 0) AS spent
    FROM BudgetTable b
    LEFT JOIN TransactionTable t
        ON b.category = t.category
        AND t.monthKey = b.monthKey
        AND t.transactionType = 'Expense'
    WHERE b.monthKey = :monthKey
    GROUP BY b.id
    """)
    suspend fun getBudgetSummary(monthKey: String): List<BudgetSummary>

    /* ---------- UPDATE ---------- */

    @Update
    suspend fun updateBudgetInfo(budget: Budget_Info)

    @Query("""
    UPDATE BudgetTable
    SET category = :category,
        amount = :amount,
        isSynced = 0
    WHERE id = :id
    """)
    suspend fun updateBudget(
        id: Long,
        category: String,
        amount: Long
    )

    /* ---------- DELETE ---------- */

    @Query("DELETE FROM BudgetTable WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    /* ---------- VALIDATION ---------- */

    @Query("""
    SELECT COUNT(*) FROM BudgetTable 
    WHERE category = :category AND monthKey = :monthKey
    """)
    suspend fun budgetExists(category: String, monthKey: String): Int

    /* ---------- SYNC ---------- */

    @Query("SELECT * FROM BudgetTable WHERE isSynced = 0")
    suspend fun getUnsynced(): List<Budget_Info>

    @Query("UPDATE BudgetTable SET isSynced = 1 WHERE id = :id")
    suspend fun setSynced(id: Long)
}
