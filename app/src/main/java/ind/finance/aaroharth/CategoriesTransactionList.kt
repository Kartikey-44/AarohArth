package ind.finance.aaroharth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.databinding.ActivityCategoriestransactionListBinding
import kotlinx.coroutines.launch

class CategoriesTransactionList : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriestransactionListBinding

    private lateinit var dao: Transaction_Dao
    private lateinit var adapter: TransactionListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriestransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Unknown"
        val transactionType = intent.getStringExtra("TRANSACTION_TYPE") ?: "ALL"
        binding.categoriesName.text = "$categoryName ($transactionType)"

        dao = App_Database.getInstance(this).transactionDao()
        adapter = TransactionListAdapter(emptyList())
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(this)  // Your RecyclerView ID
        binding.categoriesRecyclerView.adapter = adapter

        loadTransactions()
    }
    private fun loadTransactions() {
        lifecycleScope.launch {

            val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
            val transactionType = intent.getStringExtra("TRANSACTION_TYPE") ?: "ALL"

            val transactions = dao.getTransactionsByCategory(categoryName, transactionType)

            adapter.updatelist(transactions)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}