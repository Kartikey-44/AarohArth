package ind.finance.aaroharth.modificationsFragments

import android.R
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.databinding.ActivityTransactionModificationBinding
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.TransactionActionViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TransactionModification : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionModificationBinding
    private val viewModel: TransactionActionViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository)
    }

    private lateinit var original: Transaction_Info
    private lateinit var current: Transaction_Info

    private val categoryList = listOf(
        "Mobile Recharge", "FastTag Recharge", "Salary", "Business", "Freelance", "Investment", "Savings", "Food",
        "Dining Out", "Shopping", "Personal Care", "Entertainment", "Subscriptions", "Housing", "Rental", "Utilities",
        "Public Transport", "Petrol", "Diesel", "CNG", "Electricity", "LPG", "PNG", "Taxi", "Auto", "Hotel",
        "Flight", "Medical", "Insurance", "Education", "EMI / Loans", "Tax", "Gifts", "Donations",
        "Miscellaneous", "Water Bill","Grocery","Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionModificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.background.setRenderEffect(blur)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (::current.isInitialized && current != original) {
                quitConfirmationDialog(animation = "normal.json")
            } else {
                finish()
            }
        }

        binding.dateLayout.isEnabled = false
        binding.dateField.isEnabled = false
        binding.heading.text = "Edit / Delete Transaction"

        val id = intent.getLongExtra("id", -1L)
        if (id == -1L) {
            finish()
            return
        }

        observeViewModel()
        viewModel.loadTransactionById(id)

        binding.saveButton.setOnClickListener {
            if (validateCategory()) {
                viewModel.updateTransaction(original, current)
            }
        }

        binding.deleteButton.setOnClickListener {
            deleteConfirmationDialog("stirict.json")
        }

        binding.categoryLayout.setEndIconOnClickListener {
            setupCategoryDropdown()
        }
    }

    private fun observeViewModel() {
        viewModel.currentTransaction.observe(this) { tx ->
            original = tx
            current = tx.copy()
            populateUI(tx)
            attachChangeListeners()
            if (tx.transactionType == "Income" || tx.transactionType == "income") incomeUiChanges()
            else expenseUiChanges()
        }

        viewModel.saveStatus.observe(this) { message ->
            dialog("Success.json", message)
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg == "Insufficient Balance") {
                binding.amountLayout.error = errorMsg
            } else {
                dialog("Failed.json", errorMsg)
            }
        }
    }

    private fun populateUI(tx: Transaction_Info) {
        binding.amountField.setText(tx.amount.toString())
        binding.otherPartyField.setText(tx.otherParty)
        binding.categoryField.setText(tx.category)
        binding.transactionMediumField.setText(tx.transactionMedium)
        binding.transactionWayField.setText(tx.transactionWay)
        binding.remarkField.setText(tx.remark)
        val displayTime = Instant.ofEpochMilli(tx.dateAndTime)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a  dd/MM/yyyy"))
        binding.dateField.setText(displayTime)
    }

    private fun attachChangeListeners() {
        listOf(binding.amountField, binding.otherPartyField, binding.categoryField,
               binding.transactionMediumField, binding.transactionWayField, binding.remarkField).forEach {
            it.addTextChangedListener {
                current = original.copy(
                    amount = binding.amountField.text.toString().toLongOrNull() ?: 0L,
                    otherParty = binding.otherPartyField.text.toString().trim(),
                    category = binding.categoryField.text.toString().trim(),
                    transactionMedium = binding.transactionMediumField.text.toString().trim(),
                    transactionWay = binding.transactionWayField.text.toString().trim(),
                    remark = binding.remarkField.text.toString().trim()
                )
                binding.saveButton.visibility = if (current != original) View.VISIBLE else View.GONE
            }
        }
    }

    private fun validateCategory(): Boolean {
        binding.categoryLayout.error = null
        return if (binding.categoryField.text.toString().trim() !in categoryList) {
            binding.categoryLayout.error = "Select From List"
            false
        } else true
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, categoryList)
        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0
            setOnClickListener { showDropDown() }
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

    private fun dialog(lottie: String, message: String) {
        val dialog = Dialog(this)
        val dBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dBinding.dialogLottie.setAnimation(lottie)
        dBinding.message.text = message
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss(); finish() }, 1800)
    }

    private fun deleteConfirmationDialog(animation: String) {
        val dialog = Dialog(this)
        val dBinding = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dBinding.dialogLottie.setAnimation(animation)
        dBinding.attention.setText(ind.finance.aaroharth.R.string.delete)
        dBinding.save.text = "Delete"
        dBinding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        dBinding.save.setOnClickListener {
            viewModel.deleteTransaction(original)
            dialog.dismiss()
        }
        dBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun quitConfirmationDialog(animation: String) {
        val dialog = Dialog(this)
        val dBinding = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dBinding.dialogLottie.setAnimation(animation)
        dBinding.attention.setText(ind.finance.aaroharth.R.string.quit)
        dBinding.save.text = "Quit"
        dBinding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dBinding.save.setOnClickListener { finish() }
        dBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}