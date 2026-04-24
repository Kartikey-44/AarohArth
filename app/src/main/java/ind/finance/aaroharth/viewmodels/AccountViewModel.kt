package ind.finance.aaroharth.viewmodels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.data.model.Account_Info
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _accounts = MutableLiveData<List<Account_Info>>()
    val accounts: LiveData<List<Account_Info>> = _accounts

    private val _totalBalance = MutableLiveData<Long>()
    val totalBalance: LiveData<Long> = _totalBalance

    private val _accountCount = MutableLiveData<Int>()
    val accountCount: LiveData<Int> = _accountCount

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _transactionCount = MutableLiveData<Long>()
    val transactionCount: LiveData<Long> = _transactionCount

    private val _currentAccount = MutableLiveData<Account_Info>()
    val currentAccount: LiveData<Account_Info> = _currentAccount

    private val _operationSuccess = MutableLiveData<String>()
    val operationSuccess: LiveData<String> = _operationSuccess

    fun loadAccounts() {
        viewModelScope.launch {
            _accounts.postValue(repository.getAllAccounts())
            _totalBalance.postValue(repository.getTotalBalance())
            _accountCount.postValue(repository.getNumberOfAccounts())
        }
    }

    fun loadAccountDetails(id: Long, name: String?) {
        viewModelScope.launch {
            _currentAccount.postValue(repository.getAccountById(id))
            if (name != null) {
                _transactionCount.postValue(repository.getTransactionCountForAccount(name))
            }
        }
    }

    fun searchAccounts(query: String) {
        viewModelScope.launch {
            _accounts.postValue(repository.searchAccounts(query))
        }
    }

    fun deleteAccount(accountId: Long, accountName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val transactionCount = repository.getTransactionCountForAccount(accountName)
                if (transactionCount > 0) {
                    _error.postValue("Cannot delete account with existing transactions")
                    return@launch
                }
                repository.deleteAccountById(accountId, userId)
                _operationSuccess.postValue("Account Deleted Successfully")
                loadAccounts()
            } catch (e: Exception) {
                _error.postValue("Failed to delete account")
            }
        }
    }

    fun saveAccount(type: String, name: String, balance: Long) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val normalized = name.lowercase().trim()
            val existing = repository.getAllAccounts().find {
                it.normalizedName == normalized
            }
            if (existing != null) {
                _error.postValue("Account with this name already exists")
                return@launch
            }
            val account = Account_Info(
                accountType = type,
                accountName = name,
                normalizedName = normalized,
                balance = balance
            )
            try {
                repository.insertAccount(account, userId)
                _saveStatus.postValue(true)
                loadAccounts()
            } catch (e: Exception) {
                _error.postValue("Account name must be unique")
            }
        }
    }

    fun updateAccount(account: Account_Info) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.updateAccount(account, userId)
                _operationSuccess.postValue("Account Updated Successfully")
                loadAccounts()
            } catch (e: Exception) {
                _error.postValue("Failed to update account")
            }
        }
    }
}