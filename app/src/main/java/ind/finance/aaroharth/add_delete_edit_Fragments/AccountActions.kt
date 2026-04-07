package ind.finance.aaroharth.add_delete_edit_Fragments

import android.R
import android.app.Dialog
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ind.finance.aaroharth.MainActivity
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.databinding.ActivityAccountActionsBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.AccountViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class AccountActions : AppCompatActivity() {

    private lateinit var binding: ActivityAccountActionsBinding
    private var successDialog: Dialog? = null
    private val viewModel: AccountViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBlur()
        setupAccountTypes()
        observeViewModel()
        viewModel.loadAccounts()

        binding.nameLayout.setOnClickListener { binding.nameLayout.error = null }
        binding.saveBtn.setOnClickListener {
            if (isInputValid()) {
                viewModel.saveAccount(
                    type = binding.typeField.text.toString().trim(),
                    name = binding.nameField.text.toString().trim(),
                    balance = binding.balanceField.text.toString().toLong()
                )
            }
        }
    }

    private fun applyBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.bg.setRenderEffect(RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP))
        }
    }

    private fun setupAccountTypes() {
        val accountTypes = listOf("UPI", "Cash", "Debit Card", "Credit Card", "Bank Account")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, accountTypes)
        binding.typeField.setAdapter(adapter)
        binding.typeField.setOnItemClickListener { parent, _, position, _ ->
            val type = parent.getItemAtPosition(position).toString()
            binding.typeField.setText(type, false)
            binding.nameField.text?.clear()
            updateHint(type)
        }
    }

    private fun updateHint(type: String) {
        binding.nameField.isEnabled = true
        when (type.lowercase()) {
            "upi" -> binding.nameLayout.hint = "UPI ID / App Name"
            "cash" -> {
                binding.nameField.setText("Cash")
                binding.nameLayout.isClickable = false
                binding.nameLayout.isEnabled = false
            }
            "debit card", "credit card" -> binding.nameLayout.hint = "Card Name (SBI, HDFC, etc.)"
            else -> binding.nameLayout.hint = "Account Name"
        }
    }

    private fun observeViewModel() {
        viewModel.accountCount.observe(this) { count ->
            binding.heading.text = if (count == 0) "Setup Your Account" else "Add Account"
        }

        viewModel.saveStatus.observe(this) { success ->
            if (success) {
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("has_account", true).apply()
                showSuccessDialog()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            binding.nameLayout.error = errorMsg
        }
    }

    private fun isInputValid(): Boolean {
        binding.nameLayout.error = null
        binding.typeLayout.error = null
        binding.balanceLayout.error = null

        if (binding.nameField.text.toString().trim().isEmpty()) {
            binding.nameLayout.error = "Required"
            return false
        }
        if (binding.typeField.text.toString().trim().isEmpty()) {
            binding.typeLayout.error = "Required"
            return false
        }
        val balance = binding.balanceField.text.toString().toLongOrNull()
        if (balance == null || balance < 0) {
            binding.balanceLayout.error = "Invalid amount"
            return false
        }
        return true
    }

    private fun showSuccessDialog() {
        successDialog = Dialog(this)
        val dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        successDialog!!.setContentView(dialogBinding.root)
        successDialog!!.setCancelable(false)
        successDialog!!.window?.setBackgroundDrawable(getDrawable(ind.finance.aaroharth.R.drawable.dialog_background))
        dialogBinding.message.text = "Account Saved"
        dialogBinding.dialogLottie.setAnimation("Success.json")
        successDialog!!.show()

        Handler(Looper.getMainLooper()).postDelayed({
            successDialog?.dismiss()
            if ((viewModel.accountCount.value ?: 0) <= 1) {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 1800)
    }

    override fun onDestroy() {
        successDialog?.dismiss()
        super.onDestroy()
    }
}