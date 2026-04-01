package ind.finance.aaroharth.repositories

import ind.finance.aaroharth.data.local.BudgetDao
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info

class BudgetRepository(private val budgetDao: BudgetDao) {

    suspend fun insertBudget(budget: Budget_Info) {
        budgetDao.insertInfo(budget)
    }

    suspend fun getBudgetById(id: Long): Budget_Info {
        return budgetDao.getBudgetById(id)
    }

    suspend fun getBudgetSummary(monthKey: String): List<BudgetSummary> {
        return budgetDao.getBudgetSummary(monthKey)
    }

    suspend fun updateBudget(id: Long, category: String, amount: Long) {
        budgetDao.updateBudget(id, category, amount)
    }

    suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    suspend fun budgetExists(category: String, monthKey: String): Boolean {
        return budgetDao.budgetExists(category, monthKey) > 0
    }
}