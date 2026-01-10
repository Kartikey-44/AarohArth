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

        binding.accountManagement.setOnClickListener {
            startActivity(
                Intent(requireContext(), AccountManagement::class.java)
            )
        }

        binding.tranList.layoutManager = LinearLayoutManager(requireContext())
        adapter= TransactionListAdapter(emptyList())
        binding.tranList.adapter =adapter

    }

    // ------------------ Toolbar Search ------------------

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView ?: return

        val black = ContextCompat.getColor(requireContext(), android.R.color.black)

        searchView.queryHint = "Search transactions"

        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(black)
        searchEditText.setHintTextColor(black)

        searchView.findViewById<View>(
            androidx.appcompat.R.id.search_plate
        )?.setBackgroundColor(Color.TRANSPARENT)

        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )?.setColorFilter(black)

        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )?.setColorFilter(black)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val titleView = toolbar.findViewById<View>(R.id.toolbarTitle)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                titleView.visibility = View.GONE

                // Replace search icon with back arrow
                searchView.setOnSearchClickListener {
                    searchView.findViewById<ImageView>(
                        androidx.appcompat.R.id.search_mag_icon
                    )?.setImageResource(R.drawable.back_arrow)
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                titleView.visibility = View.VISIBLE
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean = true
        })
    }

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
