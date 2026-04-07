package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.TransactionRepository
import ind.finance.aaroharth.data.model.Transaction_Info
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val refreshTrigger = MutableLiveData<Unit>(Unit)

    val transactions: LiveData<List<Transaction_Info>> = transactionRepository.getAllTransactionsFlow().asLiveData()

    private val _currentBalance = MutableLiveData<Long>()
    val currentBalance: LiveData<Long> = _currentBalance

    private val _monthlyIncome = MutableLiveData<Long>()
    val monthlyIncome: LiveData<Long> = _monthlyIncome

    private val _monthlyExpense = MutableLiveData<Long>()
    val monthlyExpense: LiveData<Long> = _monthlyExpense

    fun refreshData() {
        viewModelScope.launch {
            _currentBalance.value = accountRepository.getCurrentBalance()

            val now = LocalDate.now()
            val start = now.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = now.withDayOfMonth(now.lengthOfMonth())
                .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            _monthlyIncome.value = transactionRepository.getMonthWiseSum(start, end, "Income")
            _monthlyExpense.value = transactionRepository.getMonthWiseSum(start, end, "Expense")
        }
    }
}