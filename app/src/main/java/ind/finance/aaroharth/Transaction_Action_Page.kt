package ind.finance.aaroharth

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ind.finance.aaroharth.databinding.ActivityTransactionActionPageBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class Transaction_Action_Page : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionActionPageBinding
    private lateinit var dialogBinding: DialogScreenBinding
    private lateinit var accountDao: Account_Dao
    private lateinit var transactionDao: Transaction_Dao

    private val categoryList = listOf(
        "Mobile Recharge", "FastTag Recharge", "Salary", "Business", "Freelance", "Investment", "Savings", "Food",
        "Dining Out", "Shopping", "Personal Care", "Entertainment", "Subscriptions", "Housing", "Rental", "Utilities",
        "Public Transport", "Petrol", "Diesel", "CNG", "Electricity", "LPG", "PNG", "Taxi", "Auto", "Hotel",
        "Flight", "Medical", "Insurance", "Education", "EMI / Loans", "Tax", "Gifts", "Donations",
        "Miscellaneous", "Water Bill","Grocery","Other"
    )
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Layout Inflated

        binding = ActivityTransactionActionPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.amountLayout.setOnClickListener {
            binding.amountLayout.error=null
        }

        binding.dateLayout.isEnabled=false
        binding.dateField.isEnabled=false
        binding.dateField.isClickable=false
        binding.dateField.isFocusable=false
        binding.dateLayout.isClickable=false

        //Background Blur Effect

        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.S){
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.background.setRenderEffect(blur)
        }

        //Getting Intent Passed from Home Fragment and as per condition of working

        val type = intent.getStringExtra("type") ?: "expense"

        if (type == "income") {
            income_ui_changes()
        } else {
            expense_ui_changes()
        }




        //Initializing DB

        accountDao = App_Database.getInstance(this).accountDao()
        transactionDao= App_Database.getInstance(this).transactionDao()



        //Getting exact date and time of user device

        val time= System.currentTimeMillis()
        val displaytime= Instant.ofEpochMilli(time)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a  dd/MM/yyyy"))


        //Intializing The Transaction Medium Function
        transactionMedium()

        //setting Date and Time To Date Field
        binding.dateField.setText(displaytime)

        //Save Button Working
        binding.saveButton.setOnClickListener {
            if (isnotempty()){
                val amount=binding.amountField.text.toString().trim().toLongOrNull()
                if(amount==null){
                    return@setOnClickListener
                }

                if(type=="expense"){

                    saveExpenseTransaction()
                }
                else{
                    saveIncomeTransaction()
                }
            }
        }


        setupCategoryDropdown()
        catchAssistantIntent()


    }



    // UI CHANGES ON INTENT INCOME

    private fun income_ui_changes(){
        binding.heading.text="Add Income"
        binding.subHeading.background=getDrawable(R.drawable.transaction_page_sub_heading_income)
        binding.subHeading.text="Income"
        binding.subHeading.setTextColor(getColor(R.color.subheading_color_income))
        binding.amountLayout.setStartIconDrawable(getDrawable(R.drawable.rupee_income))
        binding.amountLayout.setStartIconTintList(
            ColorStateList.valueOf(getColor(R.color.rupee_symbol_income))
        )
        binding.otherPartyLayout.hint="Received From"
        binding.amountField.setTextColor(getColor(R.color.save_button_background_income))
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_income))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)

    }


    //UI CHANGES ON INTENT EXPENSE

    private fun expense_ui_changes(){
        binding.heading.text="Add Expense"
        binding.subHeading.background=getDrawable(R.drawable.transaction_page_sub_heading_expense)
        binding.subHeading.text="Expense"
        binding.subHeading.setTextColor(getColor(R.color.subheading_color_expense))
        binding.amountLayout.setStartIconDrawable(getDrawable(R.drawable.rupee_expense))
        binding.amountLayout.setStartIconTintList(
            ColorStateList.valueOf(getColor(R.color.rupee_symbol_expense))
        )
        binding.amountField.setTextColor(getColor(R.color.save_button_background_expense))
        binding.otherPartyLayout.hint="Paid To"
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_expense))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
    }



    private fun setupCategoryDropdown() {



        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categoryList
        )

        binding.categoryField.apply {
            setAdapter(adapter)
            threshold = 0

            setOnClickListener { showDropDown() }

            setOnItemClickListener { parent, _, position, _ ->
                setText(parent.getItemAtPosition(position).toString(), false)
            }
        }
    }


    //Drop Down Menu Item Declaration For Transaction Medium

    private fun transactionMedium() {



        lifecycleScope.launch {
            val mediumList: List<String> = accountDao.getAllAccountType()

            val adapter = ArrayAdapter(
                this@Transaction_Action_Page,
                android.R.layout.simple_list_item_1,
                mediumList
            )
            binding.transactionMediumField.setAdapter(adapter)
        }
        binding.transactionMediumField.setOnItemClickListener{parent,_,position,_ ->
            binding.transactionMediumField.setText(parent.getItemAtPosition(position).toString(),false)
            transactionWay(binding.transactionMediumField.text.toString())
            binding.transactionWayField.text.clear()
        }
    }

    //Setting Drop Down For Transaction Way

    private fun transactionWay(type: String){

        lifecycleScope.launch {
            val wayList:List<String> = accountDao.getAllAccountName(type)
            val adapter= ArrayAdapter(this@Transaction_Action_Page,
                android.R.layout.simple_list_item_1,
                wayList
            )
            binding.transactionWayField.setAdapter(adapter)
        }
        binding.transactionWayField.setOnItemClickListener{parent,_,position,_ ->
            binding.transactionWayField.setText(binding.transactionWayField.text.toString(),false)
        }

    }


    //Checking that Any Field Is Empty Or Not

    private fun isnotempty(): Boolean {

        binding.amountLayout.error = null
        binding.otherPartyLayout.error = null
        binding.categoryLayout.error = null
        binding.transactionMediumLayout.error = null
        binding.transactionWayLayout.error = null

        val amount = binding.amountField.text?.toString()?.trim()
        if (amount.isNullOrEmpty()) {
            binding.amountLayout.error = "Cannot Be Empty"
            return false
        }
        val category = binding.categoryField.text?.toString()?.trim()
        if (category.isNullOrEmpty()) {
            binding.categoryLayout.error = "Cannot Be Empty"
            return false
        }
        if(category !in categoryList){
            binding.categoryLayout.error="Select From List"
            return false
        }
        val transactionMedium = binding.transactionMediumField.text?.toString()?.trim()
        if (transactionMedium.isNullOrEmpty()) {
            binding.transactionMediumLayout.error = "Cannot Be Empty"
            return false
        }
        val transactionWay = binding.transactionWayField.text?.toString()?.trim()
        if (transactionWay.isNullOrEmpty()) {
            binding.transactionWayLayout.error = "Cannot Be Empty"
            return false
        }
        return true
    }

    //INCOME SAVE FUN

    private fun saveIncomeTransaction() {

        val amount = binding.amountField.text.toString().trim().toLongOrNull()
            ?: return

        val transactionWay = binding.transactionWayField.text.toString().trim()
        var otherparty: String=binding.otherPartyField.text.toString().trim()

        if(otherparty.isEmpty()){
            otherparty="Unknown"
        }
        val now= System.currentTimeMillis()
        val monthKey= SimpleDateFormat("yyyy-MM", Locale.US)
            .format(Date(now))


        lifecycleScope.launch(Dispatchers.IO) {

            val balance = accountDao.getbalance(transactionWay)


            accountDao.updatebalance(balance + amount, transactionWay)


            transactionDao.insertTransaction(
                Transaction_Info(
                    0,
                    binding.subHeading.text.toString().trim(),
                    amount,
                    otherparty,
                    binding.categoryField.text.toString().trim(),
                    now,
                    binding.transactionMediumField.text.toString().trim(),
                    transactionWay,
                    binding.remarkField.text.toString().trim(),
                    0.00,
                    0.00,
                    "None",
                    monthKey


                )
            )

            withContext(Dispatchers.Main) {
                successLottie("Income Saved")
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1800)
            }
        }
    }



    // Save Expense Transaction Function

    private fun saveExpenseTransaction() {

        val amount = binding.amountField.text.toString().trim().toLongOrNull() ?: return

        val transactionWay = binding.transactionWayField.text.toString().trim()

        var otherparty=binding.otherPartyField.text.toString().trim()
        if(otherparty.isEmpty()){
            otherparty="Unknown"
        }
        val now= System.currentTimeMillis()
        val monthKey= SimpleDateFormat("yyyy-MM", Locale.US)
            .format(Date(now))

        lifecycleScope.launch(Dispatchers.IO) {

            val balance = accountDao.getbalance(transactionWay)

            if (balance < amount) {
                withContext(Dispatchers.Main) {
                    binding.amountLayout.error = "Insufficient Balance"
                }
                return@launch
            }


            accountDao.updatebalance(balance - amount, transactionWay)

            val category = binding.categoryField.text.toString().trim().lowercase()
            val factor=carbonEmissionFactor(category)
            val emitted=carbonEmitted(amount,factor)
            val auth=carbonEmissionAuth(emitted)


            transactionDao.insertTransaction(
                Transaction_Info(
                    0,
                    binding.subHeading.text.toString().trim(),
                    amount,
                    binding.otherPartyField.text.toString().trim()?:"Unknown",
                    binding.categoryField.text.toString().trim(),
                    now,
                    binding.transactionMediumField.text.toString().trim(),
                    transactionWay,
                    binding.remarkField.text.toString().trim(),
                   factor,
                    emitted,
                    auth,
                    monthKey
                )
            )

            withContext(Dispatchers.Main) {
                successLottie("Expense Saved")
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                },1800)
            }
        }
    }


    private fun successLottie(message: String) {
        val dialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.setCancelable(false)

        dialogBinding.dialogLottie.setAnimation("Success.json")
        dialogBinding.message.text = message
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)
    }


    private fun failedLottie(){dialog("Failed.json","Something Went Wrong")}
    private fun dialog(lottie: String, message: String){
        val dialog= Dialog(this)
        dialogBinding=DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialogBinding.dialogLottie.setAnimation(lottie)
        dialogBinding.message.text=message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        },1800
        )


    }

    private fun carbonEmissionFactor(categoryName: String): Double{

        return when (categoryName.lowercase().trim()) {

            // ---------- DAILY CONSUMPTION (PROXY VALUES) ----------
            // Relative carbon-impact weights (NOT emission factors)

            "food" -> 0.0004
            "grocery" -> 0.0005
            "dining out" -> 0.0006
            "shopping" -> 0.0006
            "personal care" -> 0.0005
            "entertainment" -> 0.0004
            "subscriptions" -> 0.0003
            "medical" -> 0.0003
            "education" -> 0.0003
            "gifts" -> 0.0005
            "miscellaneous" -> 0.0004
            "housing" -> 0.0004
            "utilities" -> 0.0005
            "water bill" -> 0.0002

            // ---------- TRANSPORT (HIGHER IMPACT PROXIES) ----------

            "public transport" -> 0.0004
            "auto" -> 0.0007
            "taxi" -> 0.0008
            "hotel" -> 0.0010
            "flight" -> 0.0012   // conservative, awareness-based

            // ---------- ENERGY / FUEL (DERIVED + NORMALIZED) ----------

            "electricity" -> 0.0010
            "petrol" -> 0.0016
            "diesel" -> 0.0018
            "cng" -> 0.0013
            "lpg" -> 0.0015
            "png" -> 0.0012

            // ---------- FINANCIAL FLOWS (NO DIRECT EMISSIONS) ----------

            "emi / loans", "emi/loans" -> 0.0
            "rental" -> 0.0
            "mobile recharge", "fasttag recharge", "recharge" -> 0.0002

            else -> 0.0
        }


    }
    private fun carbonEmissionAuth(emitted: Double): String{
        return when{
            emitted<0.20 ->"Low"
            emitted<1.00 ->"Medium"
            else ->"High"

        }
    }

    private fun carbonEmitted(amount: Long, factor: Double):Double{
        return amount*factor

    }

    private fun catchAssistantIntent() {

        val amount = intent.getLongExtra("amount", -1L)
        val category = intent.getStringExtra("category")
        val accountType = intent.getStringExtra("accountType")
        val note = intent.getStringExtra("note")

        // 1. Amount
        if (amount > 0) {
            binding.amountField.setText(amount.toString())
        }

        // 2. Category (must be from list)
        if (!category.isNullOrBlank() && category in categoryList) {
            binding.categoryField.setText(category, false)
        }

        // 3. Transaction Medium (Account Type)
        if (!accountType.isNullOrBlank()) {
            binding.transactionMediumField.setText(accountType, false)

            // load corresponding account names automatically
            transactionWay(accountType)
        }

        // 4. Original AI text as remark/note
        if (!note.isNullOrBlank()) {
            binding.remarkField.setText(note)
        }
    }



}