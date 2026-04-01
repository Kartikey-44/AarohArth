package ind.finance.aaroharth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BudgetTable")
data class Budget_Info(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val category: String,
    val amount: Long,
    val date: Long,
    val monthKey: String
)
