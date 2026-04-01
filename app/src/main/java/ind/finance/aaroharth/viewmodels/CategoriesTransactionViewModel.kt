package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.data.model.Transaction_Info
import kotlinx.coroutines.launch

class CategoriesTransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction_Info>>()
    val transactions: LiveData<List<Transaction_Info>> = _transactions

    fun loadTransactions(category: String, type: String) {
        viewModelScope.launch {
            val result = repository.getTransactionsByCategory(category, type)
            _transactions.postValue(result)
        }
    }
}