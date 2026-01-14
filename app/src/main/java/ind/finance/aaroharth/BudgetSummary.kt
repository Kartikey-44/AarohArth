package ind.finance.aaroharth

data class BudgetSummary(
    val id: Long,
    val category: String,
    val budgetLimit: Long,
    val spent: Long
)
