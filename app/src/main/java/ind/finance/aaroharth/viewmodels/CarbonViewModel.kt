package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.repositories.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class CarbonViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _period = MutableLiveData("Weekly")
    val period: LiveData<String> = _period

    val transactions: LiveData<List<Transaction_Info>> = _period.asFlow().flatMapLatest { p ->
        val now = System.currentTimeMillis()
        val startDate = when (p) {
            "Weekly" -> now - 7 * 86400000L
            "Monthly" -> now - 30 * 86400000L
            "Yearly" -> now - 365 * 86400000L
            else -> now - 7 * 86400000L
        }
        repository.getAllTransactionsFlow().map { list ->
            list.filter { it.transactionType == "Expense" && it.carbonImpact > 0 && it.dateAndTime >= startDate }
        }
    }.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setPeriod(period: String) {
        _period.value = period
    }
}
