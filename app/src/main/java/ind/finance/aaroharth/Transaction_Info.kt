package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction_Info(@PrimaryKey(autoGenerate = true)
val id:Long=0,
    val transaction_type: String,
    val amount:Long,
    val other_party:String,
    val date: Long,
    val remark:String,
    val category:String,
    val transacction_medium:String,
    val transaction_way: String)
