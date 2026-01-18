package ind.finance.aaroharth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Co2TransactionAdapter(private var transactions: List<Transaction_Info>) :
    RecyclerView.Adapter<Co2TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconholder: ImageView = view.findViewById(R.id.icon)
        val category: TextView = view.findViewById(R.id.category)
        val amount: TextView = view.findViewById(R.id.amount)
        val co2: TextView = view.findViewById(R.id.co2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_co2_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        // Fill each card with real data!
        holder.category.text = transaction.category
        holder.amount.text = "₹${transaction.amount}"
        holder.co2.text = "~ ${String.format("%.1f", transaction.carbonEmitted)}kg CO₂"

        // Set icon based on category (simple version)
        when(transaction.category.toString().lowercase()){
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
    }

    override fun getItemCount(): Int = transactions.size

    fun submitList(newList: List<Transaction_Info>) {
        transactions = newList
        notifyDataSetChanged()
    }
}
