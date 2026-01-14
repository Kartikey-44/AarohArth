package ind.finance.aaroharth

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ind.finance.aaroharth.databinding.BudgetCardBinding
import kotlin.math.min
class BudgetAdapter(
    private val onClick: (BudgetSummary) -> Unit
) : ListAdapter<BudgetSummary, BudgetAdapter.BudgetVH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetVH {
        val binding = BudgetCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetVH(binding, onClick)
    }

    override fun onBindViewHolder(holder: BudgetVH, position: Int) {
        holder.bind(getItem(position))
    }

    class BudgetVH(
        private val binding: BudgetCardBinding,
        private val onClick: (BudgetSummary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetSummary) {

            val spent = item.spent
            val limit = item.budgetLimit
            val remaining = limit - spent

            val percent = if (limit > 0)
                (spent * 100 / limit).toInt()
            else 0

            binding.budgetAmount.text = "Budget: ₹$limit"
            binding.left.text = "Left: ₹$remaining"
            binding.used.text = "Used: ${min(percent, 100)}%"
            binding.progressbar.progress = min(percent, 100)

            when {
                percent < 70 -> setColor("#2E7D32")
                percent < 90 -> setColor("#F9A825")
                else -> setColor("#C62828")
            }

            // ✅ CLICK HANDLER
            binding.root.setOnClickListener {
                onClick(item)
            }
        }

        private fun setColor(color: String) {
            val parsed = Color.parseColor(color)
            binding.progressbar.setIndicatorColor(parsed)
            binding.used.setTextColor(parsed)
        }
    }

    companion object {
        private val Diff = object : DiffUtil.ItemCallback<BudgetSummary>() {
            override fun areItemsTheSame(a: BudgetSummary, b: BudgetSummary) =
                a.id == b.id

            override fun areContentsTheSame(a: BudgetSummary, b: BudgetSummary) =
                a == b
        }
    }
}
