package ind.finance.aaroharth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ind.finance.aaroharth.BudgetSummary
import ind.finance.aaroharth.Budget_Info

@Dao
interface BudgetDao {

    /* ---------- INSERT ---------- */

    @Insert
    suspend fun insertInfo(budget: Budget_Info)

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

    @Query("""
    UPDATE BudgetTable
    SET category = :category,
        amount = :amount
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


}
