package ind.finance.aaroharth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import ind.finance.aaroharth.databinding.FragmentProfileBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUsername()
        setupDarkModeSwitch()
        setupProfileMenu()
        setupSocialMediaLinks()
    }

    private fun setupSocialMediaLinks() {
        // Email
        binding.logo1.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@aaroharth.com")
                putExtra(Intent.EXTRA_SUBJECT, "AarohArth Support")
                putExtra(Intent.EXTRA_TEXT, "Hi AarohArth Team,\n\n")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        // Discord
        binding.logo2.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/"))
            startActivity(intent)
        }

        // GitHub
        binding.logo3.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
            startActivity(intent)
        }
    }

    private fun setupProfileMenu() {
        binding.profileMenu.setOnClickListener { view ->
            showProfilePopup(view)
        }
    }

    private fun showProfilePopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_profile -> {
                    Toast.makeText(requireContext(), "Edit Profile", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.change_username -> {
                    showChangeUsernameDialog()
                    true
                }
                else -> false
            }
        }
        try {
            val popupField = PopupMenu::class.java.getDeclaredField("mPopup")
            popupField.isAccessible = true
            val popupWindow = popupField.get(popup) as android.widget.PopupWindow

            val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            popupWindow.width = width
            popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT

            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.icon_container_bg)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        popup.show()
    }

    private fun showChangeUsernameDialog() {
        val dialogBinding = UsernameDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(
            requireContext().getDrawable(R.drawable.dialog_background)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        val currentName = getUserName(requireContext())
        if (currentName != null) {
            dialogBinding.nameField.setText(currentName)
            dialogBinding.nameField.setSelection(currentName.length)
        }

        dialogBinding.nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                dialogBinding.nameLayout.error = null
            }
        })

        dialogBinding.save.setOnClickListener {
            val name = dialogBinding.nameField.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.nameLayout.error = "Required"
                return@setOnClickListener
            }

            requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("username", name)
                .apply()

            binding.userTextView.text = name

            dialog.dismiss()
            Toast.makeText(requireContext(), "Username updated!", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.skip.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun loadUsername() {
        if (hasUserName(requireContext())) {
            val name = getUserName(requireContext())!!
            binding.userTextView.text = name

            // Email To Update From FireBase
            binding.emailTextView.text = "$name@aaroharth.com"
        }
    }

    private fun setupDarkModeSwitch() {
        val sharedPrefs = requireContext()
            .getSharedPreferences("dark_mode_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("is_dark_mode", false)

        binding.switchDarkMode.isChecked = isDarkMode

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit()
                .putBoolean("is_dark_mode", isChecked)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun getUserName(context: Context): String? =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("username", null)

    private fun hasUserName(context: Context): Boolean =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .contains("username")

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
