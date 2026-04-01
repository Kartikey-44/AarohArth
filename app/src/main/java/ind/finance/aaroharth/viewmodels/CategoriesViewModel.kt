package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ind.finance.aaroharth.R
import ind.finance.aaroharth.data.model.CategoriesDataClass

class CategoriesViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<CategoriesDataClass>>()
    val categories: LiveData<List<CategoriesDataClass>> = _categories

    private val _showingIncome = MutableLiveData<Boolean?>(null)
    val showingIncome: LiveData<Boolean?> = _showingIncome

    private val fullList = mutableListOf<CategoriesDataClass>()

    init {
        loadInitialData()
        _categories.value = fullList
    }

    private fun loadInitialData() {
        fullList.add(CategoriesDataClass(R.drawable.rupee, "Salary"))
        fullList.add(CategoriesDataClass(R.drawable.freelance, "Freelance"))
        fullList.add(CategoriesDataClass(R.drawable.business, "Business"))
        fullList.add(CategoriesDataClass(R.drawable.rental, "Rental"))
        fullList.add(CategoriesDataClass(R.drawable.upiicon, "Upi"))
        fullList.add(CategoriesDataClass(R.drawable.investment, "Investment"))
        fullList.add(CategoriesDataClass(R.drawable.housing, "Housing"))
        fullList.add(CategoriesDataClass(R.drawable.utilities, "Recharge"))
        fullList.add(CategoriesDataClass(R.drawable.food, "Food"))
        fullList.add(CategoriesDataClass(R.drawable.entertainment, "Entertainment"))
        fullList.add(CategoriesDataClass(R.drawable.shopping, "Shopping"))
        fullList.add(CategoriesDataClass(R.drawable.tax, "Taxes"))
        fullList.add(CategoriesDataClass(R.drawable.medical, "Medical"))
        fullList.add(CategoriesDataClass(R.drawable.education, "Education"))
        fullList.add(CategoriesDataClass(R.drawable.insurance, "Insurance"))
        fullList.add(CategoriesDataClass(R.drawable.savings, "Savings"))
        fullList.add(CategoriesDataClass(R.drawable.taxi, "Taxi"))
        fullList.add(CategoriesDataClass(R.drawable.auto, "Auto"))
        fullList.add(CategoriesDataClass(R.drawable.hotel, "Hotel"))
        fullList.add(CategoriesDataClass(R.drawable.flight, "Flight"))
        fullList.add(CategoriesDataClass(R.drawable.petrol, "Petrol"))
        fullList.add(CategoriesDataClass(R.drawable.diesel, "Diesel"))
        fullList.add(CategoriesDataClass(R.drawable.cng, "CNG"))
        fullList.add(CategoriesDataClass(R.drawable.lpgpng, "LPG"))
        fullList.add(CategoriesDataClass(R.drawable.publictransport, "Public Transport"))
        fullList.add(CategoriesDataClass(R.drawable.electricity, "Electricity"))
        fullList.add(CategoriesDataClass(R.drawable.waterbill, "Water Bill"))
        fullList.add(CategoriesDataClass(R.drawable.dining_out, "Dining Out"))
        fullList.add(CategoriesDataClass(R.drawable.personalcare, "Personal Care"))
        fullList.add(CategoriesDataClass(R.drawable.subscription, "Subscriptions"))
        fullList.add(CategoriesDataClass(R.drawable.gift, "Gifts"))
        fullList.add(CategoriesDataClass(R.drawable.donation, "Donations"))
        fullList.add(CategoriesDataClass(R.drawable.miscellaneous, "Miscellaneous"))
        fullList.add(CategoriesDataClass(R.drawable.other, "Other"))
        fullList.add(CategoriesDataClass(R.drawable.recharge, "Mobile Recharge"))
        fullList.add(CategoriesDataClass(R.drawable.fastag, "FastTag Recharge"))
        fullList.add(CategoriesDataClass(R.drawable.loan, "EMI / Loans"))
        fullList.add(CategoriesDataClass(R.drawable.decoration, "Decoration"))
        fullList.add(CategoriesDataClass(R.drawable.grocery, "Grocery"))
    }

    fun filterCategories(query: String?) {
        val q = query.orEmpty().lowercase().trim()
        if (q.isEmpty()) {
            _categories.value = fullList
        } else {
            _categories.value = fullList
                .filter { it.name.lowercase().startsWith(q) }
                .sortedBy { it.name.lowercase() }
        }
    }

    fun setIncomeMode() {
        _showingIncome.value = true
    }

    fun setExpenseMode() {
        _showingIncome.value = false
    }
}