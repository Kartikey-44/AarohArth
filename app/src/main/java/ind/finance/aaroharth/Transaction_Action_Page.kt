package ind.finance.aaroharth

import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import ind.finance.aaroharth.databinding.ActivityTransactionActionPageBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries.zoneId

class Transaction_Action_Page : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityTransactionActionPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransactionActionPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
        binding.background.setRenderEffect(blur)

        val type=intent.getStringExtra("type")

        if(type=="income"){
            income_ui_changes()
        }
        else{
            expense_ui_changes()
        }

      val time= System.currentTimeMillis()
        val displaytime= Instant.ofEpochMilli(time)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a  dd/MM/yyyy"))

        binding.dateField.setText(displaytime)

        binding.saveButton.setOnClickListener {
            if(isnotempty()){
                save()
            }


        }

    }

    private fun income_ui_changes(){
        binding.heading.text="Add Income"
        binding.subHeading.background=getDrawable(R.drawable.transaction_page_sub_heading_income)
        binding.subHeading.text="Income"
        binding.subHeading.setTextColor(getColor(R.color.subheading_color_income))
        binding.amountLayout.setStartIconDrawable(getDrawable(R.drawable.rupee_income))
        binding.amountLayout.setStartIconTintList(
            ColorStateList.valueOf(getColor(R.color.rupee_symbol_income))
        )
        binding.amountField.setTextColor(getColor(R.color.save_button_background_income))
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_income))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_income)

    }


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
        binding.saveButton.setBackgroundColor(getColor(R.color.save_button_background_expense))
        binding.amountLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.otherPartyLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.categoryLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.dateLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionMediumLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
        binding.transactionWayLayout.boxStrokeColor=getColor(R.color.stroke_color_input_expense)
    }


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

        val otherParty = binding.otherPartyField.text?.toString()?.trim()
        if (otherParty.isNullOrEmpty()) {
            binding.otherPartyLayout.error = "Cannot Be Empty"
            return false
        }

        val category = binding.categoryField.text?.toString()?.trim()
        if (category.isNullOrEmpty()) {
            binding.categoryLayout.error = "Cannot Be Empty"
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

    private fun save(){

    }

}