package ind.finance.aaroharth

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.ActivityBudgetModificationBinding
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetModification : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetModificationBinding
    private lateinit var db: App_Database

    private lateinit var original: Budget_Info
    private lateinit var current: Budget_Info

    private var suppressChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBudgetModificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        db = App_Database.getInstance(this)

        val budgetId = intent.getLongExtra("budget_id", -1L)
        if (budgetId == -1L) {
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this) {
            if (::current.isInitialized && current != original) {
                quitConfirmationDialog("normal.json")
            } else {
                finish()
            }
        }

        lifecycleScope.launch {
            original = db.budgetDao().getBudgetById(budgetId)
            populateUI(original)
            addTextWatchers()
        }

        binding.saveBtn.setOnClickListener {
            if (!isInputValid()) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                db.budgetDao().updateBudget(
                    id = original.id,
                    category = original.category,
                    amount = current.amount
                )
                withContext(Dispatchers.Main) {
                    successDialog("Budget Updated Successfully")
                }
            }
        }

        // 🔥 DELETE BUTTON
        binding.deleteBtn.setOnClickListener{
            deleteConfirmationDialog("stirict.json")
        }
    }

    /* ---------------- UI ---------------- */

    private fun populateUI(budget: Budget_Info) {
        suppressChange = true

        binding.categoryField.setText(budget.category, false)
        binding.amountField.setText(budget.amount.toString())

        // 🔒 Category must never change
        binding.categoryField.isEnabled = false
        binding.categoryField.isFocusable = false
        binding.categoryLayout.endIconMode =
            com.google.android.material.textfield.TextInputLayout.END_ICON_NONE

        suppressChange = false
    }

    private fun addTextWatchers() {
        binding.amountField.addTextChangedListener {
            if (suppressChange) return@addTextChangedListener
            onDataChanged()
        }
    }

    private fun onDataChanged() {
        current = getCurrent()
        binding.saveBtn.visibility =
            if (current != original) View.VISIBLE else View.GONE
    }

    private fun getCurrent(): Budget_Info =
        original.copy(
            amount = binding.amountField.text.toString().toLongOrNull() ?: 0L
        )

    /* ---------------- VALIDATION ---------------- */

    private fun isInputValid(): Boolean {
        binding.amountLayout.error = null
        val amount = binding.amountField.text.toString().toLongOrNull()
        if (amount == null || amount <= 0) {
            binding.amountLayout.error = "Invalid amount"
            return false
        }
        return true
    }

    /* ---------------- DIALOGS ---------------- */

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
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dBinding.save.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.budgetDao().deleteBudgetById(original.id)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    successDialog("Budget Deleted Successfully")
                }
            }
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
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dBinding.save.setOnClickListener { finish() }
        dBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
