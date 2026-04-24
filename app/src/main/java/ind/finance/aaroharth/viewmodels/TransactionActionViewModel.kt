package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.data.model.Transaction_Info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionActionViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _accountTypes = MutableLiveData<List<String>>()
    val accountTypes: LiveData<List<String>> = _accountTypes

    private val _accountNames = MutableLiveData<List<String>>()
    val accountNames: LiveData<List<String>> = _accountNames

    private val _saveStatus = MutableLiveData<String>()
    val saveStatus: LiveData<String> = _saveStatus

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _currentTransaction = MutableLiveData<Transaction_Info>()
    val currentTransaction: LiveData<Transaction_Info> = _currentTransaction

    fun loadAccountTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            _accountTypes.postValue(accountRepository.getAllAccountTypes())
        }
    }

    fun loadAccountNames(type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accountNames.postValue(accountRepository.filterAccountsByType(type).map { it.accountName })
        }
    }

    fun loadTransactionById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentTransaction.postValue(transactionRepository.getTransactionById(id))
        }
    }

    fun saveTransaction(
        type: String,
        amount: Long,
        otherParty: String,
        category: String,
        accountName: String,
        accountType: String,
        remark: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) { // IO dispatcher
            try {
                val balance = accountRepository.getBalance(accountName)
                val now = System.currentTimeMillis()
                val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))

                if (type == "expense") {
                    if (balance < amount) {
                        _error.postValue("Insufficient Balance")
                        return@launch
                    }
                    accountRepository.updateBalance(balance - amount, accountName, userId)
                } else {
                    accountRepository.updateBalance(balance + amount, accountName, userId)
                }

                val factor = if (type == "expense") carbonEmissionFactor(category) else 0.0
                val emitted = if (type == "expense") amount * factor else 0.0
                val level = if (type == "expense") carbonEmissionAuth(emitted) else "None"

                val transaction = Transaction_Info(
                    id = 0,
                    transactionType = if (type == "income") "Income" else "Expense",
                    amount = amount,
                    otherParty = if (otherParty.isEmpty()) "Unknown" else otherParty,
                    category = category,
                    dateAndTime = now,
                    transactionMedium = accountType,
                    transactionWay = accountName,
                    remark = remark,
                    carbonImpactFactor = factor,
                    carbonImpact = emitted,
                    carbonImpactLevel = level,
                    monthKey = monthKey,
                    isSynced = false
                )

                transactionRepository.insertTransaction(transaction, userId)
                _saveStatus.postValue(if (type == "income") "Income Saved" else "Expense Saved")

            } catch (e: Exception) {
                android.util.Log.e("SAVE_ERROR", "saveTransaction failed: ${e::class.simpleName}: ${e.message}", e)
                _error.postValue(e.message ?: "Failed to save transaction")
            }
        }
    }

    fun updateTransaction(original: Transaction_Info, updated: Transaction_Info) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) { //  IO dispatcher
            try {
                val balance = accountRepository.getBalance(original.transactionWay)
                val restoredBalance = if (original.transactionType == "Income") {
                    balance - original.amount
                } else {
                    balance + original.amount
                }

                if (updated.transactionType == "Expense") {
                    if (restoredBalance < updated.amount) {
                        _error.postValue("Insufficient Balance")
                        return@launch
                    }
                }

                accountRepository.updateBalance(restoredBalance, original.transactionWay, userId)

                val currentBalanceOnTargetAccount = if (original.transactionWay == updated.transactionWay) {
                    restoredBalance
                } else {
                    accountRepository.getBalance(updated.transactionWay)
                }

                val finalBalance = if (updated.transactionType == "Income") {
                    currentBalanceOnTargetAccount + updated.amount
                } else {
                    currentBalanceOnTargetAccount - updated.amount
                }
                accountRepository.updateBalance(finalBalance, updated.transactionWay, userId)

                transactionRepository.updateTransaction(updated, userId)
                _saveStatus.postValue("Transaction Edited Successfully")

            } catch (e: Exception) {
                android.util.Log.e("SAVE_ERROR", "updateTransaction failed: ${e::class.simpleName}: ${e.message}", e)
                _error.postValue(e.message ?: "Failed to update transaction")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction_Info) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) { //  IO dispatcher
            try {
                val balance = accountRepository.getBalance(transaction.transactionWay)
                val newBalance = if (transaction.transactionType == "Income") {
                    balance - transaction.amount
                } else {
                    balance + transaction.amount
                }
                accountRepository.updateBalance(newBalance, transaction.transactionWay, userId)
                transactionRepository.deleteTransactionById(transaction.id, userId)
                _saveStatus.postValue("Transaction Deleted Successfully")

            } catch (e: Exception) {
                android.util.Log.e("SAVE_ERROR", "deleteTransaction failed: ${e::class.simpleName}: ${e.message}", e)
                _error.postValue(e.message ?: "Failed to delete transaction")
            }
        }
    }

    private fun carbonEmissionFactor(categoryName: String): Double {
        return when (categoryName.lowercase().trim()) {
            "food"             -> 0.0080
            "grocery"          -> 0.0083
            "dining out"       -> 0.0120
            "shopping"         -> 0.0035
            "personal care"    -> 0.0015
            "entertainment"    -> 0.0021
            "subscriptions"    -> 0.0011
            "medical"          -> 0.0021
            "education"        -> 0.0017
            "gifts"            -> 0.0030
            "miscellaneous"    -> 0.0064
            "housing"          -> 0.0080
            "utilities"        -> 0.0448
            "water bill"       -> 0.0112
            "public transport" -> 0.0006
            "auto"             -> 0.0102
            "taxi"             -> 0.0085
            "hotel"            -> 0.0027
            "flight"           -> 0.0765
            "electricity"      -> 0.0895
            "petrol"           -> 0.0224
            "diesel"           -> 0.0263
            "cng"              -> 0.0284
            "lpg"              -> 0.0470
            "png"              -> 0.1218
            "mobile recharge", "fasttag recharge", "recharge" -> 0.0002
            else -> 0.0
        }
    }

    private fun carbonEmissionAuth(emitted: Double): String {
        return when {
            emitted < 5.0  -> "Low"
            emitted < 50.0 -> "Medium"
            else           -> "High"
        }
    }
}