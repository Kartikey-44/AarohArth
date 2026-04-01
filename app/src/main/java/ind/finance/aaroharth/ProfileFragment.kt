package ind.finance.aaroharth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import ind.finance.aaroharth.authFragments.SignIn
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.FragmentProfileBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding
import ind.finance.aaroharth.viewmodels.ProfileViewModel
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                saveProfileImage(uri)
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

        binding.logoutCardView.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        setupSocialMediaLinks()
        setupDarkModeSwitch()
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) { name ->
            binding.userTextView.text = if (name.isNullOrEmpty()) "User" else name
        }

        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.emailTextView.text = email
        }

        viewModel.profileImagePath.observe(viewLifecycleOwner) { path ->
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    binding.shapeableImageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun saveProfileImage(uri: Uri) {
        val resolver = requireContext().contentResolver
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

        val targetSize = 300
        var scale = 1
        while (options.outWidth / scale > targetSize || options.outHeight / scale > targetSize) { scale *= 2 }

        val scaledOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, scaledOptions) } ?: return
        val file = File(requireContext().filesDir, "profile_image.jpg")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }

        viewModel.updateProfileImage(file.absolutePath)
    }

    private fun showUsernameDialog() {
        val dialogBinding = UsernameDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        
        dialogBinding.nameField.setText(viewModel.username.value)
        dialogBinding.nameField.setSelection(dialogBinding.nameField.text?.length ?: 0)

        dialogBinding.nameField.addTextChangedListener { dialogBinding.nameLayout.error = null }

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
        val isCurrentlyDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isCurrentlyDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("is_dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupSocialMediaLinks() {
        binding.logo1.setOnClickListener { startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@aaroharth.com"))) }
        binding.logo2.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/"))) }
        binding.logo3.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))) }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(requireContext())
        val db = DialogDeleteConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        
        db.save.textSize = 14f
        db.attention.setText(R.string.delete)
        db.dialogLottie.setAnimation("stirict.json")
        db.save.text = "Logout"
        db.skip.text = "Cancel"

        db.save.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), SignIn::class.java))
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
