package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.BudgetDao
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info
import kotlinx.coroutines.tasks.await

class BudgetRepository(private val budgetDao: BudgetDao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertBudget(budget: Budget_Info, userId: String) {
        val id = budgetDao.insertInfo(budget)
        val updatedBudget = budget.copy(id = id, isSynced = true)

        try {
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(id.toString())
                .set(updatedBudget)
                .await()
            budgetDao.setSynced(id)
        } catch (e: Exception) { }
    }

    suspend fun updateBudget(budget: Budget_Info, userId: String) {
        budgetDao.updateBudgetInfo(budget.copy(isSynced = false))
        
        try {
            val syncedBudget = budget.copy(isSynced = true)
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(budget.id.toString())
                .set(syncedBudget)
                .await()
            budgetDao.setSynced(budget.id)
        } catch (e: Exception) { }
    }

    suspend fun deleteBudgetById(id: Long, userId: String) {
        budgetDao.deleteBudgetById(id)
        try {
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(id.toString())
                .delete()
                .await()
        } catch (e: Exception) { }
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = budgetDao.getUnsynced()
        unsynced.forEach {
            try {
                val synced = it.copy(isSynced = true)
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(it.id.toString())
                    .set(synced)
                    .await()
                budgetDao.setSynced(it.id)
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
