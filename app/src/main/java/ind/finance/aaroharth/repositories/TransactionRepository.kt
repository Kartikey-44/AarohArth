package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.data.model.filterchart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class TransactionRepository(private val transactionDao: Transaction_Dao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertTransaction(transaction: Transaction_Info, userId: String) {
        //  Always insert locally with isSynced = FALSE
        // This guarantees it's saved offline regardless of network
        val id = transactionDao.insertTransaction(transaction.copy(isSynced = false))

        //  Try Firestore separately with timeout — never blocks the local save message
        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(id.toString())
                    .set(transaction.copy(id = id, isSynced = true))
                    .await()
                transactionDao.setSynced(id) //  Only mark synced after Firestore confirms
            }
        } catch (e: Exception) {
            // Offline or error — stays isSynced=false, syncUnsynced() will retry later
        }
    }

    suspend fun updateTransaction(transaction: Transaction_Info, userId: String) {
        //  Always update locally with isSynced = FALSE first
        transactionDao.updateTransaction(transaction.copy(isSynced = false))

        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(transaction.id.toString())
                    .set(transaction.copy(isSynced = true))
                    .await()
                transactionDao.setSynced(transaction.id)
            }
        } catch (e: Exception) {
            // Will retry via syncUnsynced()
        }
    }

    suspend fun deleteTransactionById(id: Long, userId: String) {
        //  Always delete locally first
        transactionDao.deleteById(id)

        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(id.toString())
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            // Local delete already done — Firestore will be stale until manual sync
            // Consider maintaining a "pendingDeletes" table for robust offline delete sync
        }
    }

    //  Call this when network becomes available — picks up everything missed offline
    suspend fun syncUnsynced(userId: String) {
        val unsynced = transactionDao.getUnsynced()
        unsynced.forEach { transaction ->
            try {
                withTimeoutOrNull(100) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transaction.id.toString())
                        .set(transaction.copy(isSynced = true))
                        .await()
                    transactionDao.setSynced(transaction.id)
                }
            } catch (e: Exception) {
                // Will retry next time syncUnsynced() is called
            }
        }
    }

    // ── Read methods (unchanged) ──────────────────────────────────────────────

    suspend fun getTransactionsByCategory(category: String, type: String) =
        transactionDao.getTransactionsByCategory(category, type)

    suspend fun getAllTransactions() = transactionDao.getalltransaction()

    fun getAllTransactionsFlow(): Flow<List<Transaction_Info>> =
        transactionDao.getAllTransactionsFlow()

    suspend fun getTransactionsByType(type: String) = transactionDao.gettransaction(type)

    suspend fun getTransactionById(id: Long) = transactionDao.getTransactionById(id)

    suspend fun getMonthWiseSum(startDate: Long, endDate: Long, type: String) =
        transactionDao.monthWise(startDate, endDate, type)

    suspend fun searchTransactions(query: String, type: String) =
        if (type == "ALL") transactionDao.searchTransactionstype(query)
        else transactionDao.searchTransactions(query, type)

    suspend fun getRecentCo2Transactions(startDate: Long, endDate: Long) =
        transactionDao.getRecentCo2Transactions(startDate, endDate)

    suspend fun getCategoryExpense(fromTime: Long) =
        transactionDao.getCategoryExpense(fromTime)

    suspend fun getDailyExpense(fromTime: Long) =
        transactionDao.getDailyExpense(fromTime)

    suspend fun getTotalExpense(fromTime: Long) =
        transactionDao.getTotalExpense(fromTime)

    suspend fun getCategoryExpenseFiltered(fromTime: Long, type: String) =
        transactionDao.getCategoryExpenseFiltered(fromTime, type)

    suspend fun getCategoryAll(fromTime: Long) =
        transactionDao.getCategoryAll(fromTime)
}