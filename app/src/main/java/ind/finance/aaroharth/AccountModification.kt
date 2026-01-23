package ind.finance.aaroharth

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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.ActivityAccountModificationBinding
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountModification : AppCompatActivity() {

    private lateinit var binding: ActivityAccountModificationBinding
    private lateinit var db: App_Database
    private lateinit var original: Account_Info
    private lateinit var current: Account_Info

    // prevents fake "changed" events when we clear text programmatically
    private var suppressChange = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountModificationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.S){
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bg.setRenderEffect(blur)
        }
        db = App_Database.getInstance(this)

        onBackPressedDispatcher.addCallback(this) {
            if (::current.isInitialized && current != original) {
                quitConfirmationDialog("normal.json")
            } else {
                finish()
            }
        }

        val accountId = intent.getLongExtra("id", -1L)
        if (accountId == -1L) finish()

        lifecycleScope.launch {
            val txCount = db.transactionDao().getNoOfTransaction(intent.getStringExtra("name"))

            original = db.accountDao().getAccountById(accountId)
            if (txCount > 0) {
                binding.heading.text = "Account Info"
                binding.attention.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
                binding.nameField.setText(original.accountName)
                binding.typeField.setText(original.accountType)
                binding.balanceField.setText(original.balance.toString())
                binding.balanceField.isClickable=false
                binding.balanceField.isEnabled=false
                binding.nameField.isEnabled=false
                binding.nameField.isClickable=false
                binding.typeField.isClickable=false
                binding.typeField.isEnabled=false
                binding.typeField.setTextColor(getColor(R.color.black))
            } else {
                binding.heading.text = "Edit / Delete Account"
                binding.attention.visibility = View.GONE
                binding.saveBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.VISIBLE

                setupAccountTypes()
                populateUI(original)
                updateHint(original.accountType)
                addTextWatchers()
            }
        }

        binding.saveBtn.setOnClickListener {
            if (!isInputValid()) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                db.accountDao().updateaccountinfo(current)
                withContext(Dispatchers.Main) { successDialog() }
            }
        }

        binding.deleteBtn.setOnClickListener {
            deleteConfirmationDialog("stirict.json")
        }
    }

    /* -------------------- UI SETUP -------------------- */

    private fun populateUI(acc: Account_Info) {
        binding.nameField.setText(acc.accountName)
        binding.typeField.setText(acc.accountType, false)
        binding.balanceField.setText(acc.balance.toString())
    }

    private fun addTextWatchers() {
        listOf(
            binding.nameField,
            binding.typeField,
            binding.balanceField
        ).forEach { field ->
            field.addTextChangedListener {
                if (suppressChange) return@addTextChangedListener
                onDataChanged()
            }
        }
    }

    private fun onDataChanged() {
        current = getCurrent()
        binding.saveBtn.visibility =
            if (current != original) View.VISIBLE else View.GONE
    }

    private fun getCurrent(): Account_Info =
        original.copy(
            accountName = binding.nameField.text.toString().trim(),
            accountType = binding.typeField.text.toString().trim(),
            balance = binding.balanceField.text.toString().toLongOrNull() ?: 0L
        )

    /* -------------------- ACCOUNT TYPE DROPDOWN -------------------- */

    private fun setupAccountTypes() {
        val types = listOf(
            "UPI",
            "Cash",
            "Debit Card",
            "Credit Card",
            "Bank Account"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            types
        )

        binding.typeField.setAdapter(adapter)

        // turn AutoCompleteTextView into a selector
        binding.typeField.apply {
            keyListener = null
            isFocusable = false
            isCursorVisible = false

            setOnClickListener {
                suppressChange = true
                text.clear()      // remove filter
                showDropDown()    // show full list
                suppressChange = false
            }
        }

        binding.typeField.setOnItemClickListener { parent, _, position, _ ->
            val type = parent.getItemAtPosition(position).toString()
            binding.typeField.setText(type, false)
            updateHint(type)
            onDataChanged()
        }
    }

    /* -------------------- HINT / VALIDATION -------------------- */

    private fun updateHint(type: String) {
        // FULL reset first
        binding.nameField.apply {
            isEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
        binding.nameLayout.isEnabled = true

        when (type.lowercase()) {
            "upi" -> {
                binding.nameLayout.hint = "UPI ID / App Name"
                binding.nameField.text?.clear()
            }

            "cash" -> {
                binding.nameField.setText("Cash")
                binding.nameField.isEnabled = false
                binding.nameField.isFocusable = false
                binding.nameField.isFocusableInTouchMode = false
            }

            "debit card", "credit card" -> {
                binding.nameLayout.hint = "Card Name (SBI, HDFC, etc.)"
                binding.nameField.text?.clear()
            }

            else -> {
                binding.nameLayout.hint = "Account Name"
                binding.nameField.text?.clear()
            }
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

    /* -------------------- DIALOGS -------------------- */

    private fun successDialog() {
        showDialog("Success.json", "Account Updated Successfully")
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
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dBinding.save.setOnClickListener {
            lifecycleScope.launch {
                // delete in background
                withContext(Dispatchers.IO) {
                    db.accountDao().deletebyid(original.id)
                }

                // UI work AFTER delete
                dialog.dismiss()
                showDialog("Delete.json", "Account Deleted Successfully")
            }
        }

        dBinding.skip.setOnClickListener {
            dialog.dismiss()
        }

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
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dBinding.save.setOnClickListener { finish() }
        dBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
