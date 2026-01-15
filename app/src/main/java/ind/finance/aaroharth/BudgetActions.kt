package ind.finance.aaroharth

import android.app.Dialog
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.ActivityBudgetActionsBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetActions : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetActionsBinding
    private lateinit var dialogBinding: DialogScreenBinding

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

        binding.saveBtn.setOnClickListener {
            if (validateInput()) {
                saveBudget()
            }
        }
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.S){
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bg.setRenderEffect(blur)
        }
    }


    /* ---------------- CATEGORY DROPDOWN (FIXED) ---------------- */

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categoryList
        )

        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0

            // Turn AutoCompleteTextView into selector
            keyListener = null
            isFocusable = false
            isCursorVisible = false

            setOnClickListener {
                showDropDown()
            }

            setOnItemClickListener { parent, _, position, _ ->
                setText(parent.getItemAtPosition(position).toString(), false)
            }
        }
    }

    /* ---------------- VALIDATION ---------------- */

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

    /* ---------------- SAVE BUDGET ---------------- */

    private fun saveBudget() {
        val dao = App_Database.getInstance(this).budgetDao()

        val category = binding.categoryField.text.toString().trim()
        val amount = binding.amountField.text.toString().toLong()
        val now = System.currentTimeMillis()

        val monthKey = SimpleDateFormat(
            "yyyy-MM",
            Locale.US
        ).format(Date(now))

        val budget = Budget_Info(
            id = 0,
            category = category,
            amount = amount,
            date = now,
            monthKey = monthKey
        )

        lifecycleScope.launch {
            dao.insertInfo(budget)
            successDialog("Budget Saved")
        }
    }

    /* ---------------- SUCCESS DIALOG ---------------- */

    private fun successDialog(message: String) {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialogBinding.dialogLottie.setAnimation("Success.json")
        dialogBinding.message.text = message

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1800)
    }
}
