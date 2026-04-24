package ind.finance.aaroharth.repositories

import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.Account_Dao
import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.Account_Info
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class AccountRepository(
    private val accountDao: Account_Dao,
    private val transactionDao: Transaction_Dao
) {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllAccountsFlow(): Flow<List<Account_Info>> = accountDao.getAllAccountsFlow()

    fun getNumberOfAccountsFlow(): Flow<Int> = accountDao.getNumberOfAccountsFlow()
    suspend fun updateBalance(balance: Long, accountName: String, userId: String) {
        //  Always update Room first and mark as unsynced
        val existingAccount = accountDao.getAllAccounts().find { it.accountName == accountName }
        existingAccount?.let {
            accountDao.updateaccountinfo(it.copy(balance = balance, isSynced = false))
        } ?: accountDao.updatebalance(balance, accountName)

        //  Firestore completely isolated — never throws to caller
        try {
            withTimeoutOrNull(100) {
                val account = accountDao.getAllAccounts().find { it.accountName == accountName }
                account?.let {
                    val synced = it.copy(isSynced = true)
                    firestore.collection("users")
                        .document(userId)
                        .collection("accounts")
                        .document(it.id.toString())
                        .set(synced)
                        .await()
                    accountDao.setSynced(it.id)
                }
            }
        } catch (e: Exception) {
            // Offline or error — will be picked up by syncUnsynced() later
        }
    }

    suspend fun insertAccount(account: Account_Info, userId: String) {
        // Always insert locally first with isSynced = false
        val id = accountDao.insertAccount(account.copy(isSynced = false))

        // Firestore isolated with timeout
        try {
            withTimeoutOrNull(100) {
                val synced = account.copy(id = id, isSynced = true)
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(id.toString())
                    .set(synced)
                    .await()
                accountDao.setSynced(id)
            }
        } catch (e: Exception) {
            // Stays unsynced — picked up by syncUnsynced()
        }
    }

    suspend fun updateAccount(account: Account_Info, userId: String) {
        //  Always update Room first
        accountDao.updateaccountinfo(account.copy(isSynced = false))

        //  Firestore isolated with timeout
        try {
            withTimeoutOrNull(2500) {
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(account.id.toString())
                    .set(account.copy(isSynced = true))
                    .await()
                accountDao.setSynced(account.id)
            }
        } catch (e: Exception) {
            // Stays unsynced
        }
    }

    suspend fun deleteAccountById(id: Long, userId: String) {
        accountDao.deletebyid(id)
        try {
            withTimeoutOrNull(100) {
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(id.toString())
                    .delete()
                    .await()
            }
        } catch (e: Exception) { }
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = accountDao.getUnsynced()
        unsynced.forEach {
            try {
                withTimeoutOrNull(100) {
                    val synced = it.copy(isSynced = true)
                    firestore.collection("users")
                        .document(userId)
                        .collection("accounts")
                        .document(it.id.toString())
                        .set(synced)
                        .await()
                    accountDao.setSynced(it.id)
                }
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
