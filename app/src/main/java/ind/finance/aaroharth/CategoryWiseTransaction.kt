package ind.finance.aaroharth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import ind.finance.aaroharth.databinding.ActivityCategoryWiseTransactionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CategoryWiseTransaction : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryWiseTransactionBinding
    private lateinit var dao: Transaction_Dao
    private lateinit var adapter: CategoryOverviewAdapter

    // 🔥 SINGLE SOURCE OF TRUTH (NO ENUM)
    private var selectedType = "ALL"     // ALL | Income | Expense
    private var filterMode = "ALL_TIME"  // ALL_TIME | 7_DAYS | 30_DAYS | THIS_WEEK | THIS_MONTH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryWiseTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = App_Database.getInstance(this).transactionDao()

        adapter = CategoryOverviewAdapter(emptyList())
        binding.tranList.adapter = adapter

        setupUI()
        updateToggleUI()
        loadData()
    }

    // ---------------- UI ----------------

    private fun setupUI() {
        binding.back.setOnClickListener { finish() }

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

        binding.filterIcon.setOnClickListener {
            showFilterMenu()
        }
    }

    // ---------------- FILTER MENU ----------------

    private fun showFilterMenu() {
        val popup = PopupMenu(this, binding.filterIcon)

        popup.menu.add("All Time")
        popup.menu.add("Last 30 Days")
        popup.menu.add("Last 7 Days")
        popup.menu.add("This Month")
        popup.menu.add("This Week")

        popup.setOnMenuItemClickListener {
            filterMode = when (it.title.toString()) {
                "Last 7 Days" -> "7_DAYS"
                "Last 30 Days" -> "30_DAYS"
                "This Month" -> "THIS_MONTH"
                "This Week" -> "THIS_WEEK"
                else -> "ALL_TIME"
            }
            loadData()
            true
        }
        popup.show()
    }

    // ---------------- TOGGLE UI ----------------

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

    // ---------------- DATA ----------------

    private fun loadData() {
        lifecycleScope.launch {
            val fromTime = calculateFromTime()

            val result = withContext(Dispatchers.IO) {
                when (selectedType) {
                    "Income" ->
                        dao.getCategoryExpenseFiltered(fromTime, "Income")
                    "Expense" ->
                        dao.getCategoryExpenseFiltered(fromTime, "Expense")
                    else ->
                        dao.getCategoryExpense(fromTime)
                }
            }

            adapter.updateList(result)
        }
    }

    // ---------------- TIME FILTER ----------------

    private fun calculateFromTime(): Long {
        val cal = Calendar.getInstance()

        return when (filterMode) {
            "7_DAYS" ->
                System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

            "30_DAYS" ->
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000

            "THIS_WEEK" -> {
                cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }

            "THIS_MONTH" -> {
                cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }

            else -> 0L // ALL TIME
        }
    }
}
