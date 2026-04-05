package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.aaroharth.repositories.AccountRepository
import ind.finance.aaroharth.repositories.BudgetRepository
import ind.finance.aaroharth.repositories.NotificationRepository
import ind.finance.aaroharth.repositories.TransactionRepository

class ViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoriesTransactionViewModel::class.java) -> {
                CategoriesTransactionViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(transactionRepository, accountRepository) as T
            }
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> {
                AccountViewModel(accountRepository) as T
            }
            modelClass.isAssignableFrom(BudgetViewModel::class.java) -> {
                BudgetViewModel(budgetRepository) as T
            }
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(TransactionActionViewModel::class.java) -> {
                TransactionActionViewModel(transactionRepository, accountRepository) as T
            }
            modelClass.isAssignableFrom(CarbonViewModel::class.java) -> {
                CarbonViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(transactionRepository, accountRepository) as T
            }
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                CategoriesViewModel() as T
            }
            modelClass.isAssignableFrom(NotificationHistoryViewModel::class.java) -> {
                NotificationHistoryViewModel(notificationRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}