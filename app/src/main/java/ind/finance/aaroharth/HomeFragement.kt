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

    private lateinit var binding: FragmentHomeBinding

    // FAB animations
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var fromLeft: Animation
    private lateinit var toLeft: Animation


    private var isFabOpen = false
    private lateinit var adapter: TransactionListAdapter


    // ------------------ Fragment Lifecycle ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        currentMonthAndYear()
        overviewCardsData()
        currentBalance()

        // Init animations ONCE here
        rotateOpen = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.addtransaction_rotate_open_animation
        )
        rotateClose = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.addtransaction_rotate_close_animation
        )
        fromBottom = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.addtransaction_from_bottom_animation
        )
        toBottom = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.addtransaction_to_bottom_animation
        )
        fromLeft = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_left)
        toLeft = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_left)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(hasUserName(requireContext())){
            binding.username.text="${getUserName(requireContext())}"
        }
        else{
            binding.username.text="User"
        }

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
            startActivity(
                Intent(requireContext(), AI_Activity::class.java)
            )
        }

        binding.accountManage.setOnClickListener {
            startActivity(
                Intent(requireContext(), AccountManagement::class.java)
            )
        }

        binding.tranList.layoutManager = LinearLayoutManager(requireContext())
        adapter= TransactionListAdapter(emptyList())
        binding.tranList.adapter =adapter


        binding.seeAll.setOnClickListener {
            val intent= Intent(requireContext(), TransactionList::class.java)
            intent.putExtra("category","all")
            intent.putExtra("type","all")
            startActivity(intent)

        }

        binding.budgetManage.setOnClickListener {
                startActivity(Intent(requireContext(),BudgetManagement::class.java))
        }

        binding.aiBtn.setOnClickListener {
            startActivity(Intent(requireContext(), AI_Activity::class.java))
        }

    }

    // ------------------ Toolbar Search ------------------


    // ------------------ Toolbar Visibility ------------------

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<MaterialToolbar>(R.id.toolbar)
            .visibility = View.GONE

        val dao= App_Database.getInstance(requireContext()).transactionDao()
        lifecycleScope.launch {
            val transactions=dao.getalltransaction()
           adapter.updatelist(transactions)

        }
        currentBalance()
        overviewCardsData()
        greeting()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().invalidateOptionsMenu()
    }

    // ------------------ FAB Animations ------------------

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
            visibility=View.VISIBLE
            isClickable=true
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
            visibility=View.GONE

        }


        binding.addTransactionBtn.startAnimation(rotateClose)
    }

    //LIST


    private fun currentMonthAndYear(){
        val current= LocalDate.now()
        val formatter= DateTimeFormatter.ofPattern("MMMM yyyy")
        val formatted=current.format(formatter)
        binding.thisMonthOverview.text="$formatted Overview"

    }

    private fun overviewCardsData(){

        val current=LocalDate.now()
        val startOfMonth=current.withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfMonth=current.withDayOfMonth(current.lengthOfMonth())
            .atTime(23,59,59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val format= NumberFormat.getNumberInstance(Locale("en","IN"))
        format.maximumFractionDigits=0

        lifecycleScope.launch(Dispatchers.IO) {
            val totalExpense=App_Database.getInstance(requireContext())
                .transactionDao()
                .monthWise(startOfMonth,endOfMonth,"Expense")
            val totalIncome=App_Database.getInstance(requireContext())
                .transactionDao().monthWise(startOfMonth,endOfMonth,"Income")
            withContext(Dispatchers.Main){
                val incomeBalance=format.format(totalIncome)
                binding.incomeAmount.text="\u20B9 $incomeBalance"
                val expenseBalance=format.format(totalExpense)
                binding.expenseAmount.text="\u20B9 $expenseBalance"
                if(totalExpense>totalIncome){
                    binding.thisMonthOverview.setTextColor(ContextCompat.
                    getColor(requireContext(),R.color.this_month_overview_expense))
                }
                else if(totalExpense<totalIncome){
                    binding.thisMonthOverview.setTextColor(ContextCompat.
                    getColor(requireContext(),R.color.this_month_overview_income))
                }
                else
                {
                    binding.thisMonthOverview.setTextColor(ContextCompat.
                    getColor(requireContext(),R.color.this_month_overview_neutral))
                }
            }

        }
    }

    private fun currentBalance(){
        val format= NumberFormat.getNumberInstance(Locale("en","IN"))
        format.maximumFractionDigits=0
        lifecycleScope.launch(Dispatchers.IO) {
            val currentBalance= App_Database.getInstance(requireContext())
                .accountDao().currentbalance()
            withContext(Dispatchers.Main){
                if(currentBalance>0){
                    val balance=format.format(currentBalance)
                    binding.currentBalanceAmount.text="\u20B9 $balance"
                }
                else{
                    val balance=format.format(currentBalance)
                    binding.currentBalanceAmount.text="\u20B9 $balance Deficit"
                    binding.currentBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(),R.color.red))
                }
            }

        }
    }

    private fun greeting(){
        val currentHour= LocalDateTime.now().hour
        when(currentHour){
            in 5..11->binding.greeting.text="Good Morning,"
            in 12..16->binding.greeting.text="Good Afternoon,"
            in 17..20->binding.greeting.text="Good Evening,"
            else->binding.greeting.text="Good Night,"
        }

    }

       fun getUserName(context: Context): String? =
           context.getSharedPreferences("app_prefs", MODE_PRIVATE)
               .getString("username", null)

       fun hasUserName(context: Context): Boolean =
           context.getSharedPreferences("app_prefs", MODE_PRIVATE)
               .contains("username")


}



