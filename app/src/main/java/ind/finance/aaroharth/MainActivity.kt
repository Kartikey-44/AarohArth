package ind.finance.aaroharth

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import ind.finance.aaroharth.add_delete_edit_Fragments.AccountActions
import ind.finance.aaroharth.authFragments.SignIn
import ind.finance.aaroharth.carbonFragments.CarbonFragment
import ind.finance.aaroharth.categoriesFragments.CategoriesFragment
import ind.finance.aaroharth.dashboardFragments.DashboardFragment
import ind.finance.aaroharth.databinding.ActivityMainBinding

class MainActivity : BaseSecureActivity() {

    private lateinit var binding: ActivityMainBinding
    private var activeDialog: Dialog? = null

    // ✅ SINGLE instances (VERY IMPORTANT)
    private val homeFragment = HomeFragement()
    private val dashboardFragment = DashboardFragment()
    private val categoriesFragment = CategoriesFragment()
    private val carbonFragment = CarbonFragment()
    private val profileFragment = ProfileFragment()

    private var selectedItemId = R.id.home

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        // ---------- Theme handling ----------
        val themePrefs = getSharedPreferences("dark_mode_prefs", MODE_PRIVATE)
        if (!themePrefs.contains("is_dark_mode")) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                if (themePrefs.getBoolean("is_dark_mode", false))
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupBottomNav()
        setupBackHandling()

        // ---------- Cold start only ----------
        if (savedInstanceState == null) {
            decideStartFlow()
        } else {
            selectedItemId =
                savedInstanceState.getInt("selected_item", R.id.home)
            binding.bottomNavigationBar.selectedItemId = selectedItemId
        }
    }

    // ---------- Insets ----------
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationBar) { view, insets ->
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }
    }

    // ---------- Bottom Navigation ----------
    private fun setupBottomNav() {
        binding.bottomNavigationBar.setOnItemSelectedListener { item ->

            val current =
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            val target: Fragment = when (item.itemId) {
                R.id.home -> homeFragment
                R.id.dashboard -> dashboardFragment
                R.id.categories -> categoriesFragment
                R.id.carbon_tracker -> carbonFragment
                R.id.profile -> profileFragment
                else -> homeFragment
            }

            // 🚫 DO NOTHING if same fragment
            if (current === target) return@setOnItemSelectedListener true

            selectedItemId = item.itemId

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, target)
                .commit()

            true
        }
    }

    // ---------- Back Handling ----------
    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this) {

            // 1. Close dialog if open
            if (activeDialog?.isShowing == true) {
                activeDialog?.dismiss()
                return@addCallback
            }

            val current =
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            // 2. If Home → exit
            if (current === homeFragment) {
                finish()
                return@addCallback
            }

            // 3. Otherwise → go to Home
            binding.bottomNavigationBar.selectedItemId = R.id.home
        }
    }

    // ---------- App Entry Flow ----------
    private fun decideStartFlow() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, SignIn::class.java))
            finish()
            return
        }

        if (!prefs.getBoolean("has_account", false)) {
            startActivity(Intent(this, AccountActions::class.java))
            finish()
            return
        }

        // Normal entry
        binding.bottomNavigationBar.selectedItemId = R.id.home
    }

    // ---------- State ----------
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selected_item", selectedItemId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        super.onDestroy()
    }
}
