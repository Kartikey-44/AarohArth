package ind.finance.aaroharth
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ind.finance.aaroharth.databinding.FragmentCategoriesBinding
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import android.view.inputmethod.InputMethodManager
import android.text.TextWatcher


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class CategoriesFragment : Fragment() {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoriesList: ArrayList<CategoriesDataClass>
    private lateinit var fullList: ArrayList<CategoriesDataClass>
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var showingIncome: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.titleText.visibility = View.GONE
            binding.searchInput.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchInput.requestFocus()

            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }

        // Back button
        binding.back.setOnClickListener {
            binding.searchIcon.visibility = View.VISIBLE
            binding.titleText.visibility = View.VISIBLE
            binding.searchInput.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchInput.text?.clear()
            filterCategories("")  // Reset list

            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
        }

        binding.btnIncome.setOnClickListener {
            income()
        }
        binding.btnExpense.setOnClickListener {
            expense()
        }

        binding.btnIncome.apply {
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            setTextColor(Color.parseColor("#4CAF50"))
        }
        binding.btnExpense.apply {
            setBackgroundColor(Color.parseColor("#FFEBEE"))
            setTextColor(Color.parseColor("#D32F2F"))
        }

        init()
    }

    private fun init(){
        val recyclerView = binding.categoriesRecyclerView
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        categoriesList = ArrayList()
        fullList = ArrayList()
        addDataToCategoriesList(fullList)
        categoriesList.addAll(fullList)
        categoriesAdapter = CategoriesAdapter(categoriesList)
        categoriesAdapter.updateMode(null)
        recyclerView.adapter = categoriesAdapter

        categoriesAdapter.onItemClick = { category ->
            val toggleState = when (showingIncome) {
                true -> "Income"
                false -> "Expense"
                null -> "ALL"
            }

            val intent = Intent(requireContext(), CategoriesTransactionList::class.java).apply {
                putExtra("CATEGORY_NAME", category.name)
                putExtra("TRANSACTION_TYPE", toggleState)
            }
            startActivity(intent)
        }
        setupSearch()
    }

    private fun addDataToCategoriesList(list: ArrayList<CategoriesDataClass>) {
        list.add(CategoriesDataClass(R.drawable.rupee , "Salary"))
        list.add(CategoriesDataClass(R.drawable.freelance, "Freelance"))
        list.add(CategoriesDataClass(R.drawable.business , "Business"))
        list.add(CategoriesDataClass(R.drawable.rental , "Rental"))
        list.add(CategoriesDataClass(R.drawable.upiicon , "Upi"))
        list.add(CategoriesDataClass(R.drawable.investment , "Investment"))
        list.add(CategoriesDataClass(R.drawable.housing , "Housing"))
        list.add(CategoriesDataClass(R.drawable.utilities , "Recharge"))
        list.add(CategoriesDataClass(R.drawable.food , "Food"))
        list.add(CategoriesDataClass(R.drawable.entertainment , "Entertainment"))
        list.add(CategoriesDataClass(R.drawable.shopping , "Shopping"))
        list.add(CategoriesDataClass(R.drawable.tax , "Taxes"))
        list.add(CategoriesDataClass(R.drawable.medical , "Medical"))
        list.add(CategoriesDataClass(R.drawable.education , "Education"))
        list.add(CategoriesDataClass(R.drawable.insurance , "Insurance"))
        list.add(CategoriesDataClass(R.drawable.savings , "Savings"))
        list.add(CategoriesDataClass(R.drawable.taxi , "Taxi"))
        list.add(CategoriesDataClass(R.drawable.auto , "Auto"))
        list.add(CategoriesDataClass(R.drawable.hotel , "Hotel"))
        list.add(CategoriesDataClass(R.drawable.flight , "Flight"))
        list.add(CategoriesDataClass(R.drawable.petrol , "Petrol"))
        list.add(CategoriesDataClass(R.drawable.diesel , "Diesel"))
        list.add(CategoriesDataClass(R.drawable.cng , "CNG"))
        list.add(CategoriesDataClass(R.drawable.lpgpng , "LPG"))
        list.add(CategoriesDataClass(R.drawable.publictransport , "Public Transport"))
        list.add(CategoriesDataClass(R.drawable.electricity , "Electricity"))
        list.add(CategoriesDataClass(R.drawable.waterbill , "Water Bill"))
        list.add(CategoriesDataClass(R.drawable.dining_out , "Dining Out"))
        list.add(CategoriesDataClass(R.drawable.personalcare , "Personal Care"))
        list.add(CategoriesDataClass(R.drawable.subscription , "Subscriptions"))
        list.add(CategoriesDataClass(R.drawable.gift , "Gifts"))
        list.add(CategoriesDataClass(R.drawable.donation , "Donations"))
        list.add(CategoriesDataClass(R.drawable.miscellaneous , "Miscellaneous"))
        list.add(CategoriesDataClass(R.drawable.other , "Other"))
        list.add(CategoriesDataClass(R.drawable.recharge , "Mobile Recharge"))
        list.add(CategoriesDataClass(R.drawable.fastag , "FastTag Recharge"))
        list.add(CategoriesDataClass(R.drawable.loan , "EMI / Loans"))
        list.add(CategoriesDataClass(R.drawable.decoration , "Decoration"))
        list.add(CategoriesDataClass(R.drawable.grocery , "Grocery"))
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search categories"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterCategories(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterCategories(newText)
                return true
            }
        })
        // When close arrow pressed -> reset full list
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                filterCategories("")
                return true
            }
        })
    }

    private fun filterCategories(text: String?) {
        val query = text.orEmpty().lowercase().trim()

        categoriesList.clear()

        if (query.isEmpty()) {
            categoriesList.addAll(fullList)
        } else {
            val filtered = fullList
                .filter { it.name.lowercase().startsWith(query) }
                .sortedBy { it.name.lowercase() }

            categoriesList.addAll(filtered)
        }
        val mode = when (showingIncome) {
            true -> true
            false -> false
            null -> null
        }
        categoriesAdapter.updateMode(mode)
    }

    private fun income() {
        showingIncome = true
        categoriesAdapter.updateMode(true)


        binding.btnIncome.apply {
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
        }

        binding.btnExpense.apply {
            setBackgroundColor(Color.parseColor("#FFEBEE"))
            setTextColor(Color.parseColor("#D32F2F"))
        }
    }

    private fun expense() {
        showingIncome = false
        categoriesAdapter.updateMode(false)


        binding.btnExpense.apply {
            setBackgroundColor(Color.parseColor("#D32F2F"))
            setTextColor(Color.WHITE)
        }

        binding.btnIncome.apply {
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            setTextColor(Color.parseColor("#4CAF50"))
        }
    }


    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                filterCategories(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CategoriesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoriesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}


