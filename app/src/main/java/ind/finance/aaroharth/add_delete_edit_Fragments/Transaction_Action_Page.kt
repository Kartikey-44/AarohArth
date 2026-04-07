package ind.finance.aaroharth.add_delete_edit_Fragments

import android.R
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.databinding.ActivityTransactionActionPageBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.TransactionActionViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Transaction_Action_Page : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionActionPageBinding
    private lateinit var dialogBinding: DialogScreenBinding
    private val viewModel: TransactionActionViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    private val categoryList = listOf(
        "Mobile Recharge", "FastTag Recharge", "Salary", "Business", "Freelance", "Investment", "Savings", "Food",
        "Dining Out", "Shopping", "Personal Care", "Entertainment", "Subscriptions", "Housing", "Rental", "Utilities",
        "Public Transport", "Petrol", "Diesel", "CNG", "Electricity", "LPG", "PNG", "Taxi", "Auto", "Hotel",
        "Flight", "Medical", "Insurance", "Education", "EMI / Loans", "Tax", "Gifts", "Donations",
        "Miscellaneous", "Water Bill","Grocery","Other"
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionActionPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        viewModel.loadAccountTypes()
        catchAssistantIntent()
    }

    private fun setupUI() {
        binding.amountLayout.setOnClickListener { binding.amountLayout.error = null }
        binding.dateLayout.isEnabled = false
        binding.dateField.isEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.background.setRenderEffect(blur)
        }

        val type = intent.getStringExtra("type") ?: "expense"
        if (type == "income") incomeUiChanges() else expenseUiChanges()

        val time = System.currentTimeMillis()
        val displayTime = Instant.ofEpochMilli(time)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a  dd/MM/yyyy"))
        binding.dateField.setText(displayTime)

        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                val amountText = binding.amountField.text.toString().trim()
                val amount = amountText.toLongOrNull() ?: 0L
                viewModel.saveTransaction(
                    type = type,
                    amount = amount,
                    otherParty = binding.otherPartyField.text.toString().trim(),
                    category = binding.categoryField.text.toString().trim(),
                    accountName = binding.transactionWayField.text.toString().trim(),
                    accountType = binding.transactionMediumField.text.toString().trim(),
                    remark = binding.remarkField.text.toString().trim()
                )
            }
        }

        setupCategoryDropdown()

        binding.transactionMediumField.setOnItemClickListener { parent, _, position, _ ->
            val selectedType = parent.getItemAtPosition(position).toString()
            binding.transactionMediumField.setText(selectedType, false)
            viewModel.loadAccountNames(selectedType)
            binding.transactionWayField.text.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.accountTypes.observe(this) { types ->
            val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, types)
            binding.transactionMediumField.setAdapter(adapter)
        }

        viewModel.accountNames.observe(this) { names ->
            val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, names)
            binding.transactionWayField.setAdapter(adapter)
        }

        viewModel.saveStatus.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                successLottie(message)
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg == "Insufficient Balance") {
                binding.amountLayout.error = errorMsg
            } else {
                dialog("Failed.json", errorMsg)
            }
        }
    }

    private fun incomeUiChanges() {
        binding.subHeading.background = getDrawable(ind.finance.aaroharth.R.drawable.transaction_page_sub_heading_income)
        binding.subHeading.text = "Income"
        binding.subHeading.setTextColor(getColor(ind.finance.aaroharth.R.color.subheading_color_income))
        binding.amountLayout.setStartIconDrawable(getDrawable(ind.finance.aaroharth.R.drawable.rupee_income))
        binding.amountLayout.setStartIconTintList(ColorStateList.valueOf(getColor(ind.finance.aaroharth.R.color.rupee_symbol_income)))
        binding.otherPartyLayout.hint = "Received From"
        binding.amountField.setTextColor(getColor(ind.finance.aaroharth.R.color.save_button_background_income))
        binding.saveButton.setBackgroundColor(getColor(ind.finance.aaroharth.R.color.save_button_background_income))
        val color = getColor(ind.finance.aaroharth.R.color.stroke_color_input_income)
        binding.amountLayout.boxStrokeColor = color
        binding.otherPartyLayout.boxStrokeColor = color
        binding.categoryLayout.boxStrokeColor = color
        binding.dateLayout.boxStrokeColor = color
        binding.transactionMediumLayout.boxStrokeColor = color
        binding.transactionWayLayout.boxStrokeColor = color
    }

    private fun expenseUiChanges() {
        binding.subHeading.background = getDrawable(ind.finance.aaroharth.R.drawable.transaction_page_sub_heading_expense)
        binding.subHeading.text = "Expense"
        binding.subHeading.setTextColor(getColor(ind.finance.aaroharth.R.color.subheading_color_expense))
        binding.amountLayout.setStartIconDrawable(getDrawable(ind.finance.aaroharth.R.drawable.rupee_expense))
        binding.amountLayout.setStartIconTintList(ColorStateList.valueOf(getColor(ind.finance.aaroharth.R.color.rupee_symbol_expense)))
        binding.amountField.setTextColor(getColor(ind.finance.aaroharth.R.color.save_button_background_expense))
        binding.otherPartyLayout.hint = "Paid To"
        binding.saveButton.setBackgroundColor(getColor(ind.finance.aaroharth.R.color.save_button_background_expense))
        val color = getColor(ind.finance.aaroharth.R.color.stroke_color_input_expense)
        binding.amountLayout.boxStrokeColor = color
        binding.otherPartyLayout.boxStrokeColor = color
        binding.categoryLayout.boxStrokeColor = color
        binding.dateLayout.boxStrokeColor = color
        binding.transactionMediumLayout.boxStrokeColor = color
        binding.transactionWayLayout.boxStrokeColor = color
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, categoryList)
        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0
            setOnClickListener { showDropDown() }
        }
    }

    private fun validateInput(): Boolean {
        binding.amountLayout.error = null
        binding.categoryLayout.error = null
        binding.transactionMediumLayout.error = null
        binding.transactionWayLayout.error = null

        val amount = binding.amountField.text?.toString()?.trim()
        if (amount.isNullOrEmpty()) {
            binding.amountLayout.error = "Cannot Be Empty"
            return false
        }
        val category = binding.categoryField.text?.toString()?.trim()
        if (category.isNullOrEmpty()) {
            binding.categoryLayout.error = "Cannot Be Empty"
            return false
        }
        if (category !in categoryList) {
            binding.categoryLayout.error = "Select From List"
            return false
        }
        if (binding.transactionMediumField.text.isNullOrEmpty()) {
            binding.transactionMediumLayout.error = "Cannot Be Empty"
            return false
        }
        if (binding.transactionWayField.text.isNullOrEmpty()) {
            binding.transactionWayLayout.error = "Cannot Be Empty"
            return false
        }
        return true
    }

    private fun successLottie(message: String) {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialog.setCancelable(false)
        dialogBinding.dialogLottie.setAnimation("Success.json")
        dialogBinding.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ 
            if (dialog.isShowing) dialog.dismiss() 
        }, 2000)
    }

    private fun dialog(lottie: String, message: String) {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialogBinding.dialogLottie.setAnimation(lottie)
        dialogBinding.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ 
            if (dialog.isShowing) dialog.dismiss() 
        }, 1800)
    }

    private fun catchAssistantIntent() {
        val amount = intent.getLongExtra("amount", -1L)
        val category = intent.getStringExtra("category")
        val accountType = intent.getStringExtra("accountType")
        val note = intent.getStringExtra("note")

        if (amount > 0) binding.amountField.setText(amount.toString())
        if (!category.isNullOrBlank() && category in categoryList) binding.categoryField.setText(category, false)
        if (!accountType.isNullOrBlank()) {
            binding.transactionMediumField.setText(accountType, false)
            viewModel.loadAccountNames(accountType)
        }
        if (!note.isNullOrBlank()) binding.remarkField.setText(note)
    }
}
