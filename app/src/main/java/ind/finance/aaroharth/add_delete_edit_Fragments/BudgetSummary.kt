package ind.finance.aaroharth.add_delete_edit_Fragments

data class BudgetSummary(
    val id: Long,
    val category: String,
    val budgetLimit: Long,
    val spent: Long
)