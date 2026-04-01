package ind.finance.aaroharth.repositories

import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.data.model.filterchart

class TransactionRepository(private val transactionDao: Transaction_Dao) {

    suspend fun getTransactionsByCategory(category: String, type: String): List<Transaction_Info> {
        return transactionDao.getTransactionsByCategory(category, type)
    }

    suspend fun insertTransaction(transaction: Transaction_Info) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getAllTransactions(): List<Transaction_Info> {
        return transactionDao.getalltransaction()
    }

    suspend fun getTransactionsByType(type: String): List<Transaction_Info> {
        return transactionDao.gettransaction(type)
    }

    suspend fun updateTransaction(transaction: Transaction_Info) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun getTransactionById(id: Long): Transaction_Info {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getMonthWiseSum(startDate: Long, endDate: Long, type: String): Long {
        return transactionDao.monthWise(startDate, endDate, type)
    }

    suspend fun searchTransactions(query: String, type: String): List<Transaction_Info> {
        return if (type == "ALL") {
            transactionDao.searchTransactionstype(query)
        } else {
            transactionDao.searchTransactions(query, type)
        }
    }

    suspend fun getRecentCo2Transactions(startDate: Long, endDate: Long): List<Transaction_Info> {
        return transactionDao.getRecentCo2Transactions(startDate, endDate)
    }

    suspend fun getCategoryExpense(fromTime: Long): List<CategoryExpense> {
        return transactionDao.getCategoryExpense(fromTime)
    }

    suspend fun getDailyExpense(fromTime: Long): List<filterchart> {
        return transactionDao.getDailyExpense(fromTime)
    }

    suspend fun getTotalExpense(fromTime: Long): Double {
        return transactionDao.getTotalExpense(fromTime)
    }
}