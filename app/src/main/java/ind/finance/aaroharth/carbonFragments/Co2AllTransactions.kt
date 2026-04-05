package ind.finance.aaroharth.carbonFragments

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.adapters.Co2TransactionAdapter
import ind.finance.aaroharth.databinding.ActivityCo2AllTransactionsBinding
import ind.finance.aaroharth.viewmodels.CarbonViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class Co2AllTransactions : AppCompatActivity() {

    private lateinit var binding: ActivityCo2AllTransactionsBinding
    private val viewModel: CarbonViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCo2AllTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.c02RecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Co2AllTransactions)
            adapter = Co2TransactionAdapter(emptyList())
        }

        observeViewModel()
        viewModel.loadData() // Defaults to Weekly, but we might want "All"
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            (binding.c02RecyclerView.adapter as Co2TransactionAdapter).submitList(transactions)
        }
    }
}