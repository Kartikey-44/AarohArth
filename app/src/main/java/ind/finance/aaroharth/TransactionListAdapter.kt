package ind.finance.aaroharth

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.jvm.java


class TransactionListAdapter(

    private var transaction: List<Transaction_Info>
):
    RecyclerView.Adapter<TransactionListAdapter.TransactionListViewholder>() {
    companion object {
        private val DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault())
    }


    class TransactionListViewholder(itemview: View): RecyclerView.ViewHolder(itemview){
        val iconholder: ImageView=itemview.findViewById<ImageView>(R.id.icon)
        val medium: TextView=itemview.findViewById<TextView>(R.id.transaction_medium)
        val otherparty: TextView=itemview.findViewById<TextView>(R.id.other_party)
        val dateAndTime: TextView=itemview.findViewById<TextView>(R.id.date)
        val amount: TextView=itemview.findViewById<TextView>(R.id.amount)
        val doot: TextView=itemview.findViewById<TextView>(R.id.dot)
        val dot2: TextView=itemview.findViewById<TextView>(R.id.dot2)
        val card: MaterialCardView=itemview.findViewById<MaterialCardView>(R.id.card)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionListAdapter.TransactionListViewholder {
        val view= LayoutInflater.from(parent.context)
          .inflate(R.layout.recent_transaction,parent,false)
        return TransactionListViewholder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionListAdapter.TransactionListViewholder,
        position: Int
    ) {
        val transaction=transaction[position]
        val type=transaction.transactionType.toString().lowercase().trim()
        if(type=="income"){
            holder.iconholder.setBackgroundResource(R.drawable.transaction_card_background_income)
            holder.card.setStrokeColor(
                ContextCompat
                    .getColor(holder.itemView.context
                        ,R.color.recent_transaction_card_stroke_income
                    )
            )
            holder.amount.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.recent_tran_amount_income
                )
            )
            holder.doot.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.income_dot_color
                )
            )
            holder.dot2.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.income_dot_color
                )
            )
            holder.otherparty.text="From: ${camelCaseToWords(toPascalCase(transaction.otherParty))}"

            val format= NumberFormat.getNumberInstance(Locale("en","IN"))
            format.maximumFractionDigits=0
            val amount=format.format(transaction.amount)
            holder.amount.text="+ \u20B9 ${amount}"
        }
        else{
            holder.iconholder.setBackgroundResource(R.drawable.transaction_card_background_expense)
            holder.card.setStrokeColor(
                ContextCompat
                    .getColor(holder.itemView.context
                        ,R.color.recent_transaction_card_stroke_expense
                    )
            )
            holder.amount.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.recent_tran_amount_expense
                )
            )
            holder.doot.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.expense_dot_color
                )
            )
            holder.dot2.setTextColor( ContextCompat
                .getColor(holder.itemView.context
                    ,R.color.expense_dot_color
                )
            )
            holder.otherparty.text="To: ${camelCaseToWords(toPascalCase(transaction.otherParty))}"

            val format= NumberFormat.getNumberInstance(Locale("en","IN"))
            format.maximumFractionDigits=0
            val amount=format.format(transaction.amount)
            holder.amount.text="- \u20B9 ${amount}"
        }

        holder.dateAndTime.text =
            DATE_ONLY_FORMATTER.format(
                Instant.ofEpochMilli(transaction.dateAndTime)
            )
        val medium=transaction.transactionMedium.toString().lowercase().trim()
        if(medium=="upi"){
            holder.medium.text="UPI"
        }
        else{
            holder.medium.text=camelCaseToWords(toPascalCase(medium))
        }
        when(transaction.category.toString().toLowerCase()){
            "salary"->holder.iconholder.setImageResource(R.drawable.decoration)
            "business"->holder.iconholder.setImageResource(R.drawable.business)
            "freelance"->holder.iconholder.setImageResource(R.drawable.freelance)
            "investment"->holder.iconholder.setImageResource(R.drawable.investment)
            "savings"->holder.iconholder.setImageResource(R.drawable.savings)
            "food"->holder.iconholder.setImageResource(R.drawable.food)
            "dining out"->holder.iconholder.setImageResource(R.drawable.dining_out)
            "shopping"->holder.iconholder.setImageResource(R.drawable.shopping)
            "personal care"->holder.iconholder.setImageResource(R.drawable.personalcare)
            "entertainment"->holder.iconholder.setImageResource(R.drawable.entertainment)
            "subscriptions"->holder.iconholder.setImageResource(R.drawable.subscription)
            "housing"->holder.iconholder.setImageResource(R.drawable.housing)
            "rental"->holder.iconholder.setImageResource(R.drawable.rental)
            "travel"->holder.iconholder.setImageResource(R.drawable.travel)
            "medical"->holder.iconholder.setImageResource(R.drawable.medical)
            "insurance"->holder.iconholder.setImageResource(R.drawable.insurance)
            "education"->holder.iconholder.setImageResource(R.drawable.education)
            "emi / loans"->holder.iconholder.setImageResource(R.drawable.loan)
            "tax"->holder.iconholder.setImageResource(R.drawable.tax)
            "gifts"->holder.iconholder.setImageResource(R.drawable.gift)
            "donations"->holder.iconholder.setImageResource(R.drawable.donation)
            "miscellaneous"->holder.iconholder.setImageResource(R.drawable.miscellaneous)
            "mobile recharge"->holder.iconholder.setImageResource(R.drawable.recharge)
            "fasttag recharge"->holder.iconholder.setImageResource(R.drawable.fastag)
            "electricity"->holder.iconholder.setImageResource(R.drawable.electricity)
            "water bill"->holder.iconholder.setImageResource(R.drawable.waterbill)
            "taxi"->holder.iconholder.setImageResource(R.drawable.taxi)
            "auto"->holder.iconholder.setImageResource(R.drawable.auto)
            "hotel"->holder.iconholder.setImageResource(R.drawable.hotel)
            "flight"->holder.iconholder.setImageResource(R.drawable.flight)
            "petrol"->holder.iconholder.setImageResource(R.drawable.petrol)
            "diesel"->holder.iconholder.setImageResource(R.drawable.diesel)
            "cng"->holder.iconholder.setImageResource(R.drawable.cng)
            "lpg"->holder.iconholder.setImageResource(R.drawable.lpgpng)
            "png"->holder.iconholder.setImageResource(R.drawable.lpgpng)
            "public transport"->holder.iconholder.setImageResource(R.drawable.publictransport)
            "grocery"->holder.iconholder.setImageResource(R.drawable.grocery)
            "other"->holder.iconholder.setImageResource(R.drawable.other)





            else->holder.iconholder.setImageResource(R.drawable.other)
        }



        holder.card.setOnClickListener {
            val intent = Intent(holder.itemView.context, TransactionModification::class.java)
            intent.putExtra("id", transaction.id)
            intent.putExtra("type",transaction.transactionType)
            holder.itemView.context.startActivity(intent)

        }

    }


    override fun getItemCount(): Int = transaction.size




    fun updatelist(newTransactions: List<Transaction_Info>) {
        transaction=newTransactions
        notifyDataSetChanged()
    }

    fun toPascalCase(input: String): String {
        return input
            .trim()
            .lowercase()
            .split(Regex("\\s+|_+|-+"))
            .joinToString("") { it.replaceFirstChar(Char::uppercase) }
    }
    fun camelCaseToWords(input: String): String {
        return input
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    }


}
