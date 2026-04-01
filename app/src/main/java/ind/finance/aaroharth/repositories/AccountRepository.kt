package ind.finance.aaroharth.repositories

import ind.finance.aaroharth.data.local.Account_Dao
import ind.finance.aaroharth.data.local.Transaction_Dao
import ind.finance.aaroharth.data.model.Account_Info

class AccountRepository(private val accountDao: Account_Dao, private val transactionDao: Transaction_Dao) {

    suspend fun insertAccount(account: Account_Info) {
        accountDao.insertAccount(account)
    }

    suspend fun getAllAccounts(): List<Account_Info> {
        return accountDao.getAllAccounts()
    }

    suspend fun getAllAccountTypes(): List<String> {
        return accountDao.getAllAccountType()
    }

    suspend fun getBalance(accountName: String): Long {
        return accountDao.getbalance(accountName)
    }

    suspend fun updateBalance(balance: Long, accountName: String) {
        accountDao.updatebalance(balance, accountName)
    }

    suspend fun searchAccounts(query: String): List<Account_Info> {
        return accountDao.searchAccounts(query)
    }

    suspend fun filterAccountsByType(accountType: String): List<Account_Info> {
        return accountDao.filter(accountType)
    }

    suspend fun getCurrentBalance(): Long {
        return accountDao.currentbalance()
    }

    suspend fun getNumberOfAccounts(): Int {
        return accountDao.numberOfAccounts()
    }

    suspend fun getAccountById(id: Long): Account_Info {
        return accountDao.getAccountById(id)
    }

    suspend fun deleteAccountById(id: Long) {
        accountDao.deletebyid(id)
    }

    suspend fun updateAccountInfo(account: Account_Info) {
        accountDao.updateaccountinfo(account)
    }

    suspend fun getTotalBalance(): Long {
        return accountDao.gettotalbalance()
    }

    suspend fun getTransactionCountForAccount(accountName: String): Long {
        return transactionDao.getNoOfTransaction(accountName)
    }
}