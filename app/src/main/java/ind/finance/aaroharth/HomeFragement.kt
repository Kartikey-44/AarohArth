package ind.finance.aaroharth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import ind.finance.aaroharth.databinding.ActivityAiBinding
import ind.finance.aaroharth.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.jvm.java


class HomeFragement : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // FAB animations
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var fromLeft: Animation
    private lateinit var toLeft: Animation

    private var isFabOpen = false
    private lateinit var adapter: TransactionListAdapter

    // ------------------ Lifecycle ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Insets applied ONCE per view lifecycle
        val initialTopPadding = view.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(
                v.paddingLeft,
                initialTopPadding + topInset,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        initAnimations()
        initRecyclerView()
        initClickListeners()
        initStaticUi()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ------------------ Init blocks ------------------

    private fun initAnimations() {
        rotateOpen = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_open_animation)
        rotateClose = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_close_animation)
        fromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_from_bottom_animation)
        toBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_to_bottom_animation)
        fromLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.from_left)
        toLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.to_left)
    }

    private fun initRecyclerView() {
        adapter = TransactionListAdapter(emptyList())
        binding.tranList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragement.adapter
        }
    }

    private fun initClickListeners() {

        binding.addTransactionBtn.setOnClickListener {
            if (isFabOpen) closeFab() else openFab()
        }

        binding.incomeBtn.setOnClickListener {
            closeFab()
            startActivity(
                Intent(requireContext(), Transaction_Action_Page::class.java)
                    .putExtra("type", "income")
            )
        }

        binding.expenseBtn.setOnClickListener {
            closeFab()
            startActivity(
                Intent(requireContext(), Transaction_Action_Page::class.java)
                    .putExtra("type", "expense")
            )
        }

        binding.aiBtn.setOnClickListener {
            closeFab()
            startActivity(Intent(requireContext(), AI_Activity::class.java))
        }

        binding.accountManage.setOnClickListener {
            startActivity(Intent(requireContext(), AccountManagement::class.java))
        }

        binding.budgetManage.setOnClickListener {
            startActivity(Intent(requireContext(), BudgetManagement::class.java))
        }

        binding.seeAll.setOnClickListener {
            startActivity(
                Intent(requireContext(), TransactionList::class.java)
                    .putExtra("category", "all")
                    .putExtra("type", "all")
            )
        }
    }

    private fun initStaticUi() {
        binding.username.text =
            getUserName(requireContext()) ?: "User"

        currentMonthAndYear()
        greeting()
    }

    // ------------------ Data refresh ------------------

    private fun refreshData() {
        currentBalance()
        overviewCardsData()

        val dao = App_Database.getInstance(requireContext()).transactionDao()
        lifecycleScope.launch {
            val transactions = dao.getalltransaction()
            adapter.updatelist(transactions)
        }
    }

    // ------------------ FAB helpers ------------------

    private fun openFab() {
        isFabOpen = true

        binding.incomeBtn.apply {
            visibility = View.VISIBLE
            isClickable = true
            startAnimation(fromBottom)
        }

        binding.expenseBtn.apply {
            visibility = View.VISIBLE
            isClickable = true
            startAnimation(fromBottom)
        }

        binding.aiBtn.apply {
            visibility = View.VISIBLE
            isClickable = true
            startAnimation(fromLeft)
        }

        binding.addTransactionBtn.startAnimation(rotateOpen)
    }

    private fun closeFab() {
        isFabOpen = false

        binding.incomeBtn.apply {
            startAnimation(toBottom)
            isClickable = false
            visibility = View.GONE
        }

        binding.expenseBtn.apply {
            startAnimation(toBottom)
            isClickable = false
            visibility = View.GONE
        }

        binding.aiBtn.apply {
            startAnimation(toLeft)
            isClickable = false
            visibility = View.GONE
        }

        binding.addTransactionBtn.startAnimation(rotateClose)
    }

    // ------------------ UI helpers ------------------

    private fun currentMonthAndYear() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        binding.thisMonthOverview.text =
            "${LocalDate.now().format(formatter)} Overview"
    }

    private fun greeting() {
        val hour = LocalDateTime.now().hour
        binding.greeting.text = when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
    }

    private fun currentBalance() {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val balance =
                App_Database.getInstance(requireContext()).accountDao().currentbalance()
            withContext(Dispatchers.Main) {
                val text = "\u20B9 ${format.format(balance)}"
                binding.currentBalanceAmount.text =
                    if (balance >= 0) text else "$text Deficit"
            }
        }
    }

    private fun overviewCardsData() {
        val now = LocalDate.now()
        val start = now.withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.withDayOfMonth(now.lengthOfMonth())
            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val format = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = App_Database.getInstance(requireContext()).transactionDao()
            val expense = dao.monthWise(start, end, "Expense")
            val income = dao.monthWise(start, end, "Income")

            withContext(Dispatchers.Main) {
                binding.incomeAmount.text = "\u20B9 ${format.format(income)}"
                binding.expenseAmount.text = "\u20B9 ${format.format(expense)}"
            }
        }
    }

    // ------------------ Pref helpers ------------------

    private fun getUserName(context: Context): String? =
        context.getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("username", null)
}




