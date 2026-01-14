package ind.finance.aaroharth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.databinding.ActivityBudgetManagementBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetManagement : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetManagementBinding
    private lateinit var db: App_Database
    private lateinit var adapter: BudgetAdapter

    // single source of truth for this screen
    private var currentMonthKey: String = currentMonthKey()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBudgetManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = App_Database.getInstance(this)

        setupRecycler()
        setupMonthHeader()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        loadBudgets()
    }

    // ---------------- SETUP ----------------

    private fun setupRecycler() {
        adapter = BudgetAdapter { budget ->

            // navigate to modification screen
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
            loadBudgets()
        }

        binding.next.setOnClickListener {
            currentMonthKey = nextMonth(currentMonthKey)
            binding.currentmonth.text = displayMonth(currentMonthKey)
            loadBudgets()
        }
    }

    private fun setupActions() {
        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, BudgetActions::class.java))
        }
    }

    // ---------------- DATA ----------------

    private fun loadBudgets() {
        lifecycleScope.launch {
            val list = db.budgetDao().getBudgetSummary(currentMonthKey)
            adapter.submitList(list)
        }
    }

    // ---------------- MONTH UTILS ----------------

    private fun currentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

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
