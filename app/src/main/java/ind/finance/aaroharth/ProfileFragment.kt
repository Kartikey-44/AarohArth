package ind.finance.aaroharth

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import ind.finance.aaroharth.authFragments.SignIn
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.FragmentProfileBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding
import ind.finance.aaroharth.notifications.NotificationPrefs
import ind.finance.aaroharth.notifications.NotificationScheduler
import ind.finance.aaroharth.viewmodels.ProfileViewModel


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val context = requireContext()
            if (isGranted) {
                NotificationPrefs.setNotificationsEnabled(context, true)
                NotificationScheduler.enableNotifications(context)
                binding.notificationToggle.isChecked = true
                Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                NotificationPrefs.setNotificationsEnabled(context, false)
                NotificationScheduler.disableNotifications(context)
                binding.notificationToggle.isChecked = false
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfileFromCloud()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        observeViewModel()

        binding.shapeableImageView.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.userTextView.setOnClickListener {
            showUsernameDialog()
        }

        binding.backupCardView.setOnClickListener {
            viewModel.backupProfileNow()
            Toast.makeText(requireContext(), "Profile backed up to cloud", Toast.LENGTH_SHORT).show()
        }

        binding.restoreCardView.setOnClickListener {
            viewModel.loadProfileFromCloud()
            Toast.makeText(requireContext(), "Profile restored from cloud", Toast.LENGTH_SHORT).show()
        }

        binding.logoutCardView.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        setupSocialMediaLinks()
        setupDarkModeSwitch()
        setupNotificationSwitch()
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) { name ->
            binding.userTextView.text = if (name.isNullOrEmpty()) "User" else name
        }

        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.emailTextView.text = email
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.user_png)
                .error(R.drawable.user_png)
                .circleCrop()
                .into(binding.shapeableImageView)
        }
    }

    private fun showUsernameDialog() {
        val dialogBinding = UsernameDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.nameField.setText(viewModel.username.value)
        dialogBinding.nameField.setSelection(dialogBinding.nameField.text?.length ?: 0)

        dialogBinding.nameField.addTextChangedListener {
            dialogBinding.nameLayout.error = null
        }

        dialogBinding.save.setOnClickListener {
            val name = dialogBinding.nameField.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.nameLayout.error = "Required"
                return@setOnClickListener
            }
            viewModel.updateUsername(name)
            dialog.dismiss()
            Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupDarkModeSwitch() {
        val prefs = requireContext().getSharedPreferences("dark_mode_prefs", Context.MODE_PRIVATE)
        val isCurrentlyDark =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

        binding.switchDarkMode.isChecked = isCurrentlyDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("is_dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupNotificationSwitch() {
        val context = requireContext()

        binding.notificationToggle.isChecked =
            NotificationPrefs.areNotificationsEnabled(context)

        binding.notificationToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableNotifications()
            } else {
                NotificationPrefs.setNotificationsEnabled(context, false)
                NotificationScheduler.disableNotifications(context)
                Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }

        //Test Notification Press Toggle
        binding.notificationToggle.setOnLongClickListener {
            NotificationScheduler.testNotificationIn10Seconds(requireContext())
            Toast.makeText(requireContext(), "Test notification will come in 10 seconds", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun enableNotifications() {
        val context = requireContext()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationPrefs.setNotificationsEnabled(context, true)
                NotificationScheduler.enableNotifications(context)
                Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            NotificationPrefs.setNotificationsEnabled(context, true)
            NotificationScheduler.enableNotifications(context)
            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSocialMediaLinks() {
        binding.logo1.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@aaroharth.com")))
        }
        binding.logo2.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/")))
        }
        binding.logo3.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/")))
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(requireContext())
        val db = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        db.save.textSize = 14f
        db.attention.text = "Are you sure you want to log out?"
        db.dialogLottie.setAnimation("stirict.json")
        db.save.text = "Logout"
        db.skip.text = "Cancel"

        db.save.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), SignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        db.skip.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}