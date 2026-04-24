package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AI_ViewModel : ViewModel() {

    private val _extractedData = MutableLiveData<ExtractedData>()
    val extractedData: LiveData<ExtractedData> = _extractedData


    private val categoryAliases = mapOf(
        "Rental" to listOf("rent","rental","house rent","flat rent"),
        "Food" to listOf("food","meal","lunch","dinner"),
        "Dining Out" to listOf("restaurant","dining","hotel food"),
        "Grocery" to listOf("grocery","groceries","ration"),
        "Petrol" to listOf("petrol","fuel"),
        "Diesel" to listOf("diesel"),
        "CNG" to listOf("cng"),
        "Taxi" to listOf("taxi","cab","uber","ola"),
        "Auto" to listOf("auto","rickshaw"),
        "Public Transport" to listOf("bus","metro","train"),
        "Electricity" to listOf("electricity","light bill","power bill"),
        "Water Bill" to listOf("water","water bill"),
        "LPG" to listOf("lpg","gas"),
        "PNG" to listOf("png"),
        "Mobile Recharge" to listOf("mobile recharge","recharge"),
        "FastTag Recharge" to listOf("fastag","fasttag"),
        "EMI / Loans" to listOf("emi","loan"),
        "Insurance" to listOf("insurance"),
        "Tax" to listOf("tax"),
        "Medical" to listOf("medical","doctor","hospital"),
        "Education" to listOf("school","college","education"),
        "Hotel" to listOf("hotel","stay"),
        "Flight" to listOf("flight","air ticket"),
        "Salary" to listOf("salary"),
        "Business" to listOf("business"),
        "Freelance" to listOf("freelance"),
        "Investment" to listOf("investment"),
        "Savings" to listOf("saving","savings"),
        "Shopping" to listOf("shopping","clothes"),
        "Personal Care" to listOf("salon","parlour","personal care"),
        "Entertainment" to listOf("movie","entertainment"),
        "Subscriptions" to listOf("subscription","netflix","prime"),
        "Gifts" to listOf("gift"),
        "Donations" to listOf("donation")
    )


    private val expenseKeywords = setOf(
        "spend","spent","expense","pay","paid","buy","purchase",
        "rent","donate","lend","withdraw","transfer out"
    )

    private val incomeKeywords = setOf(
        "receive","received","earn","salary","refund",
        "credit","bonus","interest","deposit","transfer in"
    )

    private val budgetKeywords = setOf("budget","limit","cap")

    private val accountKeywords = setOf(
        "add account","create account","new account"
    )


    data class ExtractedData(
        val action: String,
        val type: String?,
        val amount: Long?,
        val category: String,
        val otherParty: String?,
        val transactionWay: String?,
        val accountName: String?,
        val transactionMedium: String?,
        val monthKey: String?,
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


        val amount = Regex("""\b\d+\b""")
            .find(text.replace(",", ""))
            ?.value
            ?.toLong()


        val otherParty = Regex("""(?:to|from|paid|received)\s+([A-Za-z]+)""")
            .find(t)
            ?.groupValues
            ?.getOrNull(1)
            ?.replaceFirstChar { it.uppercase() }


        val transactionWay = Regex(
            """(?:using|via|through)?\s*(gpay|phonepe|paytm|cash|hdfc|sbi|icici|axis)"""
        )
            .find(t)
            ?.groupValues
            ?.getOrNull(1)
            ?.replaceFirstChar { it.uppercase() }


        val accountName = transactionWay


        val transactionMedium = when (transactionWay) {
            "Gpay","Phonepe","Paytm" -> "UPI"
            "Hdfc","Sbi","Icici","Axis" -> "Bank Account"
            "Cash" -> "Cash"
            else -> if (t.contains("upi")) "UPI" else null
        }


        val monthKey =
            if (t.contains("this month"))
                currentMonthKey()
            else null


        var foundCategory = "Other"

        for ((category, keywords) in categoryAliases.entries.sortedByDescending { it.value.size }) {
            if (keywords.any { t.contains(it) }) {
                foundCategory = category
                break
            }
        }


        _extractedData.value = ExtractedData(
            action,
            type,
            amount,
            foundCategory,
            otherParty,
            transactionWay,
            accountName,
            transactionMedium,
            monthKey,
            text
        )
    }


    private fun currentMonthKey(): String {

        val cal = Calendar.getInstance()

        return SimpleDateFormat(
            "MM-yyyy",
            Locale.getDefault()
        ).format(cal.time)
    }
}