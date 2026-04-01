package ind.finance.aaroharth.categoriesFragments

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.adapters.TransactionListAdapter
import ind.finance.aaroharth.databinding.ActivityCategoriestransactionListBinding
import ind.finance.aaroharth.viewmodels.CategoriesTransactionViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class CategoriesTransactionList : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriestransactionListBinding
    private val viewModel: CategoriesTransactionViewModel by viewModels {
        val app= application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }
    private lateinit var adapter: TransactionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriestransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Unknown"
        val transactionType = intent.getStringExtra("TRANSACTION_TYPE") ?: "ALL"
        binding.categoriesName.text = "$categoryName ($transactionType)"

        adapter = TransactionListAdapter(emptyList())
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoriesRecyclerView.adapter = adapter

        // Observe data from ViewModel
        viewModel.transactions.observe(this) { transactions ->
            adapter.updatelist(transactions)
        }

        // Trigger data load
        viewModel.loadTransactions(categoryName, transactionType)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}