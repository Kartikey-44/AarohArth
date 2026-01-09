package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction_Info(@PrimaryKey(autoGenerate = true)
val id:Long=0,
    val transactionType: String,
    val amount:Long,
    val otherParty:String,
    val dateAndTime: Long,
    val remark:String,
    val category:String,
    val transacctionMedium:String,
    val transactionWay: String)
