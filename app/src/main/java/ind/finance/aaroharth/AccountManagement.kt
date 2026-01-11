package ind.finance.aaroharth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.databinding.ActivityAccountManagementBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AccountManagement : AppCompatActivity() {

    private lateinit var binding: ActivityAccountManagementBinding
    private lateinit var adapter: YourAccountListAdapter
    private lateinit var dao: Account_Dao

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        dao = App_Database.getInstance(this).accountDao()

        setupRecycler()
        setupSearch()
        setupActions()
    }

    private fun setupRecycler() {
        adapter = YourAccountListAdapter(emptyList())
        binding.yourAccountsList.layoutManager = LinearLayoutManager(this)
        binding.yourAccountsList.adapter = adapter
    }

    private fun setupActions() {
        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, AccountActions::class.java))
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility= View.GONE
            binding.titleText.visibility=View.GONE
            binding.searchInput.visibility=View.VISIBLE
            binding.back.visibility=View.VISIBLE
            binding.searchInput.requestFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.back.setOnClickListener {
            binding.searchIcon.visibility = View.VISIBLE
            binding.titleText.visibility = View.VISIBLE
            binding.searchInput.visibility = View.GONE
            binding.back.visibility = View.GONE

            binding.searchInput.clearFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)

            lifecycleScope.launch {
                adapter.updatelist(dao.getAllAccounts())
            }
        }
    }

    // ================= SEARCH =================

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // debounce

                    val result = if (query.isBlank()) {
                        dao.getAllAccounts()
                    } else {
                        dao.searchAccounts(query)
                    }

                    adapter.updatelist(result)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ================= DATA =================

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            adapter.updatelist(dao.getAllAccounts())
        }
    }
}
