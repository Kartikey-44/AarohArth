package ind.finance.aaroharth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView



class CategoriesAdapter(private val categoriesList:ArrayList<CategoriesDataClass>):
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {
    private var currentMode: Int = 0  // 0=neutral, 1=income, 2=expense
    fun updateMode(incomeMode: Boolean?) {
        currentMode = when (incomeMode){
            true -> 1
            false -> 2
            null -> 0
        }
        notifyDataSetChanged()
    }

    var onItemClick : ((CategoriesDataClass) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.eachitem_categories, parent, false)
        return CategoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val categories = categoriesList[position]
        holder.imageView.setImageResource(categories.image)
        holder.textView.text = categories.name

        //Categories Transaction Page
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(categories)
        }

        val isNightMode = (holder.itemView.context.resources.configuration.uiMode
                and android.content.res.Configuration.UI_MODE_NIGHT_MASK.toInt()) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES.toInt()

        when (currentMode) {
            1 -> { // Income mode - Light green
                val colorRes = if (isNightMode) R.color.income_card_bg
                else R.color.income_card_bg
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, colorRes))
            }
            2 -> { // Expense mode - Light red
                val colorRes = if (isNightMode) R.color.expense_card_bg
                else R.color.expense_card_bg
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, colorRes))
            }
            else -> { ///Default

            }
        }
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    class CategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView : ImageView = itemView.findViewById(R.id.categories_img)
        val textView : TextView = itemView.findViewById(R.id.categories_text)
        val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.card_view_parent)
    }
}