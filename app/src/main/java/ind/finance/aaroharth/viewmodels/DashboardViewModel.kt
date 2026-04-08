package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.data.model.CategoryExpense
import ind.finance.aaroharth.data.model.filterchart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun refreshDashboard(daysRange: Int, type: String = "Expense") {
        val fromTime = if (daysRange == 0) 0L else System.currentTimeMillis() - daysRange * 24L * 60 * 60 * 1000

        viewModelScope.launch {
            val categories = withContext(Dispatchers.IO) {
                if (type == "ALL") {
                    transactionRepository.getCategoryAll(fromTime)
                } else {
                    transactionRepository.getCategoryExpenseFiltered(fromTime, type)
                }
            }
            _topCategories.value = categories

            val daily = withContext(Dispatchers.IO) {
                transactionRepository.getDailyExpense(fromTime)
            }
            _dailyExpenses.value = daily

            val total = withContext(Dispatchers.IO) {
                transactionRepository.getTotalExpense(fromTime)
            }
            _totalSpent.value = total

            val balance = withContext(Dispatchers.IO) {
                accountRepository.getTotalBalance()
            }

            val pace = if (daysRange > 0) total / daysRange else 0.0
            _spendingPace.value = pace.toInt()

            val remaining = if (pace > 0) ceil(balance / pace).toInt() else 0
            _exhaustionDays.value = remaining
        }
    }
}
