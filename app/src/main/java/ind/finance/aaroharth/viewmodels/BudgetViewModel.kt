package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.repositories.BudgetRepository
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _budgetSummary = MutableLiveData<List<BudgetSummary>>()
    val budgetSummary: LiveData<List<BudgetSummary>> = _budgetSummary

    private val _currentBudget = MutableLiveData<Budget_Info>()
    val currentBudget: LiveData<Budget_Info> = _currentBudget

    private val _operationSuccess = MutableLiveData<String>()
    val operationSuccess: LiveData<String> = _operationSuccess

    fun loadBudgetSummary(monthKey: String) {
        viewModelScope.launch {
            _budgetSummary.value = repository.getBudgetSummary(monthKey)
        }
    }

    fun loadBudgetById(id: Long) {
        viewModelScope.launch {
            _currentBudget.value = repository.getBudgetById(id)
        }
    }

    fun addBudget(category: String, amount: Long, monthKey: String) {
        viewModelScope.launch {
            if (!repository.budgetExists(category, monthKey)) {
                repository.insertBudget(
                    Budget_Info(
                        category = category,
                        amount = amount,
                        monthKey = monthKey,
                        date = System.currentTimeMillis()
                    )
                )
                _operationSuccess.value = "Budget Saved"
            }
        }
    }

    fun updateBudget(id: Long, category: String, amount: Long) {
        viewModelScope.launch {
            repository.updateBudget(id, category, amount)
            _operationSuccess.value = "Budget Updated Successfully"
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
            _operationSuccess.value = "Budget Deleted Successfully"
        }
    }
}