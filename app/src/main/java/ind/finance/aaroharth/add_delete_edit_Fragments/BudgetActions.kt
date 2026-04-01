package ind.finance.aaroharth.add_delete_edit_Fragments

import android.R
import android.app.Dialog
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.databinding.ActivityBudgetActionsBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.BudgetViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetActions : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetActionsBinding
    private lateinit var dialogBinding: DialogScreenBinding
    private val viewModel: BudgetViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }

    private val categoryList = listOf(
        "Mobile Recharge", "FastTag Recharge", "Food", "Dining Out", "Shopping",
        "Personal Care", "Entertainment", "Subscriptions", "Housing", "Utilities",
        "Public Transport", "Petrol", "Diesel", "CNG", "Electricity",
        "Medical", "Insurance", "Education", "EMI / Loans", "Tax",
        "Gifts", "Donations", "Grocery", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBudgetActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryDropdown()
        observeViewModel()

        binding.saveBtn.setOnClickListener {
            if (validateInput()) {
                val category = binding.categoryField.text.toString().trim()
                val amount = binding.amountField.text.toString().toLong()
                val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                viewModel.addBudget(category, amount, monthKey)
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bg.setRenderEffect(blur)
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, categoryList)
        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0
            keyListener = null
            isFocusable = false
            isCursorVisible = false
            setOnClickListener { showDropDown() }
            setOnItemClickListener { parent, _, position, _ ->
                setText(parent.getItemAtPosition(position).toString(), false)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.operationSuccess.observe(this) { message ->
            successDialog(message)
        }
    }

    private fun validateInput(): Boolean {
        binding.categoryLayout.error = null
        binding.amountLayout.error = null

        val category = binding.categoryField.text.toString().trim()
        val amountText = binding.amountField.text.toString().trim()

        if (category.isEmpty()) {
            binding.categoryLayout.error = "Select category"
            return false
        }
        if (category !in categoryList) {
            binding.categoryLayout.error = "Select from list"
            return false
        }
        val amount = amountText.toLongOrNull()
        if (amount == null || amount <= 0) {
            binding.amountLayout.error = "Enter valid amount"
            return false
        }
        return true
    }

    private fun successDialog(message: String) {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialogBinding.dialogLottie.setAnimation("Success.json")
        dialogBinding.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1800)
    }
}