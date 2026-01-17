package ind.finance.aaroharth
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ind.finance.aaroharth.databinding.FragmentCarbonBinding
import kotlinx.coroutines.launch
import com.github.mikephil.charting.formatter.ValueFormatter


class CarbonFragment : Fragment() {

    private var _binding: FragmentCarbonBinding? = null
    private val binding get() = _binding!!
    private lateinit var dao: Transaction_Dao
    private var currentPeriod = "Weekly"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarbonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dao = App_Database.getInstance(requireContext()).transactionDao()
        setupRecyclerView()
        setupToggleButtons()
        setupEmptyCharts()
        loadData()

        binding.viewAll.setOnClickListener {
            startActivity(Intent(requireContext(), Co2AllTransactions::class.java))
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = Co2TransactionAdapter(emptyList())
        }
    }

    private fun setupToggleButtons() {

        updateToggleColors(R.id.btn_weekly)

        binding.toggleGroup.apply {
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    currentPeriod = when (checkedId) {
                        R.id.btn_weekly -> "Weekly"
                        R.id.btn_monthly -> "Monthly"
                        R.id.btn_yearly -> "Yearly"
                        else -> "Weekly"
                    }
                    updateToggleColors(checkedId)
                    loadData()
                }
            }
            check(R.id.btn_weekly)
        }
    }

    private fun updateToggleColors(checkedId: Int) {



        binding.btnWeekly.apply {
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            setTextColor(Color.parseColor("#4CAF50"))
        }
        binding.btnMonthly.apply {
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            setTextColor(Color.parseColor("#4CAF50"))
        }
        binding.btnYearly.apply {
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            setTextColor(Color.parseColor("#4CAF50"))
        }

        val activeButton = when (checkedId) {
            R.id.btn_weekly -> binding.btnWeekly
            R.id.btn_monthly -> binding.btnMonthly
            R.id.btn_yearly -> binding.btnYearly
            else -> binding.btnWeekly
        }

        activeButton?.apply {
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
        }
    }



    private fun setupEmptyCharts() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
        }
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
        }
        binding.barChart.apply {
            description.isEnabled = false
            setFitBars(true)
        }
    }

    private fun getPeriodRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (currentPeriod) {
            "Weekly" -> (now - 7 * 86400000L) to now
            "Monthly" -> (now - 30 * 86400000L) to now
            "Yearly" -> (now - 365 * 86400000L) to now
            else -> (now - 7 * 86400000L) to now
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val (startDate, endDate) = getPeriodRange()
                val transactions = dao.getRecentCo2Transactions(startDate, endDate)
                (binding.recyclerView.adapter as Co2TransactionAdapter).submitList(transactions)
                updateCharts(transactions)

            } catch (e: Exception) {
            }
        }
    }

    private fun updateCharts(transactions: List<Transaction_Info>) {
        if (transactions.isEmpty()) {
            showEmptyCharts()
            return
        }


        val categoryTotals = transactions.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.carbonEmitted }
            }

        updatePieChart(categoryTotals)
        updateLineChart(transactions)
        updateBarChart(categoryTotals)
    }

    private fun showEmptyCharts() {
        binding.pieChart.data = null
        binding.lineChart.data = null
        binding.barChart.data = null
        binding.pieChart.invalidate()
        binding.lineChart.invalidate()
        binding.barChart.invalidate()
    }

    private fun updatePieChart(categoryTotals: Map<String, Double>) {
        val top6 = categoryTotals.entries.sortedByDescending { it.value }.take(6)
        val othersTotal = categoryTotals.values.sum() - top6.sumOf { it.value }

        val entries = if (othersTotal > 0.1) {
            top6.map { PieEntry(it.value.toFloat(), it.key.take(8)) } + PieEntry(othersTotal.toFloat(), "Others")
        } else {
            top6.map { PieEntry(it.value.toFloat(), it.key.take(8)) }
        }

        if (entries.isEmpty()) return

        val totalSum = categoryTotals.values.sum()

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.rgb(255, 87, 34), Color.rgb(76, 175, 80),
                Color.rgb(33, 150, 243), Color.rgb(255, 193, 7),
                Color.rgb(156, 39, 176), Color.rgb(244, 67, 54),
                Color.GRAY
            )
            sliceSpace = 3f

            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueTypeface = Typeface.DEFAULT_BOLD

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val percentage = (value / totalSum.toFloat() * 100).toInt()
                    return "$percentage%"
                }
            }
        }

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            data = pieData
            centerText = "Total\n%.1f kg".format(totalSum)
            setCenterTextSize(16f)
            holeRadius = 45f
            transparentCircleRadius = 52f
            setDrawSliceText(false)

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                form = Legend.LegendForm.CIRCLE
                formSize = 12f
                textSize = 11f
                xEntrySpace = 12f
                yEntrySpace = 6f
                formToTextSpace = 8f
            }
            animateY(800)
            invalidate()
        }
    }

    private fun updateLineChart(transactions: List<Transaction_Info>) {

        val daysToShow = when(currentPeriod) {
            "Weekly" -> 7   // ALL 7 days
            "Monthly" -> 30 // ALL 30 days
            "Yearly" -> 365 // ALL 365 days
            else -> 7
        }

        val dailyData = transactions
            .groupBy { (it.dateAndTime / 86400000L).toInt() }
            .map { (day, txns) ->
                val totalCo2 = txns.sumOf { it.carbonEmitted }
                Entry(day.toFloat(), totalCo2.toFloat())
            }
            .sortedBy { it.x }
            .takeLast(daysToShow)

        if (dailyData.isEmpty()) return

        val lineDataSet = LineDataSet(dailyData, "Daily CO₂").apply {
            color = Color.rgb(33, 150, 243)
            valueTextColor = Color.rgb(33, 150, 243)
            lineWidth = 2.5f
            circleRadius = if (daysToShow <= 30) 4f else 2f
            setCircleColor(Color.rgb(33, 150, 243))
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChart.apply {
            data = LineData(lineDataSet)

            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when(currentPeriod) {
                        "Weekly" -> {
                            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            days[(value % 7).toInt()]
                        }
                        "Monthly" -> "${((value % 30) + 1).toInt()}d"
                        "Yearly" -> {
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            months[((value / 30.4f).toInt() % 12)]
                        }
                        else -> "Day"
                    }
                }
            }

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textSize = if (daysToShow <= 30) 10f else 8f
            setVisibleXRangeMaximum(daysToShow.toFloat())

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            xAxis.setDrawGridLines(true)
            axisLeft.setDrawGridLines(true)
            description.isEnabled = false
            legend.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    private fun updateBarChart(categoryTotals: Map<String, Double>) {
        val top6 = categoryTotals.entries.sortedByDescending { it.value }.take(6)
        val othersTotal = categoryTotals.values.sum() - top6.sumOf { it.value }

        val barEntries = if (othersTotal > 0.1) {
            top6.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            } + BarEntry(6f, othersTotal.toFloat())
        } else {
            top6.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
        }

        if (barEntries.isEmpty()) return

        val totalSum = categoryTotals.values.sum()

        val barDataSet = BarDataSet(barEntries, "Top CO₂ Categories").apply {
            colors = listOf(
                Color.rgb(255, 87, 34), Color.rgb(76, 175, 80),
                Color.rgb(33, 150, 243), Color.rgb(255, 193, 7),
                Color.rgb(156, 39, 176), Color.rgb(244, 67, 54),
                Color.GRAY
            )

            setDrawValues(true)
            valueTextSize = 11f
            valueTextColor = Color.BLACK
            valueTypeface = Typeface.DEFAULT_BOLD

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "%.1fkg".format(value)
                }
            }
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.7f
        }

        binding.barChart.apply {
            data = barData

            val labels = if (othersTotal > 0.1) {
                top6.map { it.key.take(5) } + listOf("Other")
            } else {
                top6.map { it.key.take(5) }
            }

            xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray())
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textSize = 9f
            xAxis.yOffset = 3f

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            setFitBars(true)

            description.isEnabled = false
            legend.isEnabled = false
            animateY(800)
            invalidate()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
