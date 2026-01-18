package ind.finance.aaroharth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ind.finance.aaroharth.databinding.ActivityAiBinding
import java.util.Locale
import kotlin.jvm.java

class AI_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityAiBinding

    private val SPEECH_REQ = 101


    private val categoryAliases: Map<String, List<String>> = mapOf(

        // Housing
        "Rental" to listOf("rent", "rental", "house rent", "flat rent"),

        // Food
        "Food" to listOf("food", "meal", "lunch", "dinner"),
        "Dining Out" to listOf("restaurant", "dining", "hotel food"),
        "Grocery" to listOf("grocery", "groceries", "ration"),

        // Transport & Fuel
        "Petrol" to listOf("petrol", "fuel"),
        "Diesel" to listOf("diesel"),
        "CNG" to listOf("cng"),
        "Taxi" to listOf("taxi", "cab", "uber", "ola"),
        "Auto" to listOf("auto", "rickshaw"),
        "Public Transport" to listOf("bus", "metro", "train"),

        // Utilities
        "Electricity" to listOf("electricity", "light bill", "power bill"),
        "Water Bill" to listOf("water", "water bill"),
        "LPG" to listOf("lpg", "gas"),
        "PNG" to listOf("png"),

        // Finance
        "Mobile Recharge" to listOf("mobile recharge", "recharge"),
        "FastTag Recharge" to listOf("fastag", "fasttag"),
        "EMI / Loans" to listOf("emi", "loan"),
        "Insurance" to listOf("insurance"),
        "Tax" to listOf("tax"),

        // Health & Education
        "Medical" to listOf("medical", "doctor", "hospital"),
        "Education" to listOf("school", "college", "education"),

        // Travel & Stay
        "Hotel" to listOf("hotel", "stay"),
        "Flight" to listOf("flight", "air ticket"),

        // Income
        "Salary" to listOf("salary"),
        "Business" to listOf("business"),
        "Freelance" to listOf("freelance"),
        "Investment" to listOf("investment"),
        "Savings" to listOf("saving", "savings"),

        // Others
        "Shopping" to listOf("shopping", "clothes"),
        "Personal Care" to listOf("salon", "parlour", "personal care"),
        "Entertainment" to listOf("movie", "entertainment"),
        "Subscriptions" to listOf("subscription", "netflix", "prime"),
        "Gifts" to listOf("gift"),
        "Donations" to listOf("donation")
    )

    private val expenseKeywords = setOf(
        "spend", "spent", "expense", "pay", "paid", "payed",
        "send", "sent", "debit", "debited",
        "buy", "bought", "purchase", "purchased",
        "rent", "donate", "donated",
        "gifted", "lend", "lent",
        "invest", "invested",
        "transfer out", "withdraw", "withdrawn"
    )

    private val incomeKeywords = setOf(
        "receive", "received", "get", "got",
        "earn", "earned", "income",
        "credit", "credited",
        "salary", "refund",
        "repayment", "returned",
        "sell", "sold",
        "interest", "dividend",
        "bonus",
        "transfer in",
        "deposit", "deposited"
    )
    private val transactionKeywords = setOf(
        "spend", "spent", "expense", "pay", "paid", "payed",
        "send", "sent", "debit", "debited",
        "buy", "bought", "purchase", "purchased",
        "rent", "donate", "donated",
        "gifted", "lend", "lent",
        "invest", "invested",
        "transfer out", "withdraw", "withdrawn",
        "receive", "received", "get", "got",
        "earn", "earned", "income",
        "credit", "credited",
        "salary", "refund",
        "repayment", "returned",
        "sell", "sold",
        "interest", "dividend",
        "bonus",
        "transfer in",
        "deposit", "deposited"
    )

    private val budgetKeywords = setOf(
        "budget","limit","cap"
    )

    private val accountKeywords = setOf(
        "add account","create account","new account"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mice.setOnClickListener { startSpeech() }

        binding.inputField.setOnClickListener {
            if (binding.lottie.isVisible) stopListeningUI()
        }

        binding.send.setOnClickListener {
            val input = binding.inputField.text.toString().trim()
            if (input.isEmpty()) {
                binding.inputLayout.error = "Enter something"
                return@setOnClickListener
            }
            processInput(input)
        }
    }
    object AccountTypes {
        const val UPI = "UPI"
        const val CASH = "Cash"
        const val DEBIT_CARD = "Debit Card"
        const val CREDIT_CARD = "Credit Card"
        const val BANK_ACCOUNT = "Bank Account"

        val ALL = listOf(UPI, CASH, DEBIT_CARD, CREDIT_CARD, BANK_ACCOUNT)
    }


    // ---------------- SPEECH ----------------

    private fun startSpeech() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                SPEECH_REQ
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")

        startListeningUI()
        startActivityForResult(intent, SPEECH_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stopListeningUI()

        if (requestCode == SPEECH_REQ && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                binding.inputField.setText(result[0])
            }
        }
    }

    private fun startListeningUI() {
        binding.lottie.visibility = View.VISIBLE
        binding.instruct.visibility = View.GONE
        binding.lottie.playAnimation()
    }

    private fun stopListeningUI() {
        binding.lottie.cancelAnimation()
        binding.lottie.visibility = View.GONE
        binding.instruct.visibility = View.VISIBLE
    }

    // ---------------- CORE LOGIC ----------------

    private fun processInput(text: String) {
        when (detectAction(text)) {
            "TRANSACTION" -> openTransaction(text)
            "BUDGET" -> openBudget(text)
            "ACCOUNT" -> openAccount(text)
            else -> Toast.makeText(this, "Could not understand intent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detectAction(text: String): String {
        val t = text.lowercase()
        if (budgetKeywords.any { t.contains(it) }) {
            return "BUDGET"
        }

        if (accountKeywords.any { t.contains(it) }) {
            return "ACCOUNT"
        }

        // ANY money movement = transaction
        if (transactionKeywords.any { t.contains(it)}) {
            return "TRANSACTION"
        }

        return "UNKNOWN"
    }

    // ---------------- TRANSACTION ----------------

    private fun openTransaction(text: String) {
        val intent = Intent(this, Transaction_Action_Page::class.java)
        intent.putExtra("type",gettype(text))
        intent.putExtra("amount", extractAmount(text))
        intent.putExtra("category", extractCategory(text))
        intent.putExtra("accountType", extractAccountType(text))
        intent.putExtra("note", text)
        startActivity(intent)
    }
    private fun gettype(text: String):String{
        val t=text.lowercase()
        if(expenseKeywords.any {t.contains(it)} ){
            return "expense"
        }
        else if(incomeKeywords.any{t.contains(it)})
        {
            return "income"
        }
        else{
            return Toast.makeText(this,"Can't Decide Income Or Expense",Toast.LENGTH_SHORT).toString()
        }

    }

    // ---------------- BUDGET ----------------

    private fun openBudget(text: String) {
        val intent = Intent(this, BudgetActions::class.java)
        intent.putExtra("amount", extractAmount(text))
        intent.putExtra("category", extractCategory(text))
        intent.putExtra("note", text)
        startActivity(intent)
    }

    // ---------------- ACCOUNT ----------------

    private fun openAccount(text: String) {
        val intent = Intent(this, AccountActions::class.java)
        intent.putExtra("accountType", extractAccountType(text))
        intent.putExtra("balance", extractAmount(text))
        intent.putExtra("note", text)
        startActivity(intent)
    }

    // ---------------- EXTRACTION ----------------

    private fun extractAmount(text: String): Long? {
        val regex = Regex("""\b\d+\b""")
        return regex.find(text)?.value?.toLong()
    }

    private fun extractCategory(text: String): String {
        val t = text.lowercase()

        for ((category, keywords) in categoryAliases) {
            if (keywords.any { t.contains(it) }) {
                return category
            }
        }

        return "Other"
    }


    private fun extractAccountType(text: String): String? {
        val t = text.lowercase()
        return when {
            t.contains("upi") || t.contains("gpay") || t.contains("phonepe") ->
                AccountTypes.UPI
            t.contains("cash") ->
                AccountTypes.CASH
            t.contains("debit") ->
                AccountTypes.DEBIT_CARD
            t.contains("credit") ->
                AccountTypes.CREDIT_CARD
            t.contains("bank") || t.contains("account") ->
                AccountTypes.BANK_ACCOUNT
            else -> null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SPEECH_REQ &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeech()
        }
    }


}
