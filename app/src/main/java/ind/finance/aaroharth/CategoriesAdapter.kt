package ind.finance.aaroharth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(private val categoriesList:ArrayList<CategoriesDataClass>):
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.eachitem_categories, parent, false)
        return CategoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val categories = categoriesList[position]
        holder.imageView.setImageResource(categories.image)
        holder.textView.text = categories.name
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    class CategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView : ImageView = itemView.findViewById(R.id.categories_img)
        val textView : TextView = itemView.findViewById(R.id.categories_text)
    }
}