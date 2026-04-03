package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.Account_Dao
import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.Account_Info
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AccountRepository(
    private val accountDao: Account_Dao,
    private val transactionDao: Transaction_Dao
) {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllAccountsFlow(): Flow<List<Account_Info>> = accountDao.getAllAccountsFlow()

    fun getNumberOfAccountsFlow(): Flow<Int> = accountDao.getNumberOfAccountsFlow()

    suspend fun insertAccount(account: Account_Info, userId: String) {
        val id = accountDao.insertAccount(account)
        val updatedAccount = account.copy(id = id, isSynced = true)

        try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(id.toString())
                .set(updatedAccount)
                .await()
            accountDao.setSynced(id)
        } catch (e: Exception) {
            // Leave isSynced = false for background sync
        }
    }

    suspend fun updateAccount(account: Account_Info, userId: String) {
        accountDao.updateaccountinfo(account.copy(isSynced = false))
        
        try {
            val cloudAccount = account.copy(isSynced = true)
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id.toString())
                .set(cloudAccount)
                .await()
            accountDao.setSynced(account.id)
        } catch (e: Exception) {
            // Stay unsynced
        }
    }

    suspend fun updateBalance(balance: Long, accountName: String, userId: String) {
        accountDao.updatebalance(balance, accountName)
        // Sync the full account object to cloud
        val account = accountDao.getAllAccounts().find { it.accountName == accountName }
        account?.let { updateAccount(it, userId) }
    }

    suspend fun deleteAccountById(id: Long, userId: String) {
        accountDao.deletebyid(id)
        try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(id.toString())
                .delete()
                .await()
        } catch (e: Exception) { }
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = accountDao.getUnsynced()
        unsynced.forEach {
            try {
                val synced = it.copy(isSynced = true)
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(it.id.toString())
                    .set(synced)
                    .await()
                accountDao.setSynced(it.id)
            } catch (e: Exception) { }
        }
    }

    suspend fun getAllAccounts() = accountDao.getAllAccounts()
    suspend fun getAllAccountTypes() = accountDao.getAllAccountType()
    suspend fun getBalance(accountName: String) = accountDao.getbalance(accountName)
    suspend fun searchAccounts(query: String) = accountDao.searchAccounts(query)
    suspend fun filterAccountsByType(accountType: String) = accountDao.filter(accountType)
    suspend fun getCurrentBalance() = accountDao.currentbalance()
    suspend fun getNumberOfAccounts() = accountDao.numberOfAccounts()
    suspend fun getAccountById(id: Long) = accountDao.getAccountById(id)
    suspend fun getTotalBalance() = accountDao.gettotalbalance()
    suspend fun getTransactionCountForAccount(accountName: String) = transactionDao.getNoOfTransaction(accountName)
}
