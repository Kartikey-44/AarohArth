package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.BudgetDao
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class BudgetRepository(private val budgetDao: BudgetDao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertBudget(budget: Budget_Info, userId: String) {
        // Always insert locally with isSynced = false
        val id = budgetDao.insertInfo(budget.copy(isSynced = false))

        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(id.toString())
                    .set(budget.copy(id = id, isSynced = true))
                    .await()
                budgetDao.setSynced(id)
            }
        } catch (e: Exception) { }
    }

    suspend fun updateBudget(budget: Budget_Info, userId: String) {
        // Always update locally with isSynced = false
        budgetDao.updateBudgetInfo(budget.copy(isSynced = false))
        
        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(budget.id.toString())
                    .set(budget.copy(isSynced = true))
                    .await()
                budgetDao.setSynced(budget.id)
            }
        } catch (e: Exception) { }
    }

    suspend fun deleteBudgetById(id: Long, userId: String) {
        // Always delete locally first
        budgetDao.deleteBudgetById(id)
        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(id.toString())
                    .delete()
                    .await()
            }
        } catch (e: Exception) { }
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = budgetDao.getUnsynced()
        unsynced.forEach {
            try {
                withTimeoutOrNull(100) {
                    val synced = it.copy(isSynced = true)
                    firestore.collection("users")
                        .document(userId)
                        .collection("budgets")
                        .document(it.id.toString())
                        .set(synced)
                        .await()
                    budgetDao.setSynced(it.id)
                }
            } catch (e: Exception) { }
        }
    }

    suspend fun getBudgetById(id: Long): Budget_Info {
        return budgetDao.getBudgetById(id)
    }

    suspend fun getBudgetSummary(monthKey: String): List<BudgetSummary> {
        return budgetDao.getBudgetSummary(monthKey)
    }

    suspend fun budgetExists(category: String, monthKey: String): Boolean {
        return budgetDao.budgetExists(category, monthKey) > 0
    }
}
