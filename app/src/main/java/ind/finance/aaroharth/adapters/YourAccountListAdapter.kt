package ind.finance.aaroharth.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ind.finance.aaroharth.modificationsFragments.AccountModification
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.Account_Info
import java.text.NumberFormat
import java.util.Locale

class YourAccountListAdapter(
    private var accounts: List<Account_Info>
) : RecyclerView.Adapter<YourAccountListAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val balance: TextView = itemView.findViewById(R.id.balance)
        val type: TextView = itemView.findViewById(R.id.type)
        val name: TextView = itemView.findViewById(R.id.name)
        val icon: ImageView = itemView.findViewById<ImageView>(R.id.icon_container)
        val card: MaterialCardView = itemView.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.your_accounts_card, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AccountViewHolder,
        position: Int
    ) {
        val account = accounts[position]

        val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        val amount = format.format(account.balance)

        holder.balance.text = "\u20B9 $amount"
        holder.type.text = account.accountType

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

        holder.name.text = camelCaseToWords(toPascalCase(account.accountName))
        when (account.accountType) {
            "UPI" -> holder.icon.setImageResource(R.drawable.upi)
            "Cash" -> holder.icon.setImageResource(R.drawable.cash)
            "Debit Card" -> holder.icon.setImageResource(R.drawable.debitcard)
            "Credit Card" -> holder.icon.setImageResource(R.drawable.creditcard)
            "Bank Account" -> holder.icon.setImageResource(R.drawable.bank)
        }

        holder.card.setOnClickListener {
            val intent = Intent(holder.itemView.context, AccountModification::class.java)
            intent.putExtra("id", account.id)
            intent.putExtra("name", account.accountName)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = accounts.size

    fun updatelist(newAccounts: List<Account_Info>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
}