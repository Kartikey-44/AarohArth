package ind.finance.aaroharth

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
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

        setupFilter()
        setupLineChartUI()

        refreshDashboard()
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

    private fun refreshDashboard() {
        loadExpenseGraph()
        loadSpendingPace()
        loadCategoryPie()
    }

    private fun daysRange(): Int =
        if (binding.filter.text.toString() == "Last 7 Days") 7 else 30

    private fun fromTime(): Long =
        System.currentTimeMillis() - daysRange() * 24 * 60 * 60 * 1000L

    // ---------------- LINE CHART ----------------

    private fun setupLineChartUI() {
        binding.linechart.apply {
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.dashboardText)
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.dashboardText)
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
        }
    }

    private fun loadExpenseGraph() {
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
                lineWidth = 2f
                mode = LineDataSet.Mode.LINEAR
                color = ContextCompat.getColor(requireContext(), R.color.chartAccent)
            }

            binding.linechart.data = LineData(dataSet)
            binding.linechart.invalidate()
        }
    }

    // ---------------- PACE ----------------

    private fun loadSpendingPace() {
        lifecycleScope.launch {
            val totalSpent = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalExpense(fromTime())
            }

            val balance = withContext(Dispatchers.IO) {
                db.accountDao().gettotalbalance()
            }

            val days = daysRange()
            val pace = totalSpent / days
            val remaining = if (pace > 0) ceil(balance / pace).toInt() else 0

            binding.pace.text = "₹${pace.toInt()} / day"
            binding.pace.setTextColor(ContextCompat.getColor(requireContext(),R.color.dashboardText))

            when {
                remaining <= 7 -> {
                    binding.remainingDays.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.redwarn)
                    )
                }
                remaining <= 15 -> {
                    binding.remainingDays.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.amber)
                    )
                }
                else -> {
                    binding.remainingDays.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                }
            }

            binding.remainingDays.text =
                if (remaining > 0)
                    "Balance will be exhausted in ~$remaining days"
                else
                    "No spending detected"
        }
    }

    // ---------------- PIE CHART ----------------

    private fun loadCategoryPie() {
        lifecycleScope.launch {
            val categories = withContext(Dispatchers.IO) {
                db.transactionDao().getCategoryExpense(fromTime())
            }

            if (categories.isEmpty()) {
                binding.piechart.clear()
                return@launch
            }

            // 1️⃣ Pie entries
            val entries = categories.take(5).map {
                PieEntry(it.total.toFloat(), it.category)
            }

            // 2️⃣ DataSet (FIXED)
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 2f
                setDrawValues(true)

                // 🔹 VALUE LINES (THIS WAS MISSING)
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.6f
                valueLineWidth = 1.5f
                valueLineColor =
                    ContextCompat.getColor(requireContext(), R.color.dashboardText)

                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

                colors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.pie1),
                    ContextCompat.getColor(requireContext(), R.color.pie2),
                    ContextCompat.getColor(requireContext(), R.color.pie3),
                    ContextCompat.getColor(requireContext(), R.color.pie4),
                    ContextCompat.getColor(requireContext(), R.color.pie5)
                )
            }

            // 3️⃣ PieData (₹ formatter is correct)
            val pieData = PieData(dataSet).apply {
                setValueTextColor(
                    ContextCompat.getColor(requireContext(), R.color.dashboardText)
                )
                setValueTextSize(12f)
                setValueFormatter(object :
                    com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "₹${value.toInt()}"
                    }
                })
            }
            binding.piechart.rotationAngle=-260f

            // 4️⃣ Apply to chart
            binding.piechart.apply {
                setExtraOffsets(
                    30f,  // left
                    30f,  // top   👈 IMPORTANT
                    30f,  // right
                    10f   // bottom
                )

                data = pieData
                description.isEnabled = false

                legend.isEnabled = true
                legend.textColor =
                    ContextCompat.getColor(requireContext(), R.color.dashboardText)
                legend.textSize=10f

                setDrawEntryLabels(false)   // IMPORTANT
                setUsePercentValues(false)
                isRotationEnabled = false

                holeRadius = 60f
                setHoleColor(Color.TRANSPARENT)

                invalidate()
            }
        }
    }

}
