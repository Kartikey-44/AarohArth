package ind.finance.aaroharth.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ind.finance.aaroharth.data.model.Notification_History_Info
import ind.finance.aaroharth.databinding.ItemNotificationHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHistoryAdapter(
    private var items: List<Notification_History_Info>
) : RecyclerView.Adapter<NotificationHistoryAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(
        private val binding: ItemNotificationHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notification_History_Info) {
            binding.tvTitle.text = item.title
            binding.tvMessage.text = item.message
            //binding.tvType.text = item.type
            binding.tvTime.text = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date(item.createdAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Notification_History_Info>) {
        items = newItems
        notifyDataSetChanged()
    }
}