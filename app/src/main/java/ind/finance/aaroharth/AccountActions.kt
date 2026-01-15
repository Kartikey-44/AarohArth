package ind.finance.aaroharth

import android.app.Dialog
import android.content.Intent
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
import ind.finance.aaroharth.databinding.ActivityAccountActionsBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountActions : AppCompatActivity() {

    private lateinit var binding: ActivityAccountActionsBinding
    private var successDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBlur()
        setupAccountTypes()
        numberOfAccounts()

        binding.nameLayout.setOnClickListener {
            binding.nameLayout.error=null
        }
        binding.saveBtn.setOnClickListener {
            if (!isInputValid()) return@setOnClickListener
            saveAccount()
        }
    }

    private fun applyBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.bg.setRenderEffect(
                RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            )
        }
    }

    private fun setupAccountTypes() {
        val accountTypes = listOf("UPI", "Cash", "Debit Card", "Credit Card", "Bank Account")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            accountTypes
        )
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
            "cash" -> {binding.nameField.setText("Cash")
                binding.nameLayout.isClickable=false
            binding.nameLayout.isEnabled=false}
            "debit card", "credit card" ->
                binding.nameLayout.hint = "Card Name (SBI, HDFC, etc.)"
            else -> binding.nameLayout.hint = "Account Name"
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

    private fun saveAccount() {
        val dao = App_Database.getInstance(this).accountDao()
        val account = Account_Info(
            accountType = binding.typeField.text.toString().trim(),
            accountName = binding.nameField.text.toString().trim(),
            normalizedName = binding.nameField.text.toString().lowercase().trim(),
            balance = binding.balanceField.text.toString().toLong()
        )

        lifecycleScope.launch {
            try {
                dao.insertAccount(account)

                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("has_account", true)
                    .apply()

                showSuccessDialog()

            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                binding.nameLayout.error = "Account already exists"
            }
        }

    }

    private fun showSuccessDialog() {
        successDialog = Dialog(this)
        val dialogBinding = DialogScreenBinding.inflate(layoutInflater)

        successDialog!!.setContentView(dialogBinding.root)
        successDialog!!.setCancelable(false)
        successDialog!!.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )

        dialogBinding.message.text = "Account Saved"
        dialogBinding.dialogLottie.setAnimation("Success.json")

        successDialog!!.show()

        val dao= App_Database.getInstance(this).accountDao()
      lifecycleScope.launch(Dispatchers.IO) {
          val numberofAccount=dao.numberOfAccounts()
          withContext(Dispatchers.Main){
              if(numberofAccount<=1){
                  Handler(Looper.getMainLooper()).postDelayed({
                      startActivity(Intent(this@AccountActions,MainActivity::class.java))
                      finish()
                  },1800)
              }
              else{
                  Handler(Looper.getMainLooper()).postDelayed({
                      finish()
                  },1800)

              }
          }
      }

    }

    private fun numberOfAccounts(){
        val dao=App_Database.getInstance(this).accountDao()
        lifecycleScope.launch(Dispatchers.IO) {
            val numberOfAccounts=dao.numberOfAccounts()
            withContext(Dispatchers.Main){
                if(numberOfAccounts==0){
                    binding.heading.text="Setup Your Account"
                }
                else{
                    binding.heading.text="Add Account"
                }
            }

        }

    }

    override fun onDestroy() {
        successDialog?.dismiss()
        super.onDestroy()
    }
}
