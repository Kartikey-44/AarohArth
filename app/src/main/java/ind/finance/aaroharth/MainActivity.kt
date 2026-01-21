package ind.finance.aaroharth

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import ind.finance.aaroharth.databinding.ActivityMainBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding
import ind.finance.aaroharth.databinding.UsernameGreetBinding

class MainActivity : BaseSecureActivity() {

    private lateinit var binding: ActivityMainBinding
    private var activeDialog: Dialog? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("dark_mode_prefs", MODE_PRIVATE)

        if (!prefs.contains("is_dark_mode")) {
            // First launch → follow device theme
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        } else {
            val isDarkMode = prefs.getBoolean("is_dark_mode", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode)
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
        decideStartFlow()

        if (savedInstanceState == null) {
            changeFragment(HomeFragement())
        }

        //changeFragment(HomeFragement())
    }

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

    private fun setupBottomNav() {
        binding.bottomNavigationBar.setOnItemSelectedListener {
            changeFragment(
                when (it.itemId) {
                    R.id.categories -> CategoriesFragment()
                    R.id.carbon_tracker -> CarbonFragment()
                    R.id.dashboard -> DashboardFragment()
                    R.id.profile -> ProfileFragment()
                    else -> HomeFragement()
                }
            )
            true
        }
    }


    private fun decideStartFlow() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // 1. Must be logged in
        if (!prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, SignIn::class.java))
            finish()
            return
        }

        // 2. Account setup is mandatory
        if (!prefs.getBoolean("has_account", false)) {
            startActivity(Intent(this, AccountActions::class.java))
            finish()
            return
        }

        // 3. Ask name only if not set AND not skipped
        if (!prefs.contains("username") &&
            !prefs.getBoolean("name_skipped", false)
        ) {
            showNameDialog()
            return
        }

        // 4. Normal home
        loadHome()
    }

    private fun loadHome() {
        binding.bottomNavigationBar.selectedItemId = R.id.home
        changeFragment(HomeFragement())
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showNameDialog() {
        val dialogBinding = UsernameDialogBinding.inflate(layoutInflater)
        activeDialog = Dialog(this)

        activeDialog!!.setContentView(dialogBinding.root)
        activeDialog!!.setCancelable(false)
        activeDialog!!.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )
        activeDialog!!.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        activeDialog!!.show()

        dialogBinding.nameField.addTextChangedListener {
            dialogBinding.nameLayout.error = null
        }

        dialogBinding.save.setOnClickListener {
            val name = dialogBinding.nameField.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.nameLayout.error = "Required"
                return@setOnClickListener
            }

            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("username", name)
                .remove("name_skipped")
                .apply()

            activeDialog!!.dismiss()
            showGreeting(name)
        }

        dialogBinding.skip.setOnClickListener {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("name_skipped", true)
                .apply()

            activeDialog!!.dismiss()
            decideStartFlow() // 🔥 NOT loadHome()
        }

    }

    private fun showGreeting(name: String) {
        val dialogBinding = UsernameGreetBinding.inflate(layoutInflater)
        activeDialog = Dialog(this)

        activeDialog!!.setContentView(dialogBinding.root)
        activeDialog!!.window?.setBackgroundDrawable(
            getDrawable(R.drawable.dialog_background)
        )
        activeDialog!!.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.message.text = "Welcome\n$name"
        dialogBinding.dialogLottie.setAnimation("Success.json")

        activeDialog!!.show()

        Handler(Looper.getMainLooper()).postDelayed({
            activeDialog!!.dismiss()
            decideStartFlow()
        }, 1800)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        super.onDestroy()
    }
}
