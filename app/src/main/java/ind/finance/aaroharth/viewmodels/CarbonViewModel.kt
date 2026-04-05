package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.repositories.TransactionRepository
import kotlinx.coroutines.launch

class CarbonViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction_Info>>()
    val transactions: LiveData<List<Transaction_Info>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _period = MutableLiveData("Weekly")
    val period: LiveData<String> = _period

    init {
        loadData()
    }

    fun setPeriod(period: String) {
        _period.value = period
        loadData()
    }

    fun loadData() {
        val currentPeriod = _period.value ?: "Weekly"
        val now = System.currentTimeMillis()
        val startDate = when (currentPeriod) {
            "Weekly" -> now - 7 * 86400000L
            "Monthly" -> now - 30 * 86400000L
            "Yearly" -> now - 365 * 86400000L
            else -> now - 7 * 86400000L
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getRecentCo2Transactions(startDate, now)
                _transactions.postValue(result)
            } catch (e: Exception) {
                _transactions.postValue(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }
}
