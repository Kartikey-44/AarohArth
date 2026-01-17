package ind.finance.aaroharth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.databinding.ActivityCo2AllTransactionsBinding
import kotlinx.coroutines.launch

class Co2AllTransactions : AppCompatActivity() {

    private lateinit var binding: ActivityCo2AllTransactionsBinding
    private lateinit var dao: Transaction_Dao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCo2AllTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = App_Database.getInstance(this).transactionDao()


        binding.c02RecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Co2AllTransactions)
            adapter = Co2TransactionAdapter(emptyList())
        }

        loadAllCo2Transactions()
    }

    private fun loadAllCo2Transactions() {
        lifecycleScope.launch {
            val transactions = dao.getRecentCo2Transactions(0, Long.MAX_VALUE)
            (binding.c02RecyclerView.adapter as Co2TransactionAdapter).submitList(transactions)
        }

    }
}