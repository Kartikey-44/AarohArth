package ind.finance.aaroharth
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ind.finance.aaroharth.databinding.FragmentCategoriesBinding
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView



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

        binding.btnIncome.setOnClickListener {
            Toast.makeText(requireContext(), "Income is Selected", Toast.LENGTH_SHORT).show()
        }
        binding.btnExpense.setOnClickListener {
            Toast.makeText(requireContext(), "Expense is Selected", Toast.LENGTH_SHORT).show()
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
        recyclerView.adapter = categoriesAdapter

    }

    private fun addDataToCategoriesList(list: ArrayList<CategoriesDataClass>) {
        list.add(CategoriesDataClass(R.drawable.rupee , "Salary"))
        list.add(CategoriesDataClass(R.drawable.freelance, "Freelance"))
        list.add(CategoriesDataClass(R.drawable.business , "Business"))
        list.add(CategoriesDataClass(R.drawable.rental , "Rental"))
        list.add(CategoriesDataClass(R.drawable.upi , "Upi"))
        list.add(CategoriesDataClass(R.drawable.investment , "Investment"))
        list.add(CategoriesDataClass(R.drawable.housing , "Housing"))
        list.add(CategoriesDataClass(R.drawable.utilities , "Recharge"))
        list.add(CategoriesDataClass(R.drawable.food , "Food"))
        list.add(CategoriesDataClass(R.drawable.transportation , "Transportation"))
        list.add(CategoriesDataClass(R.drawable.entertainment , "Entertainment"))
        list.add(CategoriesDataClass(R.drawable.shopping , "Shopping"))
        list.add(CategoriesDataClass(R.drawable.travel , "Travel"))
        list.add(CategoriesDataClass(R.drawable.tax , "Taxes"))
        list.add(CategoriesDataClass(R.drawable.medical , "Medical"))
        list.add(CategoriesDataClass(R.drawable.education , "Education"))
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
                filterCategories(newText) // live search
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

        categoriesAdapter.notifyDataSetChanged()
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


