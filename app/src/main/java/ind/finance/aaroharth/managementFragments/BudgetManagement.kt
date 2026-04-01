package ind.finance.aaroharth.managementFragments

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.add_delete_edit_Fragments.BudgetActions
import ind.finance.aaroharth.modificationsFragments.BudgetModification
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.adapters.BudgetAdapter
import ind.finance.aaroharth.databinding.ActivityBudgetManagementBinding
import ind.finance.aaroharth.viewmodels.BudgetViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BudgetManagement : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetManagementBinding
    private lateinit var adapter: BudgetAdapter
    private val viewModel: BudgetViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }

    private var currentMonthKey: String = currentMonthKey()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBudgetManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupMonthHeader()
        setupActions()
        observeViewModel()
    }

    private fun setupRecycler() {
        adapter = BudgetAdapter(emptyList()) { budget ->
            val intent = Intent(this, BudgetModification::class.java)
            intent.putExtra("budget_id", budget.id)
            startActivity(intent)
        }
        binding.yourBudgetList.layoutManager = LinearLayoutManager(this)
        binding.yourBudgetList.adapter = adapter
    }

    private fun setupMonthHeader() {
        binding.currentmonth.text = displayMonth(currentMonthKey)
        binding.previous.setOnClickListener {
            currentMonthKey = prevMonth(currentMonthKey)
            binding.currentmonth.text = displayMonth(currentMonthKey)
            viewModel.loadBudgetSummary(currentMonthKey)
        }
        binding.next.setOnClickListener {
            currentMonthKey = nextMonth(currentMonthKey)
            binding.currentmonth.text = displayMonth(currentMonthKey)
            viewModel.loadBudgetSummary(currentMonthKey)
        }
    }

    private fun setupActions() {
        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, BudgetActions::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.budgetSummary.observe(this) { list ->
            adapter.updateList(list)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBudgetSummary(currentMonthKey)
    }

    private fun currentMonthKey(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private fun displayMonth(monthKey: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val out = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return out.format(sdf.parse(monthKey)!!)
    }

    private fun nextMonth(monthKey: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(monthKey)!!
        cal.add(Calendar.MONTH, 1)
        return sdf.format(cal.time)
    }

    private fun prevMonth(monthKey: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(monthKey)!!
        cal.add(Calendar.MONTH, -1)
        return sdf.format(cal.time)
    }
}