package ind.finance.aaroharth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.databinding.ActivityAccountManagementBinding
import kotlinx.coroutines.launch

class AccountManagement : AppCompatActivity() {
    private lateinit var binding: ActivityAccountManagementBinding
    private lateinit var adapter: YourAccountListAdapter
    private lateinit var dao: Account_Dao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAccountManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addBtn.setOnClickListener {
            startActivity(Intent(applicationContext, AccountActions::class.java))
        }

        binding.yourAccountsList.layoutManager= LinearLayoutManager(this)
        adapter= YourAccountListAdapter(emptyList())
        binding.yourAccountsList.adapter=adapter

        //dao
        dao= App_Database.getInstance(this).accountDao()

    }
    override fun onResume(){
        super.onResume()
        lifecycleScope.launch {
            val accounts=dao.getAllAccounts()
            adapter.updatelist(accounts)
        }
    }
}