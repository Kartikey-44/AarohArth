package ind.finance.aaroharth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TransactionTable")
data class Transaction_Info(@PrimaryKey(autoGenerate = true)
                            val id:Long=0,
                            val transactionType: String,
                            val amount:Long,
                            val otherParty:String,
                            val category:String,
                            val dateAndTime: Long,
                            val transactionMedium:String,
                            val transactionWay: String,
                            val remark:String,
                            val carbonEmissionFactor: Double,
                            val carbonEmitted: Double,
                            val carbonEmissionAuth: String,
                            val monthKey:String
)
