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
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomMenu) { view, insets ->
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

        changeFragment(HomeFragement())
        menuclick(binding.home)

        binding.home.setOnClickListener {
            menuclick(binding.home)
            changeFragment(HomeFragement())
        }
        binding.categories.setOnClickListener {
            menuclick(binding.categories)
            changeFragment(CategoriesFragment())
        }
        binding.carbonTracker.setOnClickListener {
            menuclick(binding.carbonTracker)
            changeFragment(CarbonFragment())
        }
        binding.dashboard.setOnClickListener {
            menuclick(binding.dashboard)
            changeFragment(DashboardFragment())
        }
        binding.profile.setOnClickListener {
            menuclick(binding.profile)
            changeFragment(ProfileFragment())
        }


    }
    private fun menuclick(view: ImageView){
        listOf(binding.home,binding.categories,binding.carbonTracker,binding.dashboard,binding.profile)
            .forEach { it.isSelected=false }
        view.isSelected=true
    }

    private fun changeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }
}