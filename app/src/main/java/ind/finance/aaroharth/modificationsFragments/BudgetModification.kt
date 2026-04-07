package ind.finance.aaroharth.modificationsFragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.Budget_Info
import ind.finance.aaroharth.databinding.ActivityBudgetModificationBinding
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.BudgetViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class BudgetModification : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetModificationBinding
    private val viewModel: BudgetViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    private lateinit var original: Budget_Info
    private var currentAmount: Long = 0
    private var suppressChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBudgetModificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val budgetId = intent.getLongExtra("budget_id", -1L)
        if (budgetId == -1L) {
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this) {
            if (::original.isInitialized && currentAmount != original.amount) {
                quitConfirmationDialog("normal.json")
            } else {
                finish()
            }
        }

        observeViewModel()
        viewModel.loadBudgetById(budgetId)

        binding.saveBtn.setOnClickListener {
            if (isInputValid()) {
                viewModel.updateBudget(original.id, original.category, currentAmount)
            }
        }

        binding.deleteBtn.setOnClickListener {
            deleteConfirmationDialog("stirict.json")
        }
    }

    private fun observeViewModel() {
        viewModel.currentBudget.observe(this) { budget ->
            original = budget
            currentAmount = budget.amount
            populateUI(budget)
            addTextWatchers()
        }

        viewModel.operationSuccess.observe(this) { message ->
            successDialog(message)
        }
    }

    private fun populateUI(budget: Budget_Info) {
        suppressChange = true
        binding.categoryField.setText(budget.category, false)
        binding.amountField.setText(budget.amount.toString())
        binding.categoryField.isEnabled = false
        binding.categoryField.isFocusable = false
        binding.categoryLayout.endIconMode = TextInputLayout.END_ICON_NONE
        suppressChange = false
    }

    private fun addTextWatchers() {
        binding.amountField.addTextChangedListener {
            if (suppressChange) return@addTextChangedListener
            currentAmount = binding.amountField.text.toString().toLongOrNull() ?: 0L
            binding.saveBtn.visibility = if (currentAmount != original.amount) View.VISIBLE else View.GONE
        }
    }

    private fun isInputValid(): Boolean {
        binding.amountLayout.error = null
        val amount = binding.amountField.text.toString().toLongOrNull()
        if (amount == null || amount <= 0) {
            binding.amountLayout.error = "Invalid amount"
            return false
        }
        return true
    }

    private fun successDialog(message: String) {
        showDialog("Success.json", message)
    }

    private fun showDialog(lottie: String, message: String) {
        val dialog = Dialog(this)
        val dBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dBinding.dialogLottie.setAnimation(lottie)
        dBinding.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1800)
    }

    private fun deleteConfirmationDialog(animation: String) {
        val dialog = Dialog(this)
        val dBinding = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dBinding.dialogLottie.setAnimation(animation)
        dBinding.attention.text = "Delete this budget?"
        dBinding.save.text = "Delete"
        dBinding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        dBinding.save.setOnClickListener {
            viewModel.deleteBudget(original.id)
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
        dBinding.attention.text = "Discard changes?"
        dBinding.save.text = "Quit"
        dBinding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dBinding.save.setOnClickListener { finish() }
        dBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}