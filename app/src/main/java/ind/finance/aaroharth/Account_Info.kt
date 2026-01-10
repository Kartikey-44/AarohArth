package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "AccountsTable",
    indices = [Index(value = ["normalizedName"], unique = true)
    ]
)

data class Account_Info (@PrimaryKey(autoGenerate = true)
                         val id:Long=0,
                         val accountType: String,
                         val accountName: String,
                         val normalizedName: String,
                         val balance: Long)