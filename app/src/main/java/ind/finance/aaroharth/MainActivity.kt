package ind.finance.aaroharth

import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import ind.finance.aaroharth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationBar) { view, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
            ).bottom

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                bottomInset
            )
            insets
        }

        enableEdgeToEdge()

        //      Change screen or fragment
        binding.bottomNavigationBar.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> changeFragment(HomeFragement())
                R.id.categories -> changeFragment(CategoriesFragment())
                R.id.carbon_tracker -> changeFragment(CarbonFragment())
                R.id.dashboard -> changeFragment(DashboardFragment())
                R.id.profile -> changeFragment(ProfileFragment())
            }
            true
        }
        binding.bottomNavigationBar.selectedItemId = R.id.home
    }

    private fun changeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }
}