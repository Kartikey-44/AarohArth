package ind.finance.aaroharth

import androidx.room.PrimaryKey

data class Account_Info (@PrimaryKey(autoGenerate = true)
val id:Long=0,
    val account_name: String,
    val account_type: String,
    val balance: Long)