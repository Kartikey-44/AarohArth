package ind.finance.aaroharth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.adapters.TransactionListAdapter
import ind.finance.aaroharth.add_delete_edit_Fragments.Transaction_Action_Page
import ind.finance.aaroharth.databinding.FragmentHomeBinding
import ind.finance.aaroharth.managementFragments.AccountManagement
import ind.finance.aaroharth.managementFragments.BudgetManagement
import ind.finance.aaroharth.viewmodels.HomeViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragement : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    // FAB animations
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var fromLeft: Animation
    private lateinit var toLeft: Animation

    private var isFabOpen = false
    private lateinit var adapter: TransactionListAdapter

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
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

        binding.notification.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationHistoryFragment())
                .addToBackStack("notification_history")
                .commit()
        }
    }

    private fun initStaticUi() {
        binding.username.text = getUserName(requireContext()) ?: "User"
        currentMonthAndYear()
        greeting()
    }

    private fun observeViewModel() {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updatelist(transactions)
        }

        viewModel.currentBalance.observe(viewLifecycleOwner) { balance ->
            val text = "\u20B9 ${format.format(balance)}"
            binding.currentBalanceAmount.text = if (balance >= 0) text else "$text Deficit"
        }

        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.incomeAmount.text = "\u20B9 ${format.format(income)}"
        }

        viewModel.monthlyExpense.observe(viewLifecycleOwner) { expense ->
            binding.expenseAmount.text = "\u20B9 ${format.format(expense)}"
        }
    }

    private fun openFab() {
        isFabOpen = true
        binding.incomeBtn.apply { visibility = View.VISIBLE; isClickable = true; startAnimation(fromBottom) }
        binding.expenseBtn.apply { visibility = View.VISIBLE; isClickable = true; startAnimation(fromBottom) }
        binding.aiBtn.apply { visibility = View.VISIBLE; isClickable = true; startAnimation(fromLeft) }
        binding.addTransactionBtn.startAnimation(rotateOpen)
    }

    private fun closeFab() {
        isFabOpen = false
        binding.incomeBtn.apply { startAnimation(toBottom); isClickable = false; visibility = View.GONE }
        binding.expenseBtn.apply { startAnimation(toBottom); isClickable = false; visibility = View.GONE }
        binding.aiBtn.apply { startAnimation(toLeft); isClickable = false; visibility = View.GONE }
        binding.addTransactionBtn.startAnimation(rotateClose)
    }

    private fun currentMonthAndYear() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        binding.thisMonthOverview.text = "${LocalDate.now().format(formatter)} Overview"
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

    private fun getUserName(context: Context): String? =
        context.getSharedPreferences("app_prefs", MODE_PRIVATE).getString("username", null)
}
