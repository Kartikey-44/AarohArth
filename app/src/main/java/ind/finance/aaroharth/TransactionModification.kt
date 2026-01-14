package ind.finance.aaroharth

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TransactionModification : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionModificationBinding
    private lateinit var transactionDao: Transaction_Dao
    private lateinit var accountDao: Account_Dao

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

        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.S){
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
        binding.dateLayout.isEnabled=false
        binding.dateField.isEnabled=false
        binding.dateField.isClickable=false
        binding.dateField.isFocusable=false
        binding.dateLayout.isClickable=false


        transactionDao = App_Database.getInstance(this).transactionDao()
        accountDao = App_Database.getInstance(this).accountDao()

        val id = intent.getLongExtra("id", -1L)
        if (id == -1L) finish()

        lifecycleScope.launch {
            original = transactionDao.getTransactionById(id).copy()
            populateUI(original)
            attachChangeListeners()
            setupActions()
        }

        if (intent.getStringExtra("type") == "Income") income_ui_changes()
        else expense_ui_changes()

        binding.categoryLayout.setEndIconOnClickListener {
            setupCategoryDropdown()
        }
    }

    private fun setupCategoryDropdown() {



        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categoryList
        )

        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0

            setOnClickListener { showDropDown() }

            setOnItemClickListener { parent, _, position, _ ->
                setText(parent.getItemAtPosition(position).toString(), false)
            }
        }
    }

    // ---------------- UI ----------------

    private fun populateUI(tx: Transaction_Info) {
        binding.amountField.setText(tx.amount.toString())
        binding.otherPartyField.setText(tx.otherParty)
        binding.categoryField.setText(tx.category)
        binding.transactionMediumField.setText(tx.transactionMedium)
        binding.transactionWayField.setText(tx.transactionWay)
        binding.remarkField.setText(tx.remark)
        val displaytime= Instant.ofEpochMilli(tx.dateAndTime)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a  dd/MM/yyyy"))
        binding.dateField.setText(displaytime)
    }

    private fun attachChangeListeners() {
        listOf(
            binding.amountField,
            binding.otherPartyField,
            binding.categoryField,
            binding.transactionMediumField,
            binding.transactionWayField,
            binding.remarkField
        ).forEach {
            it.addTextChangedListener { checkForChanges() }
        }
    }

    private fun checkForChanges() {
        current = getCurrentTransactionFromUI()
        binding.saveButton.visibility =
            if (current != original) View.VISIBLE else View.GONE
    }

    private fun getCurrentTransactionFromUI(): Transaction_Info =
        original.copy(
            amount = binding.amountField.text.toString().toLongOrNull() ?: 0L,
            otherParty = binding.otherPartyField.text.toString().trim(),
            category = binding.categoryField.text.toString().trim(),
            transactionMedium = binding.transactionMediumField.text.toString().trim(),
            transactionWay = binding.transactionWayField.text.toString().trim(),
            remark = binding.remarkField.text.toString().trim()
        )

    // ---------------- ACTIONS ----------------

    private fun categorycheck():Boolean{
        binding.categoryLayout.error=null
        if(binding.categoryField.text.toString().trim() !in categoryList){
            binding.categoryLayout.error="Select From List"
            return false
        }
        else{
            return true
        }

    }

    private fun setupActions() {
        binding.saveButton.setOnClickListener {
            if(!categorycheck()){
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                if (!hasSufficientBalance()) {
                    withContext(Dispatchers.Main) {
                        binding.amountLayout.error = "Insufficient Balance"
                    }
                    return@launch
                }

                updateAccountBalance()
                transactionDao.updateTransaction(current)

                withContext(Dispatchers.Main) {
                    saveDialog()
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            deleteConfirmationDialog("stirict.json")
        }
    }

    // ---------------- BALANCE LOGIC (CORRECT) ----------------

    private suspend fun hasSufficientBalance(): Boolean {
        if (original.transactionType == "Income") return true

        val balance = accountDao.getbalance(original.transactionWay)

        // restore original first, then check
        val restored = balance + original.amount

        return restored >= current.amount
    }


    private suspend fun updateAccountBalance() {
        val type = original.transactionType   // DO NOT use intent

        // 1️⃣ RESTORE ORIGINAL TRANSACTION
        val originalBalance = accountDao.getbalance(original.transactionWay)

        val restoredBalance = if (type == "Income") {
            originalBalance - original.amount
        } else {
            originalBalance + original.amount
        }

        accountDao.updatebalance(restoredBalance, original.transactionWay)

        // 2️⃣ APPLY UPDATED TRANSACTION
        val updatedBalance = accountDao.getbalance(current.transactionWay)

        val finalBalance = if (type == "Income") {
            updatedBalance + current.amount
        } else {
            updatedBalance - current.amount
        }

        accountDao.updatebalance(finalBalance, current.transactionWay)
    }







    // ---------------- DIALOGS ----------------

    private fun saveDialog() {
        dialog("Success.json", "Transaction Edited Successfully")
    }

    private fun deleteDialog() {
        dialog("Delete.json", "Transaction Deleted Successfully")
    }

    private fun dialog(lottie: String, message: String) {
        val dialog = Dialog(this)
        val binding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        binding.dialogLottie.setAnimation(lottie)
        binding.message.text = message
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1800)
    }

    private fun deleteConfirmationDialog(animation: String) {
        val dialog = Dialog(this)
        val binding = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.dialogLottie.setAnimation(animation)
        binding.attention.setText(R.string.delete)
        binding.save.text = "Delete"
        binding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.save.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                revertBalanceOnDelete()
                transactionDao.deleteById(original.id)

                withContext(Dispatchers.Main) {
                    deleteDialog()
                }
            }
            dialog.dismiss()
        }


        binding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun quitConfirmationDialog(animation: String) {
        val dialog = Dialog(this)
        val binding = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.dialogLottie.setAnimation(animation)
        binding.attention.setText(R.string.quit)
        binding.save.text = "Quit"
        binding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.save.setOnClickListener {
            finish()
        }

        binding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ---------------- UI THEMES (UNCHANGED) ----------------

    private fun income_ui_changes(){
        binding.heading.text="Add Income"
        binding.subHeading.background=getDrawable(R.drawable.transaction_page_sub_heading_income)
        binding.subHeading.text="Income"
        binding.subHeading.setTextColor(getColor(R.color.subheading_color_income))
        binding.amountLayout.setStartIconDrawable(getDrawable(R.drawable.rupee_income))
        binding.amountLayout.setStartIconTintList(
            ColorStateList.valueOf(getColor(R.color.rupee_symbol_income))
        )
        binding.otherPartyLayout.hint="Received From"
        binding.amountField.setTextColor(getColor(R.color.save_button_background_income))
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_income))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)

    }


    //UI CHANGES ON INTENT EXPENSE

    private fun expense_ui_changes(){
        binding.heading.text="Add Expense"
        binding.subHeading.background=getDrawable(R.drawable.transaction_page_sub_heading_expense)
        binding.subHeading.text="Expense"
        binding.subHeading.setTextColor(getColor(R.color.subheading_color_expense))
        binding.amountLayout.setStartIconDrawable(getDrawable(R.drawable.rupee_expense))
        binding.amountLayout.setStartIconTintList(
            ColorStateList.valueOf(getColor(R.color.rupee_symbol_expense))
        )
        binding.amountField.setTextColor(getColor(R.color.save_button_background_expense))
        binding.otherPartyLayout.hint="Paid To"
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_expense))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
    }


    private suspend fun revertBalanceOnDelete() {
        val balance = accountDao.getbalance(original.transactionWay)

        val newBalance = if (original.transactionType == "Income") {
            balance - original.amount
        } else {
            balance + original.amount
        }

        accountDao.updatebalance(newBalance, original.transactionWay)
    }


}
