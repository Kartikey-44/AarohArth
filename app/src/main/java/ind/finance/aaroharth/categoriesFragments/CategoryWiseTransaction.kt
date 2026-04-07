package ind.finance.aaroharth.categoriesFragments

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.button.MaterialButton
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.R
import ind.finance.aaroharth.adapters.CategoryOverviewAdapter
import ind.finance.aaroharth.databinding.ActivityCategoryWiseTransactionBinding
import ind.finance.aaroharth.viewmodels.DashboardViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import ind.finance.aaroharth.data.model.CategoryExpense

class CategoryWiseTransaction : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryWiseTransactionBinding
    private val viewModel: DashboardViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }
    private lateinit var adapter: CategoryOverviewAdapter

    private var selectedType = "ALL"
    private var daysRange = 0 // 0 for All Time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryWiseTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CategoryOverviewAdapter(emptyList())
        binding.tranList.adapter = adapter

        setupUI()
        observeViewModel()
        updateToggleUI()
        viewModel.refreshDashboard(daysRange)
    }

    private fun setupUI() {
        binding.back.setOnClickListener { finish() }

        binding.btnIncome.setOnClickListener {
            selectedType = if (selectedType == "Income") "ALL" else "Income"
            updateToggleUI()
            viewModel.refreshDashboard(daysRange)
        }

        binding.btnExpense.setOnClickListener {
            selectedType = if (selectedType == "Expense") "ALL" else "Expense"
            updateToggleUI()
            viewModel.refreshDashboard(daysRange)
        }

        binding.filterIcon.setOnClickListener { showFilterMenu() }
    }

    private fun observeViewModel() {
        viewModel.topCategories.observe(this) { list ->

            adapter.updateList(list)
        }
    }

    private fun showFilterMenu() {
        val popup = PopupMenu(this, binding.filterIcon)
        popup.menu.add("All Time")
        popup.menu.add("Last 30 Days")
        popup.menu.add("Last 7 Days")

        popup.setOnMenuItemClickListener {
            daysRange = when (it.title.toString()) {
                "Last 7 Days" -> 7
                "Last 30 Days" -> 30
                else -> 0
            }
            viewModel.refreshDashboard(daysRange)
            true
        }
        popup.show()
    }

    private fun updateToggleUI() {
        when (selectedType) {
            "Income" -> {
                binding.titleText.text = "Income Categories"
                styleSelected(binding.btnIncome, true)
                styleUnselected(binding.btnExpense)
            }
            "Expense" -> {
                binding.titleText.text = "Expense Categories"
                styleSelected(binding.btnExpense, false)
                styleUnselected(binding.btnIncome)
            }
            else -> {
                binding.titleText.text = "All Categories"
                styleUnselected(binding.btnIncome)
                styleUnselected(binding.btnExpense)
            }
        }
    }

    private fun styleSelected(btn: MaterialButton, income: Boolean) {
        btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_4dp)
        btn.setTypeface(null, Typeface.BOLD)
        btn.setBackgroundColor(getColor(if (income) R.color.toggle_income_bg else R.color.toggle_expense_bg))
    }

    private fun styleUnselected(btn: MaterialButton) {
        btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        btn.setTypeface(null, Typeface.NORMAL)
        btn.setBackgroundColor(getColor(R.color.toggle_bg))
    }
}