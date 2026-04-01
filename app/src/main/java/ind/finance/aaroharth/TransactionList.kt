package ind.finance.aaroharth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import ind.finance.aaroharth.adapters.TransactionListAdapter
import ind.finance.aaroharth.databinding.ActivityTransactionListBinding
import ind.finance.aaroharth.viewmodels.TransactionViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class TransactionList : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private val viewModel: TransactionViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }
    private lateinit var adapter: TransactionListAdapter
    private var selectedType = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TransactionListAdapter(emptyList())
        binding.tranList.adapter = adapter

        setupUI()
        setupSearch()
        observeViewModel()

        updateToggleUI()
        viewModel.loadTransactions(selectedType)
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.updatelist(transactions)
        }
    }

    private fun setupUI() {
        binding.toggleGroup.visibility = View.VISIBLE

        binding.btnIncome.setOnClickListener {
            selectedType = if (selectedType == "Income") "ALL" else "Income"
            updateToggleUI()
            viewModel.loadTransactions(selectedType)
        }

        binding.btnExpense.setOnClickListener {
            selectedType = if (selectedType == "Expense") "ALL" else "Expense"
            updateToggleUI()
            viewModel.loadTransactions(selectedType)
        }

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
            updateToggleUI()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
            viewModel.loadTransactions(selectedType)
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                if (query.isBlank()) {
                    viewModel.loadTransactions(selectedType)
                } else {
                    viewModel.searchTransactions(query, selectedType)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

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
        btn.setBackgroundColor(getColor(if (income) R.color.toggle_income_bg else R.color.toggle_expense_bg))
    }

    private fun styleUnselected(btn: MaterialButton) {
        btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        btn.setTypeface(null, android.graphics.Typeface.NORMAL)
        btn.setBackgroundColor(getColor(R.color.toggle_bg))
    }
}
