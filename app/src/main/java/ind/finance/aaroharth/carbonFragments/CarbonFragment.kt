package ind.finance.aaroharth.carbonFragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import ind.finance.aaroharth.carbonFragments.Co2AllTransactions
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.R
import ind.finance.aaroharth.adapters.Co2TransactionAdapter
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.databinding.FragmentCarbonBinding
import ind.finance.aaroharth.viewmodels.CarbonViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import kotlin.collections.plus

class CarbonFragment : Fragment() {

    private var _binding: FragmentCarbonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CarbonViewModel by viewModels {
        val app = requireActivity().application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        setupRecyclerView()
        setupToggleButtons()
        setupEmptyCharts()
        observeViewModel()

        binding.viewAll.setOnClickListener {
            startActivity(Intent(requireContext(), Co2AllTransactions::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = Co2TransactionAdapter(emptyList<Transaction_Info>())
        }
    }

    private fun setupToggleButtons() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val period = when (checkedId) {
                    R.id.btn_weekly -> "Weekly"
                    R.id.btn_monthly -> "Monthly"
                    R.id.btn_yearly -> "Yearly"
                    else -> "Weekly"
                }
                viewModel.setPeriod(period)
                updateToggleColors(checkedId)
            }
        }
        binding.toggleGroup.check(R.id.btn_weekly)
    }

    private fun updateToggleColors(checkedId: Int) {
        val buttons = listOf(binding.btnWeekly, binding.btnMonthly, binding.btnYearly)
        buttons.forEach { btn ->
            if (btn.id == checkedId) {
                btn.setBackgroundColor(Color.parseColor("#4CAF50"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#E8F5E9"))
                btn.setTextColor(Color.parseColor("#4CAF50"))
            }
        }
    }

    private fun setupEmptyCharts() {
        binding.lineChart.apply { description.isEnabled = false; setTouchEnabled(true); isDragEnabled = true; setScaleEnabled(true); setPinchZoom(true) }
        binding.pieChart.apply { description.isEnabled = false; isDrawHoleEnabled = true }
        binding.barChart.apply { description.isEnabled = false; setFitBars(true) }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            (binding.recyclerView.adapter as Co2TransactionAdapter).submitList(transactions)
            updateCharts(transactions)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Update UI loading state if needed
        }
    }

    private fun updateCharts(transactions: List<Transaction_Info>) {
        if (transactions.isEmpty()) {
            showEmptyCharts()
            return
        }

        val categoryTotals = transactions.groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.carbonImpact } }

        updatePieChart(categoryTotals)
        updateLineChart(transactions)
        updateBarChart(categoryTotals)
    }

    private fun showEmptyCharts() {
        binding.pieChart.data = null; binding.lineChart.data = null; binding.barChart.data = null
        binding.pieChart.invalidate(); binding.lineChart.invalidate(); binding.barChart.invalidate()
    }

    private fun updatePieChart(categoryTotals: Map<String, Double>) {
        val top6 = categoryTotals.entries.sortedByDescending { it.value }.take(6)
        val othersTotal = categoryTotals.values.sum() - top6.sumOf { it.value }
        val entries = if (othersTotal > 0.1) {
            top6.map {
                PieEntry(
                    it.value.toFloat(),
                    it.key.take(8)
                )
            } + PieEntry(othersTotal.toFloat(), "Others")
        } else {
            top6.map { PieEntry(it.value.toFloat(), it.key.take(8)) }
        }
        if (entries.isEmpty()) return
        val totalSum = categoryTotals.values.sum()
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.rgb(255, 87, 34),
                Color.rgb(76, 175, 80),
                Color.rgb(33, 150, 243),
                Color.rgb(255, 193, 7),
                Color.rgb(156, 39, 176),
                Color.rgb(244, 67, 54), Color.GRAY)
            sliceSpace = 3f; setDrawValues(true); valueTextSize = 12f; valueTextColor = Color.WHITE; valueTypeface = Typeface.DEFAULT_BOLD
            valueFormatter = object : ValueFormatter() { override fun getFormattedValue(value: Float) = "${(value / totalSum.toFloat() * 100).toInt()}%" }
        }
        binding.pieChart.apply {
            data = PieData(dataSet); centerText = "Total\n%.1f kg".format(totalSum); setCenterTextSize(16f); holeRadius = 45f; transparentCircleRadius = 52f; setDrawSliceText(false)
            legend.apply { isEnabled = true; verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM; horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER; orientation = Legend.LegendOrientation.HORIZONTAL; formSize = 12f }
            animateY(800); invalidate()
        }
    }

    private fun updateLineChart(transactions: List<Transaction_Info>) {
        val period = viewModel.period.value ?: "Weekly"
        val daysToShow = when(period) { "Weekly" -> 7; "Monthly" -> 30; "Yearly" -> 365; else -> 7 }
        val dailyData = transactions.groupBy { (it.dateAndTime / 86400000L).toInt() }
            .map { (day, txns) -> Entry(day.toFloat(), txns.sumOf { it.carbonImpact }.toFloat()) }
            .sortedBy { it.x }.takeLast(daysToShow)
        if (dailyData.isEmpty()) return
        val lineDataSet = LineDataSet(dailyData, "Daily CO₂").apply { color = Color.rgb(33, 150, 243); lineWidth = 2.5f; circleRadius = if (daysToShow <= 30) 4f else 2f; setCircleColor(
            Color.rgb(33, 150, 243)); mode = LineDataSet.Mode.CUBIC_BEZIER }
        binding.lineChart.apply {
            data = LineData(lineDataSet)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = when(period) {
                    "Weekly" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[(value % 7).toInt()]
                    "Monthly" -> "${((value % 30) + 1).toInt()}d"
                    "Yearly" -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[((value / 30.4f).toInt() % 12)]
                    else -> "Day"
                }
            }
            xAxis.position = XAxis.XAxisPosition.BOTTOM; xAxis.granularity = 1f; setVisibleXRangeMaximum(daysToShow.toFloat()); axisRight.isEnabled = false; axisLeft.axisMinimum = 0f; animateX(1000); invalidate()
        }
    }

    private fun updateBarChart(categoryTotals: Map<String, Double>) {
        val top6 = categoryTotals.entries.sortedByDescending { it.value }.take(6)
        val othersTotal = categoryTotals.values.sum() - top6.sumOf { it.value }
        val barEntries = if (othersTotal > 0.1) top6.mapIndexed { i, e ->
            BarEntry(
                i.toFloat(),
                e.value.toFloat()
            )
        } + BarEntry(6f, othersTotal.toFloat())
        else top6.mapIndexed { i, e -> BarEntry(i.toFloat(), e.value.toFloat()) }
        if (barEntries.isEmpty()) return
        val barDataSet = BarDataSet(barEntries, "Top CO₂ Categories").apply { colors = listOf(Color.rgb(255, 87, 34), Color.rgb(76, 175, 80), Color.rgb(33, 150, 243), Color.rgb(255, 193, 7), Color.rgb(156, 39, 176), Color.rgb(244, 67, 54), Color.GRAY); valueTextSize = 11f; valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = "%.1fkg".format(v) } }
        binding.barChart.apply {
            data = BarData(barDataSet).apply { barWidth = 0.7f }
            val labels = if (othersTotal > 0.1) top6.map { it.key.take(5) } + listOf("Other") else top6.map { it.key.take(5) }
            xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray()); xAxis.position = XAxis.XAxisPosition.BOTTOM; axisRight.isEnabled = false; axisLeft.axisMinimum = 0f; animateY(800); invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}