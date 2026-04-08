package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.data.model.filterchart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TransactionRepository(private val transactionDao: Transaction_Dao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertTransaction(transaction: Transaction_Info, userId: String) {
        val id = transactionDao.insertTransaction(transaction)
        val updatedTransaction = transaction.copy(id = id, isSynced = true)

        try {
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(id.toString())
                .set(updatedTransaction)
                .await()
            transactionDao.setSynced(id)
        } catch (e: Exception) {
            // Stay unsynced locally
        }
    }

    suspend fun updateTransaction(transaction: Transaction_Info, userId: String) {
        transactionDao.updateTransaction(transaction.copy(isSynced = false))
        
        try {
            val syncedTransaction = transaction.copy(isSynced = true)
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id.toString())
                .set(syncedTransaction)
                .await()
            transactionDao.setSynced(transaction.id)
        } catch (e: Exception) {
            // Stay unsynced
        }
    }

    suspend fun deleteTransactionById(id: Long, userId: String) {
        transactionDao.deleteById(id)
        try {
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(id.toString())
                .delete()
                .await()
        } catch (e: Exception) { }
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = transactionDao.getUnsynced()
        unsynced.forEach {
            try {
                val synced = it.copy(isSynced = true)
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(it.id.toString())
                    .set(synced)
                    .await()
                transactionDao.setSynced(it.id)
            } catch (e: Exception) { }
        }
    }

    suspend fun getTransactionsByCategory(category: String, type: String): List<Transaction_Info> {
        return transactionDao.getTransactionsByCategory(category, type)
    }

    suspend fun getAllTransactions(): List<Transaction_Info> {
        return transactionDao.getalltransaction()
    }

    fun getAllTransactionsFlow(): Flow<List<Transaction_Info>> {
        return transactionDao.getAllTransactionsFlow()
    }

    suspend fun getTransactionsByType(type: String): List<Transaction_Info> {
        return transactionDao.gettransaction(type)
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

    suspend fun getCategoryExpenseFiltered(fromTime: Long, type: String): List<CategoryExpense> {
        return transactionDao.getCategoryExpenseFiltered(fromTime, type)
    }

    suspend fun getCategoryAll(fromTime: Long): List<CategoryExpense> {
        return transactionDao.getCategoryAll(fromTime)
    }
}
