package ind.finance.aaroharth.managementFragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.add_delete_edit_Fragments.AccountActions
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.adapters.YourAccountListAdapter
import ind.finance.aaroharth.databinding.ActivityAccountManagementBinding
import ind.finance.aaroharth.viewmodels.AccountViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class AccountManagement : AppCompatActivity() {

    private lateinit var binding: ActivityAccountManagementBinding
    private lateinit var adapter: YourAccountListAdapter
    private val viewModel: AccountViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupRecycler()
        setupSearch()
        setupActions()
        observeViewModel()
    }

    private fun setupRecycler() {
        adapter = YourAccountListAdapter(emptyList())
        binding.yourAccountsList.layoutManager = LinearLayoutManager(this)
        binding.yourAccountsList.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(this) { accounts ->
            adapter.updatelist(accounts)
        }
    }

    private fun setupActions() {
        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, AccountActions::class.java))
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.titleText.visibility = View.GONE
            binding.searchInput.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchInput.requestFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.back.setOnClickListener {
            binding.searchIcon.visibility = View.VISIBLE
            binding.titleText.visibility = View.VISIBLE
            binding.searchInput.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchInput.text?.clear()
            binding.searchInput.clearFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)

            viewModel.loadAccounts()
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                if (query.isBlank()) {
                    viewModel.loadAccounts()
                } else {
                    viewModel.searchAccounts(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccounts()
    }
}