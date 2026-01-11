package ind.finance.aaroharth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.ActivityTransactionListBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp

class TransactionList : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var dao: Transaction_Dao
    private lateinit var adapter: TransactionListAdapter
    private var searchJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val type = intent.getStringExtra("type")
        val category = intent.getStringExtra("category")

        if (type == "all" && category == "all") {
            binding.titleText.text = "All Transactions"
            binding.toggleGroup.visibility = View.VISIBLE

        }

        binding.btnIncome.setOnClickListener {
            income()
        }
        binding.btnExpense.setOnClickListener {
            expense()
        }


        dao = App_Database.getInstance(this).transactionDao()
        adapter = TransactionListAdapter(emptyList())
        binding.tranList.adapter = adapter


        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.titleText.visibility = View.GONE
            binding.searchInput.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchInput.requestFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.back.setOnClickListener {
            binding.searchIcon.visibility = View.VISIBLE
            binding.titleText.visibility = View.VISIBLE
            binding.searchInput.visibility = View.GONE
            binding.back.visibility = View.GONE

            binding.searchInput.clearFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)

        }

        setupSearch()
    }


    private fun income() {
        binding.titleText.text = "Income Transactions"
        binding.btnIncome.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_4dp)
        binding.btnExpense.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        lifecycleScope.launch {
            val transactions = dao.gettransaction("Income")
            adapter.updatelist(transactions)

        }
        binding.btnIncome.isSelected = true
        if (binding.btnExpense.isSelected) {
            binding.btnExpense.isSelected = false
        }
        binding.btnIncome.setTypeface(null, android.graphics.Typeface.BOLD)
        binding.btnExpense.setTypeface(null, android.graphics.Typeface.NORMAL)
        binding.btnIncome.setBackgroundColor(getColor(R.color.toggle_income_bg))
        binding.btnExpense.setBackgroundColor(getColor(R.color.toggle_bg))

    }

    private fun expense() {
        binding.titleText.text = "Expense Transactions"
        binding.btnIncome.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        binding.btnExpense.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_4dp)
        lifecycleScope.launch {
            val transactions = dao.gettransaction("Expense")
            adapter.updatelist(transactions)

        }
        binding.btnExpense.isSelected = true
        if (binding.btnIncome.isSelected) {
            binding.btnIncome.isSelected = false
        }
        binding.btnExpense.setTypeface(null, android.graphics.Typeface.BOLD)
        binding.btnIncome.setTypeface(null, android.graphics.Typeface.NORMAL)
        binding.btnIncome.setBackgroundColor(getColor(R.color.toggle_bg))
        binding.btnExpense.setBackgroundColor(getColor(R.color.toggle_expense_bg))
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val transactions = dao.getalltransaction()
            adapter.updatelist(transactions)

        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // debounce

                    val result =
                        if (query.isBlank()) {
                            dao.getalltransaction()
                        } else if (binding.btnIncome.isSelected) {
                            dao.searchTransactions(query, "Income")
                        } else if (binding.btnExpense.isSelected) {
                            dao.searchTransactions(query, "Expense")
                        } else {
                            dao.searchTransactionstype(query)
                        }

                    adapter.updatelist(result)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}
