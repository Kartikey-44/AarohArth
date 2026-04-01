package ind.finance.aaroharth.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.CategoryExpense
import java.text.NumberFormat
import java.util.Locale

class CategoryOverviewAdapter(
    private var categories: List<CategoryExpense>
) : RecyclerView.Adapter<CategoryOverviewAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iconconta)
        val name: TextView = itemView.findViewById(R.id.categoryname)
        val amount: TextView = itemView.findViewById(R.id.amountview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_expense_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]

        holder.name.text = formatCategoryName(item.category)

        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        holder.amount.text = "₹ ${formatter.format(item.total)}"

        holder.amount.setTextColor(
            ContextCompat.getColor(holder.itemView.context, R.color.recent_tran_amount_expense)
        )

        holder.icon.setImageResource(getCategoryIcon(item.category))
    }

    override fun getItemCount(): Int = categories.size

    fun updateList(newList: List<CategoryExpense>) {
        categories = newList
        notifyDataSetChanged()
    }

    // ---------------- HELPERS ----------------

    private fun formatCategoryName(input: String): String {
        return input.lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category.lowercase()) {
            "food" -> R.drawable.food
            "shopping" -> R.drawable.shopping
            "travel" -> R.drawable.travel
            "medical" -> R.drawable.medical
            "rent", "rental" -> R.drawable.rental
            "education" -> R.drawable.education
            "fuel", "petrol", "diesel" -> R.drawable.petrol
            "entertainment" -> R.drawable.entertainment
            "grocery" -> R.drawable.grocery
            "salary" -> R.drawable.salary
            "business" -> R.drawable.business
            "freelance" -> R.drawable.freelance
            "investment" -> R.drawable.investment
            "savings" -> R.drawable.savings
            "insurance" -> R.drawable.insurance
            "tax" -> R.drawable.tax
            "gifts" -> R.drawable.gift
            "miscellaneous" -> R.drawable.miscellaneous
            "mobile recharge" -> R.drawable.recharge
            "fasttag recharge" -> R.drawable.fastag
            "electricity" -> R.drawable.electricity
            "water bill" -> R.drawable.waterbill
            "taxi" -> R.drawable.taxi
            "auto" -> R.drawable.auto
            "hotel" -> R.drawable.hotel
            "flight" -> R.drawable.flight
            "cng" -> R.drawable.cng
            else -> R.drawable.other
        }
    }
}