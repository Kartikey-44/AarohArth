package ind.finance.aaroharth.dashboardFragments

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter

import ind.finance.aaroharth.categoriesFragments.CategoryWiseTransaction
import ind.finance.aaroharth.adapters.CategoryOverviewAdapter
import ind.finance.aaroharth.data.local.App_Database
import ind.finance.aaroharth.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import ind.finance.aaroharth.data.model.CategoryExpense

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var db: App_Database

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                topInset,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = App_Database.Companion.getInstance(requireContext())
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
        val items = listOf("Last 7 Days", "Last 30 Days", "Last 365 Days")
        binding.filter.setAdapter(
            ArrayAdapter(requireContext(), R.layout.simple_list_item_1, items)
        )
        binding.filter.setText(items[0], false)

        binding.filter.setOnItemClickListener { _, _, _, _ ->
            refreshDashboard()
        }
        binding.linechart.setHardwareAccelerationEnabled(false)
    }

    private fun daysRange(): Int =
        when (binding.filter.text.toString()) {
            "Last 7 Days"   -> 7
            "Last 30 Days"  -> 30
            "Last 365 Days" -> 365
            else            -> 7
        }

    private fun fromTime(): Long =
        System.currentTimeMillis() - daysRange() * 24L * 60 * 60 * 1000

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

            setTouchEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDragEnabled(false)

            axisLeft.apply {
                axisMinimum = 0f
                spaceBottom = 30f
                textColor = ContextCompat.getColor(requireContext(), R.color.black)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.black)
                setDrawGridLines(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "₹${value.toInt()}"
                    }
                }
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textColor = ContextCompat.getColor(requireContext(), R.color.black)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.black)
            }
        }
    }

    private fun loadExpenseLineChart() {
        lifecycleScope.launch {
            val rawData = withContext(Dispatchers.IO) {
                db.transactionDao().getDailyExpense(fromTime())
            }

            if (rawData.isEmpty()) {
                binding.linechart.clear()
                return@launch
            }

            val entries = rawData.mapIndexed { index, item ->
                Entry(index.toFloat(), item.total.toFloat())
            }

            val maxY = entries.maxOf { it.y }

            if (maxY == 0f) {
                entries.forEach { it.y = 1f }
            }

            val lineColor = ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.red)

            val dataSet = LineDataSet(entries, "").apply {
                setDrawValues(false)
                color = lineColor
                lineWidth = 3f
                setDrawCircles(true)
                setDrawCircleHole(false)
                circleRadius = 4f
                setCircleColor(lineColor)
                isHighlightEnabled = false
                mode = LineDataSet.Mode.LINEAR
            }

            binding.linechart.apply {
                data = LineData(dataSet)

                axisLeft.axisMaximum =
                    if (maxY == 0f) 2f else maxY * 1.2f

                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = (entries.size - 1).toFloat()

                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (binding.filter.text.toString()) {
                            "Last 365 Days" -> "Mo ${value.toInt() + 1}"
                            else -> "Day ${value.toInt() + 1}"
                        }
                    }
                }

                notifyDataSetChanged()
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

    // ---------------- PIE CHART ----------------

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
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie1),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie2),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie3),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie4),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie5)
            )
            setDrawValues(true)
        }

        binding.pieChart.apply {
            setUsePercentValues(true)
            data = PieData(dataSet).apply {
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 10f) "${value.toInt()}%" else ""
                    }
                })
                setValueTextSize(18f)
            }
            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 10f
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
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie1),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie2),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie3),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie4),
                ContextCompat.getColor(requireContext(), ind.finance.aaroharth.R.color.pie5)
            )
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
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
                ContextCompat.getColor(requireContext(), R.color.black)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor =
                    ContextCompat.getColor(requireContext(), R.color.black)
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