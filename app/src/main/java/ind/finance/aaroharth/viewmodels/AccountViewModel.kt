package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.data.model.Account_Info
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

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
            _accounts.value = repository.getAllAccounts()
            _totalBalance.value = repository.getTotalBalance()
            _accountCount.value = repository.getNumberOfAccounts()
        }
    }

    fun loadAccountDetails(id: Long, name: String?) {
        viewModelScope.launch {
            _currentAccount.value = repository.getAccountById(id)
            if (name != null) {
                _transactionCount.value = repository.getTransactionCountForAccount(name)
            }
        }
    }

    fun searchAccounts(query: String) {
        viewModelScope.launch {
            _accounts.value = repository.searchAccounts(query)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
            _operationSuccess.value = "Account Deleted Successfully"
        }
    }

    fun saveAccount(type: String, name: String, balance: Long) {
        viewModelScope.launch {
            val account = Account_Info(
                accountType = type,
                accountName = name,
                normalizedName = name.lowercase().trim(),
                balance = balance
            )
            try {
                repository.insertAccount(account)
                _saveStatus.value = true
            } catch (e: Exception) {
                _error.value = "Account already exists or error occurred"
            }
        }
    }

    fun updateAccount(account: Account_Info) {
        viewModelScope.launch {
            repository.updateAccountInfo(account)
            _operationSuccess.value = "Account Updated Successfully"
        }
    }
}