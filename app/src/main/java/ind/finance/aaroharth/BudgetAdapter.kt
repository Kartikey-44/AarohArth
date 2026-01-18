package ind.finance.aaroharth

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min



class BudgetAdapter(
    private var budgets: List<BudgetSummary>,
    private val onClick: (BudgetSummary) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val budgetAmount: TextView = itemView.findViewById(R.id.budget_amount)
        val left: TextView = itemView.findViewById(R.id.left)
        val used: TextView = itemView.findViewById(R.id.used)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressbar)
        val iconholder: ImageView = itemView.findViewById(R.id.iconcont)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.budget_card, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val item = budgets[position]

        val spent = item.spent
        val limit = item.budgetLimit
        val remaining = limit - spent

        val percent =
            if (limit > 0) ((spent * 100) / limit).toInt()
            else 0

        holder.budgetAmount.text = "Budget: ₹$limit"
        holder.left.text = "Left: ₹$remaining"
        holder.used.text = "Used: ${min(percent, 100)}%"
        holder.progressBar.progress = min(percent, 100)

        val color = when {
            percent < 70 -> "#2E7D32"
            percent < 90 -> "#F9A825"
            else -> "#C62828"
        }

        val parsed = Color.parseColor(color)
        holder.progressBar.progressDrawable.setTint(parsed)
        holder.used.setTextColor(parsed)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
        when(item.category.toString().lowercase().trim()){
            "salary"->holder.iconholder.setImageResource(R.drawable.salary)
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
            "decoration"->holder.iconholder.setImageResource(R.drawable.decoration)






            else->holder.iconholder.setImageResource(R.drawable.other)
        }
    }

    override fun getItemCount(): Int = budgets.size

    fun updateList(newBudgets: List<BudgetSummary>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }
}
