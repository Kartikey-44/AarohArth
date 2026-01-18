package ind.finance.aaroharth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import ind.finance.aaroharth.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var db: App_Database

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = App_Database.getInstance(requireContext())
        binding.categoryWiseExpense.layoutManager =
            LinearLayoutManager(requireContext())


        setupFilter()
        setupLineChartUI()
        refreshDashboard()

        binding.seeall.setOnClickListener {
            val intent = Intent(requireContext(), CategoryWiseTransaction::class.java)
            startActivity(intent)
        }

    }

    // ---------------- FILTER ----------------

    private fun setupFilter() {
        val items = listOf("Last 7 Days", "Last 30 Days")
        binding.filter.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        )
        binding.filter.setText(items[0], false)

        binding.filter.setOnItemClickListener { _, _, _, _ ->
            refreshDashboard()
        }
    }

    private fun daysRange(): Int =
        if (binding.filter.text.toString() == "Last 7 Days") 7 else 30

    private fun fromTime(): Long =
        System.currentTimeMillis() - daysRange() * 24 * 60 * 60 * 1000L

    // ---------------- DASHBOARD ----------------

    private fun refreshDashboard() {
        lifecycleScope.launch {
            val topCategories = withContext(Dispatchers.IO) {
                db.transactionDao().getCategoryExpense(fromTime()).take(5)
            }

            loadExpenseLineChart()
            loadSpendingPace()
            loadPieChart(topCategories)
            loadBarChart(topCategories)
            val adapter = CategoryOverviewAdapter(topCategories)
            binding.categoryWiseExpense.adapter = adapter

        }
    }

    // ---------------- LINE CHART (TOTAL EXPENSE) ----------------

    private fun setupLineChartUI() {
        binding.linechart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), android.R.color.black)
                axisLineColor = ContextCompat.getColor(requireContext(), android.R.color.black)
                setDrawAxisLine(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "₹${value.toInt()}"
                }
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), android.R.color.black)
                axisLineColor = ContextCompat.getColor(requireContext(), android.R.color.black)
                setDrawGridLines(false)
                setDrawAxisLine(true)
            }
        }
    }

    private fun loadExpenseLineChart() {
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                db.transactionDao().getDailyExpense(fromTime())
            }

            val entries = data.mapIndexed { index, item ->
                Entry(index.toFloat(), item.total.toFloat())
            }

            val dataSet = LineDataSet(entries, "").apply {
                setDrawValues(false)
                setDrawCircles(false)
                lineWidth = 2.5f
                color = ContextCompat.getColor(requireContext(), R.color.chartAccent)
            }

            binding.linechart.apply {
                this.data = LineData(dataSet)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        (value.toInt() + 1).toString()
                }
                invalidate()
            }
        }
    }

    // ---------------- SPENDING PACE ----------------

    private fun loadSpendingPace() {
        lifecycleScope.launch {
            val totalSpent = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalExpense(fromTime())
            }
            val balance = withContext(Dispatchers.IO) {
                db.accountDao().gettotalbalance()
            }

            val pace = if (daysRange() > 0) totalSpent / daysRange() else 0.0
            val remaining = if (pace > 0) ceil(balance / pace).toInt() else 0

            binding.pace.text = "₹${pace.toInt()} / day"
            binding.remainingDays.text =
                if (remaining > 0) "Balance will be exhausted in ~$remaining days"
                else "No spending detected"
        }
    }

    // ---------------- PIE CHART (PERCENTAGE ONLY) ----------------

    private fun loadPieChart(categories: List<CategoryExpense>) {
        if (categories.isEmpty()) {
            binding.pieChart.clear()
            return
        }

        val total = categories.sumOf { it.total }

        val entries = categories.map {
            val percent = (it.total / total) * 100
            PieEntry(
                percent.toFloat(),
                "${it.category} (₹${it.total.toInt()})"
            )
        }



        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.pie1),
                ContextCompat.getColor(requireContext(), R.color.pie2),
                ContextCompat.getColor(requireContext(), R.color.pie3),
                ContextCompat.getColor(requireContext(), R.color.pie4),
                ContextCompat.getColor(requireContext(), R.color.pie5)
            )
            setDrawValues(true)   // NO % TEXT ON PIE
        }


        binding.pieChart.apply {
            setUsePercentValues(true)
            data = PieData(dataSet).apply {
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 10f) {
                            "${value.toInt()}%"
                        } else {
                            ""   // 🔥 hide text below 10%
                        }
                    }
                })
                setValueTextSize(18f)
            }


            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize=10f
            invalidate()
        }
    }

    // ---------------- BAR CHART ----------------

    private fun loadBarChart(categories: List<CategoryExpense>) {
        if (categories.isEmpty()) {
            binding.barChart.clear()
            return
        }

        val entries = categories.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.total.toFloat())
        }

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.pie1),
                ContextCompat.getColor(requireContext(), R.color.pie2),
                ContextCompat.getColor(requireContext(), R.color.pie3),
                ContextCompat.getColor(requireContext(), R.color.pie4),
                ContextCompat.getColor(requireContext(), R.color.pie5)
            )
            valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            valueTextSize = 14f
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.6f
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "₹${value.toInt()}"
                })
            }

            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.textColor =
                ContextCompat.getColor(requireContext(), android.R.color.black)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor =
                    ContextCompat.getColor(requireContext(), android.R.color.black)
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val i = value.toInt()
                        return if (i in categories.indices) categories[i].category else ""
                    }
                }
            }

            setFitBars(true)
            invalidate()
        }
    }
}
