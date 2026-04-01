package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.iterator

class AI_ViewModel : ViewModel() {

    private val _extractedData = MutableLiveData<ExtractedData>()
    val extractedData: LiveData<ExtractedData> = _extractedData

    private val categoryAliases: Map<String, List<String>> = mapOf(
        "Rental" to listOf("rent", "rental", "house rent", "flat rent"),
        "Food" to listOf("food", "meal", "lunch", "dinner"),
        "Dining Out" to listOf("restaurant", "dining", "hotel food"),
        "Grocery" to listOf("grocery", "groceries", "ration"),
        "Petrol" to listOf("petrol", "fuel"),
        "Diesel" to listOf("diesel"),
        "CNG" to listOf("cng"),
        "Taxi" to listOf("taxi", "cab", "uber", "ola"),
        "Auto" to listOf("auto", "rickshaw"),
        "Public Transport" to listOf("bus", "metro", "train"),
        "Electricity" to listOf("electricity", "light bill", "power bill"),
        "Water Bill" to listOf("water", "water bill"),
        "LPG" to listOf("lpg", "gas"),
        "PNG" to listOf("png"),
        "Mobile Recharge" to listOf("mobile recharge", "recharge"),
        "FastTag Recharge" to listOf("fastag", "fasttag"),
        "EMI / Loans" to listOf("emi", "loan"),
        "Insurance" to listOf("insurance"),
        "Tax" to listOf("tax"),
        "Medical" to listOf("medical", "doctor", "hospital"),
        "Education" to listOf("school", "college", "education"),
        "Hotel" to listOf("hotel", "stay"),
        "Flight" to listOf("flight", "air ticket"),
        "Salary" to listOf("salary"),
        "Business" to listOf("business"),
        "Freelance" to listOf("freelance"),
        "Investment" to listOf("investment"),
        "Savings" to listOf("saving", "savings"),
        "Shopping" to listOf("shopping", "clothes"),
        "Personal Care" to listOf("salon", "parlour", "personal care"),
        "Entertainment" to listOf("movie", "entertainment"),
        "Subscriptions" to listOf("subscription", "netflix", "prime"),
        "Gifts" to listOf("gift"),
        "Donations" to listOf("donation")
    )

    private val expenseKeywords = setOf("spend", "spent", "expense", "pay", "paid", "payed", "send", "sent", "debit", "debited", "buy", "bought", "purchase", "purchased", "rent", "donate", "donated", "gifted", "lend", "lent", "invest", "invested", "transfer out", "withdraw", "withdrawn")
    private val incomeKeywords = setOf("receive", "received", "get", "got", "earn", "earned", "income", "credit", "credited", "salary", "refund", "repayment", "returned", "sell", "sold", "interest", "dividend", "bonus", "transfer in", "deposit", "deposited")
    private val budgetKeywords = setOf("budget", "limit", "cap")
    private val accountKeywords = setOf("add account", "create account", "new account")

    data class ExtractedData(
        val action: String,
        val type: String?,
        val amount: Long?,
        val category: String,
        val accountType: String?,
        val originalText: String
    )

    fun processInput(text: String) {
        val t = text.lowercase()
        val action = when {
            budgetKeywords.any { t.contains(it) } -> "BUDGET"
            accountKeywords.any { t.contains(it) } -> "ACCOUNT"
            (expenseKeywords + incomeKeywords).any { t.contains(it) } -> "TRANSACTION"
            else -> "UNKNOWN"
        }

        val type = when {
            expenseKeywords.any { t.contains(it) } -> "expense"
            incomeKeywords.any { t.contains(it) } -> "income"
            else -> null
        }

        val amount = Regex("""\b\d+\b""").find(text)?.value?.toLong()

        var foundCategory = "Other"
        for ((category, keywords) in categoryAliases) {
            if (keywords.any { t.contains(it) }) {
                foundCategory = category
                break
            }
        }

        val accountType = when {
            t.contains("upi") || t.contains("gpay") || t.contains("phonepe") -> "UPI"
            t.contains("cash") -> "Cash"
            t.contains("debit") -> "Debit Card"
            t.contains("credit") -> "Credit Card"
            t.contains("bank") || t.contains("account") -> "Bank Account"
            else -> null
        }

        _extractedData.value = ExtractedData(action, type, amount, foundCategory, accountType, text)
    }
}