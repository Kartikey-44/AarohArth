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
import ind.finance.aaroharth.databinding.ActivityAccountActionsBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.launch

class AccountActions : AppCompatActivity() {

    private lateinit var binding: ActivityAccountActionsBinding
    private lateinit var dialogBinding: DialogScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBlur()
        setupAccountTypes()

        binding.saveBtn.setOnClickListener {
            if (isInputValid()) {
                saveAccount()
            }
        }
    }

    private fun applyBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(
                18f,
                18f,
                Shader.TileMode.CLAMP
            )
            binding.bg.setRenderEffect(blur)
        }
    }

    private fun isInputValid(): Boolean {
        // clear old errors
        binding.nameLayout.error = null
        binding.typeLayout.error = null
        binding.balanceLayout.error = null

        val name = binding.nameField.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameLayout.error = "Required"
            binding.nameField.requestFocus()
            return false
        }

        val type = binding.typeField.text.toString().trim()
        if (type.isEmpty()) {
            binding.typeLayout.error = "Required"
            binding.typeField.requestFocus()
            return false
        }

        val balanceText = binding.balanceField.text.toString().trim()
        val balance = balanceText.toLongOrNull()
        if (balance == null || balance < 0) {
            binding.balanceLayout.error = "Enter valid amount"
            binding.balanceField.requestFocus()
            return false
        }

        return true
    }

    private fun setupAccountTypes() {
        val accountTypes = listOf("UPI", "Cash", "Debit Card","Credit Card", "Bank Account")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            accountTypes
        )
        binding.typeField.setAdapter(adapter)
        binding.typeField.setOnItemClickListener { parent, _, position, _ ->
            binding.typeField.setText(parent.getItemAtPosition(position).toString(), false)
        }
    }

    private fun saveAccount() {
        val account = Account_Info(
            accountType = binding.typeField.text.toString().trim(),
            accountName = binding.nameField.text.toString().trim(),
            normalizedName = binding.nameField.text.toString().lowercase().trim(),
            balance = binding.balanceField.text.toString().trim().toLong()
        )

        val dao = App_Database.getInstance(this).accountDao()

        lifecycleScope.launch {
            try {
                dao.insertAccount(account)
                showSuccessDialog()
            }
            catch (e:android.database.sqlite.SQLiteConstraintException){
                binding.nameLayout.error="Account Name Already Exist"
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )

        dialogBinding.dialogLottie.setAnimation("Success.json")
        dialogBinding.message.text = "Account Saved"
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1800)
    }
}
