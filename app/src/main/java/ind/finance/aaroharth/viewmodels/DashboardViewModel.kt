package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.filterchart
import kotlinx.coroutines.launch
import kotlin.math.ceil

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _topCategories = MutableLiveData<List<CategoryExpense>>()
    val topCategories: LiveData<List<CategoryExpense>> = _topCategories

    private val _dailyExpenses = MutableLiveData<List<filterchart>>()
    val dailyExpenses: LiveData<List<filterchart>> = _dailyExpenses

    private val _spendingPace = MutableLiveData<Int>()
    val spendingPace: LiveData<Int> = _spendingPace

    private val _exhaustionDays = MutableLiveData<Int>()
    val exhaustionDays: LiveData<Int> = _exhaustionDays

    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    fun refreshDashboard(daysRange: Int) {
        val fromTime = System.currentTimeMillis() - daysRange * 24L * 60 * 60 * 1000

        viewModelScope.launch {
            val categories = transactionRepository.getCategoryExpense(fromTime).take(5)
            _topCategories.postValue(categories)

            val daily = transactionRepository.getDailyExpense(fromTime)
            _dailyExpenses.postValue(daily)

            val total = transactionRepository.getTotalExpense(fromTime)
            _totalSpent.postValue(total)

            val balance = accountRepository.getTotalBalance()
            val pace = if (daysRange > 0) total / daysRange else 0.0
            _spendingPace.postValue(pace.toInt())

            val remaining = if (pace > 0) ceil(balance / pace).toInt() else 0
            _exhaustionDays.postValue(remaining)
        }
    }
}