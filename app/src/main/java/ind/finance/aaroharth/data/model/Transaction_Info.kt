package ind.finance.aaroharth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TransactionTable")
data class Transaction_Info(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionType: String = "",
    val amount: Long = 0L,
    val otherParty: String = "",
    val category: String = "",
    val dateAndTime: Long = 0L,
    val transactionMedium: String = "",
    val transactionWay: String = "",
    val remark: String = "",
    val carbonImpactFactor: Double = 0.0,
    val carbonImpact: Double = 0.0,
    val carbonImpactLevel: String = "",
    val monthKey: String = "",
    val isSynced: Boolean = false
)
