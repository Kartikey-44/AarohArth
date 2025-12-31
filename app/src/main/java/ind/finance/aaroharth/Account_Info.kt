package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "Account_Info")

data class Account_Info (@PrimaryKey(autoGenerate = true)
val id:Long=0,
    val account_name: String,
    val account_type: String,
    val balance: Long)