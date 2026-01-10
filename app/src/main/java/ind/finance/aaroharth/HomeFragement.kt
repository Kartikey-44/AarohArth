package ind.finance.aaroharth
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import ind.finance.aaroharth.databinding.FragmentHomeFragementBinding

class HomeFragement : Fragment() {

    private lateinit var binding: FragmentHomeFragementBinding

    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private var isFabOpen = false

    // ------------------ Fragment Lifecycle ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFragementBinding.inflate(inflater, container, false)
        rotateOpen = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_open_animation)
        rotateClose = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_close_animation)
        fromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_from_bottom_animation)
        toBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_to_bottom_animation)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tell Fragment it has a menu (SearchView lives here)
        setHasOptionsMenu(true)

        // Main FAB
        binding.addTransactionBtn.setOnClickListener {
            if (isFabOpen) closeFab() else openFab()
        }

        // Income FAB
        binding.incomeBtn.setOnClickListener {
            closeFab()
            startActivity(
                Intent(requireContext(), Transaction_Action_Page::class.java)
                    .putExtra("type", "income")
            )
        }

        // Expense FAB
        binding.expenseBtn.setOnClickListener {
            closeFab()
            startActivity(
                Intent(requireContext(), Transaction_Action_Page::class.java)
                    .putExtra("type", "expense")
            )
        }
    }

    // ------------------ Toolbar Search ------------------

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        // Force black color everywhere
        val black = ContextCompat.getColor(requireContext(), android.R.color.black)

        // Search hint text
        searchView.queryHint = "Search transactions"

        // Search text + hint color
        val searchEditText = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(black)
        searchEditText.setHintTextColor(black)

        // Remove default underline
        searchView.findViewById<View>(
            androidx.appcompat.R.id.search_plate
        )?.setBackgroundColor(Color.TRANSPARENT)

        // Search icon (magnifier)
        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )?.setColorFilter(black)

        // Close (X) icon
        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )?.setColorFilter(black)

        // Handle toolbar title hide/show
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val titleView = toolbar.findViewById<View>(R.id.toolbarTitle)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Hide app name
                titleView.visibility = View.GONE

                // IMPORTANT:
                // Replace back arrow drawable instead of tinting
                searchView.setOnSearchClickListener {
                    val backIcon = searchView.findViewById<ImageView>(
                        androidx.appcompat.R.id.search_mag_icon
                    )
                    backIcon?.setImageResource(R.drawable.back_arrow)
                }

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Restore app name
                titleView.visibility = View.VISIBLE
                return true
            }
        })

        // Listen to search text
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle submit
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle live search
                return true
            }
        })
    }

    // ------------------ Toolbar Visibility ------------------

    override fun onResume() {
        super.onResume()
        // Show toolbar only on HomeFragment
        requireActivity()
            .findViewById<MaterialToolbar>(R.id.toolbar)
            .visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent menu leaking to other fragments
        requireActivity().invalidateOptionsMenu()
    }

    // ------------------ FAB Animations ------------------

    private fun openFab() {
        isFabOpen = true

        // SHOW + ANIMATE INCOME FAB
        binding.incomeBtn.visibility = View.VISIBLE
        binding.incomeBtn.isClickable = true
        binding.incomeBtn.startAnimation(fromBottom)

        // SHOW + ANIMATE EXPENSE FAB
        binding.expenseBtn.visibility = View.VISIBLE
        binding.expenseBtn.isClickable = true
        binding.expenseBtn.startAnimation(fromBottom)

        // ROTATE MAIN FAB
        binding.addTransactionBtn.startAnimation(rotateOpen)
    }

    private fun closeFab() {
        isFabOpen = false

        // HIDE INCOME FAB
        binding.incomeBtn.startAnimation(toBottom)
        binding.incomeBtn.isClickable = false
        binding.incomeBtn.visibility = View.GONE

        // HIDE EXPENSE FAB
        binding.expenseBtn.startAnimation(toBottom)
        binding.expenseBtn.isClickable = false
        binding.expenseBtn.visibility = View.GONE

        // ROTATE MAIN FAB BACK
        binding.addTransactionBtn.startAnimation(rotateClose)
    }
}
