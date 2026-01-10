package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BudgetsInfo(
    @PrimaryKey(autoGenerate = true)
    val id:Long=0,
    val category: String,
    val amount: Long,
    val time: String
)
