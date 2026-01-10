package ind.finance.aaroharth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YourAccountListAdapter(
    private var accounts: List<Account_Info>
) : RecyclerView.Adapter<YourAccountListAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val balance: TextView = itemView.findViewById(R.id.balance)
        val type: TextView = itemView.findViewById(R.id.type)
        val name: TextView = itemView.findViewById(R.id.name)
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

        holder.balance.text = "₹ ${account.balance}"
        holder.type.text = account.accountType
        holder.name.text = account.accountName
    }


    override fun getItemCount(): Int = accounts.size
    fun updatelist(newAccounts: List<Account_Info>) {
        accounts=newAccounts
        notifyDataSetChanged()
    }
}
