package ind.finance.aaroharth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TransactionPageListAdapter(
    private val transactions: List<Transaction_Info>
): RecyclerView.Adapter<TransactionPageListAdapter.TransactionViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_transaction,parent,false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val transaction=transactions[position]
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    class TransactionViewHolder(itemview: View): RecyclerView.ViewHolder(itemview){

    }
}