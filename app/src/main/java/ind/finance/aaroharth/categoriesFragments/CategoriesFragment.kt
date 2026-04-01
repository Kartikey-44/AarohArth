package ind.finance.aaroharth.categoriesFragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import ind.finance.aaroharth.categoriesFragments.CategoriesTransactionList
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.adapters.CategoriesAdapter
import ind.finance.aaroharth.data.model.CategoriesDataClass
import ind.finance.aaroharth.databinding.FragmentCategoriesBinding
import ind.finance.aaroharth.viewmodels.CategoriesViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels {
        val app = requireActivity().application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }

    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, topInset, view.paddingRight, view.paddingBottom)
            insets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        categoriesAdapter = CategoriesAdapter(arrayListOf())
        binding.categoriesRecyclerView.adapter = categoriesAdapter

        categoriesAdapter.onItemClick = { category ->
            val toggleState = when (viewModel.showingIncome.value) {
                true -> "Income"
                false -> "Expense"
                else -> "ALL"
            }
            val intent = Intent(requireContext(), CategoriesTransactionList::class.java).apply {
                putExtra("CATEGORY_NAME", category.name)
                putExtra("TRANSACTION_TYPE", toggleState)
            }
            startActivity(intent)
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.titleText.visibility = View.GONE
            binding.searchInput.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchInput.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.back.setOnClickListener {
            binding.searchIcon.visibility = View.VISIBLE
            binding.titleText.visibility = View.VISIBLE
            binding.searchInput.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchInput.text?.clear()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterCategories(s?.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnIncome.setOnClickListener { viewModel.setIncomeMode() }
        binding.btnExpense.setOnClickListener { viewModel.setExpenseMode() }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categoriesAdapter.updateList(list)
        }

        viewModel.showingIncome.observe(viewLifecycleOwner) { showingIncome ->
            categoriesAdapter.updateMode(showingIncome)
            updateButtonUI(showingIncome)
        }
    }

    private fun updateButtonUI(showingIncome: Boolean?) {
        when (showingIncome) {
            true -> {
                binding.btnIncome.setBackgroundColor(Color.parseColor("#4CAF50"))
                binding.btnIncome.setTextColor(Color.WHITE)
                binding.btnExpense.setBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.btnExpense.setTextColor(Color.parseColor("#D32F2F"))
            }
            false -> {
                binding.btnExpense.setBackgroundColor(Color.parseColor("#D32F2F"))
                binding.btnExpense.setTextColor(Color.WHITE)
                binding.btnIncome.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.btnIncome.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                binding.btnIncome.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.btnIncome.setTextColor(Color.parseColor("#4CAF50"))
                binding.btnExpense.setBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.btnExpense.setTextColor(Color.parseColor("#D32F2F"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}