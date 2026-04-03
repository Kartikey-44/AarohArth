package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ind.finance.aaroharth.repositories.BudgetRepository
import ind.finance.aaroharth.data.model.BudgetSummary
import ind.finance.aaroharth.data.model.Budget_Info
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

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
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            if (!repository.budgetExists(category, monthKey)) {
                repository.insertBudget(
                    Budget_Info(
                        category = category,
                        amount = amount,
                        monthKey = monthKey,
                        date = System.currentTimeMillis(),
                        isSynced = false
                    ),
                    userId
                )
                _operationSuccess.value = "Budget Saved"
                loadBudgetSummary(monthKey)
            }
        }
    }

    fun updateBudget(id: Long, category: String, amount: Long) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val original = repository.getBudgetById(id)
            val updated = original.copy(category = category, amount = amount, isSynced = false)
            repository.updateBudget(updated, userId)
            _operationSuccess.value = "Budget Updated Successfully"
            loadBudgetSummary(updated.monthKey)
        }
    }

    fun deleteBudget(id: Long) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val budget = repository.getBudgetById(id)
            repository.deleteBudgetById(id, userId)
            _operationSuccess.value = "Budget Deleted Successfully"
            loadBudgetSummary(budget.monthKey)
        }
    }
}
