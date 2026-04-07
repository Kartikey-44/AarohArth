package ind.finance.aaroharth.modificationsFragments

import android.app.Dialog
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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import ind.finance.aaroharth.MyApplication
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.Account_Info
import ind.finance.aaroharth.databinding.ActivityAccountModificationBinding
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.AccountViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class AccountModification : AppCompatActivity() {

    private lateinit var binding: ActivityAccountModificationBinding
    private val viewModel: AccountViewModel by viewModels {
        val app = application as MyApplication
        ViewModelFactory(app.transactionRepository, app.accountRepository, app.budgetRepository, app.notificationRepository)
    }

    private lateinit var original: Account_Info
    private lateinit var current: Account_Info
    private var suppressChange = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountModificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bg.setRenderEffect(blur)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (::current.isInitialized && current != original) {
                quitConfirmationDialog("normal.json")
            } else {
                finish()
            }
        }

        val accountId = intent.getLongExtra("id", -1L)
        val accountName = intent.getStringExtra("name")
        if (accountId == -1L) {
            finish()
            return
        }

        observeViewModel()
        viewModel.loadAccountDetails(accountId, accountName)

        binding.saveBtn.setOnClickListener {
            if (isInputValid()) {
                viewModel.updateAccount(current)
            }
        }

        binding.deleteBtn.setOnClickListener {
            deleteConfirmationDialog("stirict.json")
        }
    }

    private fun observeViewModel() {
        viewModel.currentAccount.observe(this) { acc ->
            original = acc
            current = acc.copy()

            val txCount = viewModel.transactionCount.value ?: 0L
            if (txCount > 0) {
                setupViewOnlyMode(acc)
            } else {
                setupEditMode(acc)
            }
        }

        viewModel.operationSuccess.observe(this) { message ->
            successDialog(message)
        }
    }

    private fun setupViewOnlyMode(acc: Account_Info) {
        binding.heading.text = "Account Info"
        binding.attention.visibility = View.VISIBLE
        binding.saveBtn.visibility = View.GONE
        binding.deleteBtn.visibility = View.GONE
        populateUI(acc)
        disableInputs()
    }

    private fun setupEditMode(acc: Account_Info) {
        binding.heading.text = "Edit / Delete Account"
        binding.attention.visibility = View.GONE
        binding.saveBtn.visibility = View.GONE
        binding.deleteBtn.visibility = View.VISIBLE
        setupAccountTypes()
        populateUI(acc)
        updateHint(acc.accountType)
        addTextWatchers()
    }

    private fun populateUI(acc: Account_Info) {
        suppressChange = true
        binding.nameField.setText(acc.accountName)
        binding.typeField.setText(acc.accountType, false)
        binding.balanceField.setText(acc.balance.toString())
        suppressChange = false
    }

    private fun disableInputs() {
        binding.balanceField.isEnabled = false
        binding.nameField.isEnabled = false
        binding.typeField.isEnabled = false
        binding.typeField.setTextColor(getColor(R.color.black))
    }

    private fun addTextWatchers() {
        listOf(binding.nameField, binding.typeField, binding.balanceField).forEach { field ->
            field.addTextChangedListener {
                if (suppressChange) return@addTextChangedListener
                current = getCurrentFromUI()
                binding.saveBtn.visibility = if (current != original) View.VISIBLE else View.GONE
            }
        }
    }

    private fun getCurrentFromUI(): Account_Info =
        original.copy(
            accountName = binding.nameField.text.toString().trim(),
            accountType = binding.typeField.text.toString().trim(),
            balance = binding.balanceField.text.toString().toLongOrNull() ?: 0L
        )

    private fun setupAccountTypes() {
        val types = listOf("UPI", "Cash", "Debit Card", "Credit Card", "Bank Account")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        binding.typeField.setAdapter(adapter)
        binding.typeField.apply {
            keyListener = null
            isFocusable = false
            setOnClickListener {
                suppressChange = true
                text.clear()
                showDropDown()
                suppressChange = false
            }
        }
        binding.typeField.setOnItemClickListener { parent, _, position, _ ->
            val type = parent.getItemAtPosition(position).toString()
            binding.typeField.setText(type, false)
            updateHint(type)
        }
    }

    private fun updateHint(type: String) {
        binding.nameField.apply { isEnabled = true; isFocusable = true; isFocusableInTouchMode = true }
        binding.nameLayout.isEnabled = true
        when (type.lowercase()) {
            "upi" -> binding.nameLayout.hint = "UPI ID / App Name"
            "cash" -> {
                binding.nameField.setText("Cash")
                binding.nameField.isEnabled = false
            }
            "debit card", "credit card" -> binding.nameLayout.hint = "Card Name (SBI, HDFC, etc.)"
            else -> binding.nameLayout.hint = "Account Name"
        }
    }

    private fun isInputValid(): Boolean {
        binding.nameLayout.error = null
        binding.typeLayout.error = null
        binding.balanceLayout.error = null
        if (binding.nameField.text.isNullOrBlank()) {
            binding.nameLayout.error = "Required"
            return false
        }
        if (binding.typeField.text.isNullOrBlank()) {
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
        dBinding.attention.text = "Delete this account?"
        dBinding.save.text = "Delete"
        dBinding.skip.text = "Cancel"
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dBinding.save.setOnClickListener {
            viewModel.deleteAccount(original.id)
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