package ind.finance.aaroharth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import ind.finance.aaroharth.databinding.FragmentHomeFragementBinding
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HomeFragement : Fragment() {

    private lateinit var binding: FragmentHomeFragementBinding

    // FAB animations
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation

    private var isFabOpen = false
    private lateinit var adapter: TransactionListAdapter


    // ------------------ Fragment Lifecycle ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFragementBinding.inflate(inflater, container, false)


        setHasOptionsMenu(false)
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

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

    }

    // ------------------ Toolbar Search ------------------


    // ------------------ Toolbar Visibility ------------------

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<MaterialToolbar>(R.id.toolbar)
            .visibility = View.VISIBLE

        val dao= App_Database.getInstance(requireContext()).transactionDao()
        lifecycleScope.launch {
            val transactions=dao.getalltransaction()
           adapter.updatelist(transactions)

        }
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

        binding.addTransactionBtn.startAnimation(rotateClose)
    }

    //LIST


}
