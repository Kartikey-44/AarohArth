package ind.finance.aaroharth.adapters

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.CategoriesDataClass

/**
 * RecyclerView Adapter for displaying transaction categories in a grid.
 *
 * Supports three display modes (neutral / income / expense) which change
 * the card background color to give the user visual context about which
 * type of categories are currently being shown.
 *
 * @param categoriesList Initial list of categories to display
 */
class CategoriesAdapter(private val categoriesList: ArrayList<CategoriesDataClass>) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    /**
     * Tracks the current display mode for card background coloring:
     * - 0 → Neutral (default, no tint)
     * - 1 → Income mode (green tint)
     * - 2 → Expense mode (red tint)
     */
    private var currentMode: Int = 0

    /**
     * Optional click listener — invoked with the tapped [CategoriesDataClass]
     * when the user taps a category card. Set externally by the Fragment.
     */
    var onItemClick: ((CategoriesDataClass) -> Unit)? = null

    /**
     * Updates the display mode based on the Income/Expense toggle state
     * and refreshes all visible cards to reflect the new background color.
     *
     * @param incomeMode true = Income, false = Expense, null = Neutral/All
     */
    fun updateMode(incomeMode: Boolean?) {
        currentMode = when (incomeMode) {
            true  -> 1  // Income mode
            false -> 2  // Expense mode
            null  -> 0  // Neutral / All mode
        }
        notifyDataSetChanged()
    }

    /**
     * Inflates the eachitem_categories layout and wraps it in a [CategoriesViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.eachitem_categories, parent, false)
        return CategoriesViewHolder(view)
    }

    /**
     * Binds a [CategoriesDataClass] item to the views in [CategoriesViewHolder].
     * Sets the category icon, name, click listener, and card background color
     * based on the current Income/Expense/Neutral mode.
     */
    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val category = categoriesList[position]

        // Bind category icon and name to the card
        holder.imageView.setImageResource(category.image)
        holder.textView.text = category.name

        // Notify the Fragment which category was tapped
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(category)
        }

        // Detect whether the device is currently in dark mode
        val isNightMode = (holder.itemView.context.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // Apply card background color based on current mode
        // Note: currently both dark and light mode use the same color resource —
        // update colorRes per branch if you want distinct dark-mode colors in future
        when (currentMode) {
            1 -> {
                // Income mode — green card background
                val colorRes = if (isNightMode) R.color.income_card_bg else R.color.income_card_bg
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, colorRes)
                )
            }
            2 -> {
                // Expense mode — red card background
                val colorRes = if (isNightMode) R.color.expense_card_bg else R.color.expense_card_bg
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, colorRes)
                )
            }
            else -> {
                // Neutral / All mode — reset to default card background (no tint)
                // TODO: explicitly reset card background to default theme color here
                // e.g. holder.cardView.setCardBackgroundColor(ContextCompat.getColor(..., R.color.default_card_bg))
            }
        }
    }

    /** Returns the total number of categories in the list. */
    override fun getItemCount(): Int = categoriesList.size

    /**
     * Updates the displayed category list and refreshes the RecyclerView.
     * Called by the Fragment whenever the ViewModel pushes a new filtered or full list.
     *
     * Note: Consider using [DiffUtil] instead of [notifyDataSetChanged] for
     * better performance and smooth item animations on large lists.
     *
     * TODO: Parameter type should be List<CategoriesDataClass>, not Any —
     *  the current signature accepts anything and does nothing, which is a bug.
     */
    fun updateList(list: List<CategoriesDataClass>) {
        categoriesList.clear()
        categoriesList.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder that caches view references for each category card (eachitem_categories.xml).
     * Avoids repeated findViewById calls during RecyclerView scrolling.
     */
    class CategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.categories_img)   // Category icon
        val textView: TextView   = itemView.findViewById(R.id.categories_text)  // Category name
        val cardView: CardView   = itemView.findViewById(R.id.card_view_parent) // Card container
    }
}