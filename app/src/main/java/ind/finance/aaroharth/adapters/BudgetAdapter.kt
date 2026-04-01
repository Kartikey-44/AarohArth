package ind.finance.aaroharth.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.BudgetSummary
import kotlin.math.min

/**
 * RecyclerView Adapter for displaying a list of [BudgetSummary] items as budget cards.
 *
 * Each card shows:
 * - Budget limit and amount spent
 * - Remaining balance
 * - A progress bar with color-coded status (green / yellow / red)
 * - A category icon matching the budget's category name
 *
 * @param budgets Initial list of budget summaries to display
 * @param onClick Callback invoked when a budget card is tapped
 */
class BudgetAdapter(
    private var budgets: List<BudgetSummary>,
    private val onClick: (BudgetSummary) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    /**
     * ViewHolder that holds references to all views inside a single budget card (budget_card.xml).
     * Using ViewHolder pattern avoids repeated findViewById calls during scrolling.
     */
    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val budgetAmount: TextView = itemView.findViewById(R.id.budget_amount)  // Shows total budget limit
        val left: TextView = itemView.findViewById(R.id.left)                   // Shows remaining balance
        val used: TextView = itemView.findViewById(R.id.used)                   // Shows percentage used
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressbar)  // Visual usage indicator
        val iconholder: ImageView = itemView.findViewById(R.id.iconcont)        // Category icon
    }

    /**
     * Inflates the budget_card layout and wraps it in a [BudgetViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.budget_card, parent, false)
        return BudgetViewHolder(view)
    }

    /**
     * Binds data from a [BudgetSummary] to the views in the [BudgetViewHolder].
     * Handles spend calculations, progress bar color coding, click events, and category icons.
     */
    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val item = budgets[position]

        // --- Spend Calculations ---
        val spent = item.spent
        val limit = item.budgetLimit
        val remaining = limit - spent

        // Calculate usage percentage; guard against division by zero if limit is 0
        val percent =
            if (limit > 0) ((spent * 100) / limit).toInt()
            else 0

        // --- Bind Text Values ---
        holder.budgetAmount.text = "Budget: ₹$limit"
        holder.left.text = "Left: ₹$remaining"
        holder.used.text = "Used: ${min(percent, 100)}%"  // Cap display at 100% even if overspent

        // Cap progress bar at 100 to prevent overflow beyond the bar's max value
        holder.progressBar.progress = min(percent, 100)

        // --- Color Code by Spend Level ---
        // Green  → under 70% used (healthy)
        // Yellow → 70–89% used (caution)
        // Red    → 90%+ used (critical / over budget)
        val color = when {
            percent < 70 -> "#2E7D32"   // Green
            percent < 90 -> "#F9A825"   // Yellow/Amber
            else         -> "#C62828"   // Red
        }

        // Apply the same color to both the progress bar tint and the "Used" text
        val parsed = Color.parseColor(color)
        holder.progressBar.progressDrawable.setTint(parsed)
        holder.used.setTextColor(parsed)

        // --- Click Listener ---
        // Passes the tapped BudgetSummary back to the caller via the onClick lambda
        holder.itemView.setOnClickListener {
            onClick(item)
        }

        // --- Category Icon Mapping ---
        // Maps each known category name (case-insensitive, trimmed) to its drawable resource.
        // Falls back to R.drawable.other for any unrecognized category.
        when (item.category.lowercase().trim()) {
            // Income categories
            "salary"            -> holder.iconholder.setImageResource(R.drawable.salary)
            "business"          -> holder.iconholder.setImageResource(R.drawable.business)
            "freelance"         -> holder.iconholder.setImageResource(R.drawable.freelance)
            "investment"        -> holder.iconholder.setImageResource(R.drawable.investment)
            "savings"           -> holder.iconholder.setImageResource(R.drawable.savings)

            // Food & Dining
            "food"              -> holder.iconholder.setImageResource(R.drawable.food)
            "dining out"        -> holder.iconholder.setImageResource(R.drawable.dining_out)
            "grocery"           -> holder.iconholder.setImageResource(R.drawable.grocery)

            // Shopping & Lifestyle
            "shopping"          -> holder.iconholder.setImageResource(R.drawable.shopping)
            "personal care"     -> holder.iconholder.setImageResource(R.drawable.personalcare)
            "entertainment"     -> holder.iconholder.setImageResource(R.drawable.entertainment)
            "subscriptions"     -> holder.iconholder.setImageResource(R.drawable.subscription)
            "decoration"        -> holder.iconholder.setImageResource(R.drawable.decoration)

            // Housing & Utilities
            "housing"           -> holder.iconholder.setImageResource(R.drawable.housing)
            "rental"            -> holder.iconholder.setImageResource(R.drawable.rental)
            "electricity"       -> holder.iconholder.setImageResource(R.drawable.electricity)
            "water bill"        -> holder.iconholder.setImageResource(R.drawable.waterbill)
            "mobile recharge"   -> holder.iconholder.setImageResource(R.drawable.recharge)
            "fasttag recharge"  -> holder.iconholder.setImageResource(R.drawable.fastag)

            // Travel & Transport
            "travel"            -> holder.iconholder.setImageResource(R.drawable.travel)
            "taxi"              -> holder.iconholder.setImageResource(R.drawable.taxi)
            "auto"              -> holder.iconholder.setImageResource(R.drawable.auto)
            "hotel"             -> holder.iconholder.setImageResource(R.drawable.hotel)
            "flight"            -> holder.iconholder.setImageResource(R.drawable.flight)
            "public transport"  -> holder.iconholder.setImageResource(R.drawable.publictransport)

            // Fuel
            "petrol"            -> holder.iconholder.setImageResource(R.drawable.petrol)
            "diesel"            -> holder.iconholder.setImageResource(R.drawable.diesel)
            "cng"               -> holder.iconholder.setImageResource(R.drawable.cng)
            "lpg", "png"        -> holder.iconholder.setImageResource(R.drawable.lpgpng)  // Both gas types share one icon

            // Health & Finance
            "medical"           -> holder.iconholder.setImageResource(R.drawable.medical)
            "insurance"         -> holder.iconholder.setImageResource(R.drawable.insurance)
            "education"         -> holder.iconholder.setImageResource(R.drawable.education)
            "emi / loans"       -> holder.iconholder.setImageResource(R.drawable.loan)
            "tax"               -> holder.iconholder.setImageResource(R.drawable.tax)

            // Giving
            "gifts"             -> holder.iconholder.setImageResource(R.drawable.gift)
            "donations"         -> holder.iconholder.setImageResource(R.drawable.donation)
            "miscellaneous"     -> holder.iconholder.setImageResource(R.drawable.miscellaneous)

            // Fallback for any category not listed above
            else                -> holder.iconholder.setImageResource(R.drawable.other)


        }
    }

    /** Returns the total number of budget items currently in the adapter. */
    override fun getItemCount(): Int = budgets.size

    /**
     * Replaces the current budget list with a new one and refreshes the RecyclerView.
     * Called whenever the ViewModel pushes updated data (e.g., after a budget is added/deleted).
     *
     * Note: Consider replacing [notifyDataSetChanged] with [DiffUtil] for better performance
     * on large lists, as it only redraws changed items instead of the entire list.
     */
    fun updateList(newBudgets: List<BudgetSummary>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }
}