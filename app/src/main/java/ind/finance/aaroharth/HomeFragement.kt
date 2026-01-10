package ind.finance.aaroharth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import ind.finance.aaroharth.databinding.FragmentHomeFragementBinding

class HomeFragement : Fragment() {

    private lateinit var binding: FragmentHomeFragementBinding
    private var isFabOpen = false

    // ------------------ Fragment Lifecycle ------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFragementBinding.inflate(inflater, container, false)
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
    }

    // ------------------ Toolbar Search ------------------

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as? SearchView
            ?: return

        val black = ContextCompat.getColor(requireContext(), android.R.color.black)

        searchView.queryHint = "Search transactions"

        // Search text
        val searchEditText =
            searchView.findViewById<EditText>(
                androidx.appcompat.R.id.search_src_text
            )

        searchEditText.setTextColor(black)
        searchEditText.setHintTextColor(black)

        // Remove underline
        searchView.findViewById<View>(
            androidx.appcompat.R.id.search_plate
        )?.setBackgroundColor(Color.TRANSPARENT)

        // Search icon
        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )?.setColorFilter(black)

        // Close (X) icon
        searchView.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )?.setColorFilter(black)

        // Toolbar title handling
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val titleView = toolbar.findViewById<View>(R.id.toolbarTitle)

        menu.findItem(R.id.action_search)
            .setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    titleView.visibility = View.GONE
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().invalidateOptionsMenu()
    }

    // ------------------ FAB Animations ------------------

    private fun openFab() {
        isFabOpen = true
        binding.incomeBtn.visibility = View.VISIBLE
        binding.expenseBtn.visibility = View.VISIBLE
    }

    private fun closeFab() {
        isFabOpen = false
        binding.incomeBtn.visibility = View.GONE
        binding.expenseBtn.visibility = View.GONE
    }
}
