package ind.finance.aaroharth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import ind.finance.aaroharth.databinding.ActivityTransactionListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionList : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var dao: Transaction_Dao
    private lateinit var adapter: TransactionListAdapter

    private var searchJob: Job? = null

    // SINGLE SOURCE OF TRUTH
    private var selectedType = "ALL" // ALL | Income | Expense

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = App_Database.getInstance(this).transactionDao()
        adapter = TransactionListAdapter(emptyList())
        binding.tranList.adapter = adapter

        setupUI()
        setupSearch()

        updateToggleUI()   // 🔥 force UI sync
        loadData()
    }

    // ---------------- UI ----------------

    private fun setupUI() {
        binding.toggleGroup.visibility = View.VISIBLE

        binding.btnIncome.setOnClickListener {
            selectedType = if (selectedType == "Income") "ALL" else "Income"
            updateToggleUI()
            loadData()
        }

        binding.btnExpense.setOnClickListener {
            selectedType = if (selectedType == "Expense") "ALL" else "Expense"
            updateToggleUI()
            loadData()
        }

        // Search
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
            binding.searchInput.text?.clear()

            updateToggleUI() // 🔥 restore UI safely

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
        }
    }

    // ---------------- DATA ----------------

    private fun loadData() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (selectedType) {
                    "Income" -> dao.gettransaction("Income")
                    "Expense" -> dao.gettransaction("Expense")
                    else -> dao.getalltransaction()
                }
            }
            adapter.updatelist(result)
        }
    }

    // ---------------- SEARCH ----------------

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)

                    val result = withContext(Dispatchers.IO) {
                        when {
                            query.isBlank() -> {
                                when (selectedType) {
                                    "Income" -> dao.gettransaction("Income")
                                    "Expense" -> dao.gettransaction("Expense")
                                    else -> dao.getalltransaction()
                                }
                            }
                            selectedType == "Income" ->
                                dao.searchTransactions(query, "Income")
                            selectedType == "Expense" ->
                                dao.searchTransactions(query, "Expense")
                            else ->
                                dao.searchTransactionstype(query)
                        }
                    }
                    adapter.updatelist(result)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ---------------- UI STATE ----------------

    private fun updateToggleUI() {
        when (selectedType) {
            "Income" -> {
                binding.titleText.text = "Income Transactions"
                styleSelected(binding.btnIncome, income = true)
                styleUnselected(binding.btnExpense)
            }
            "Expense" -> {
                binding.titleText.text = "Expense Transactions"
                styleSelected(binding.btnExpense, income = false)
                styleUnselected(binding.btnIncome)
            }
            "ALL" -> {
                binding.titleText.text = "All Transactions"
                styleUnselected(binding.btnIncome)
                styleUnselected(binding.btnExpense)
            }
        }
    }

    private fun styleSelected(btn: MaterialButton, income: Boolean) {
        btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_4dp)
        btn.setTypeface(null, android.graphics.Typeface.BOLD)
        btn.setBackgroundColor(
            getColor(if (income) R.color.toggle_income_bg else R.color.toggle_expense_bg)
        )
    }

    private fun styleUnselected(btn: MaterialButton) {
        btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        btn.setTypeface(null, android.graphics.Typeface.NORMAL)
        btn.setBackgroundColor(getColor(R.color.toggle_bg))
    }
}
