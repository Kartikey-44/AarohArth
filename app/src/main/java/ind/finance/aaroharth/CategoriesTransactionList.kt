package ind.finance.aaroharth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.aaroharth.databinding.ActivityCategoriestransactionListBinding

class CategoriesTransactionList : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriestransactionListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriestransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Unknown"
        val transactionType = intent.getStringExtra("TRANSACTION_TYPE") ?: "ALL"
        binding.categoriesName.text = "$categoryName ($transactionType)"
        binding.categoriesName.text = "$categoryName ($transactionType)"

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}