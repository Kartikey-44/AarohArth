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
import androidx.lifecycle.ViewModelStore
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.FragmentProfileBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ---------------- IMAGE PICKER ----------------

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                topInset,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }


        loadUsername()
        loadEmail()
        setupDarkModeSwitch()
        setupSocialMediaLinks()
        loadProfileImage()
        binding.shapeableImageView.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.userTextView.setOnClickListener {
            showUsernameDialog()
        }

        binding.logoutCardView.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    private fun loadProfileImage() {
        val path = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("profile_image_path", null)

        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.shapeableImageView.setImageBitmap(bitmap)
            }
        }
    }


    // ---------------- PROFILE IMAGE ----------------
    private fun saveProfileImage(uri: Uri) {
        val resolver = requireContext().contentResolver

        // 1. Get image bounds only
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // 2. Calculate scale (target ~300px)
        val targetSize = 300
        var scale = 1
        while (options.outWidth / scale > targetSize || options.outHeight / scale > targetSize) {
            scale *= 2
        }

        // 3. Decode scaled bitmap
        val scaledOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, scaledOptions)
        } ?: return

        // 4. Save compressed bitmap
        val file = File(requireContext().filesDir, "profile_image.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        // 5. Save path
        requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("profile_image_path", file.absolutePath)
            .apply()

        // 6. Display safely
        binding.shapeableImageView.setImageBitmap(bitmap)
    }


    // ---------------- USERNAME ----------------

    private fun loadUsername() {
        val prefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val name = prefs.getString("username", null)
        if (!name.isNullOrEmpty()) {
            binding.userTextView.text = name
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
        dialog.show()

        val prefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val currentName = prefs.getString("username", null)
        if (!currentName.isNullOrEmpty()) {
            dialogBinding.nameField.setText(currentName)
            dialogBinding.nameField.setSelection(currentName.length)
        }

        dialogBinding.nameField.addTextChangedListener {
            dialogBinding.nameLayout.error = null
        }

        dialogBinding.save.setOnClickListener {
            val name = dialogBinding.nameField.text.toString().trim()

            if (name.isEmpty()) {
                dialogBinding.nameLayout.error = "Required"
                return@setOnClickListener
            }

            prefs.edit()
                .putString("username", name)
                .apply()

            binding.userTextView.text = name
            dialog.dismiss()
            Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.skip.setOnClickListener {
            dialog.dismiss()
        }
    }

    // ---------------- EMAIL ----------------

    private fun loadEmail() {
        val email = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("email", null)

        if (!email.isNullOrEmpty()) {
            binding.emailTextView.text = email
        }
    }

    // ---------------- DARK MODE ----------------
    private fun setupDarkModeSwitch() {

        val prefs = requireContext()
            .getSharedPreferences("dark_mode_prefs", Context.MODE_PRIVATE)

        val currentNightMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val isCurrentlyDark = currentNightMode ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        // ✅ Sync switch with REAL mode
        binding.switchDarkMode.isChecked = isCurrentlyDark

        // ✅ Persist it once (important for first install)
        prefs.edit()
            .putBoolean("is_dark_mode", isCurrentlyDark)
            .apply()

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("is_dark_mode", isChecked)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    // ---------------- SOCIAL LINKS ----------------

    private fun setupSocialMediaLinks() {

        binding.logo1.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@aaroharth.com"))
            )
        }

        binding.logo2.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/")))
        }

        binding.logo3.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/")))
        }
    }

    // ---------------- LOGOUT / DELETE ----------------

    private fun showDeleteConfirmationDialog() {
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
        db.save.textSize=14f

        db.attention.setText(R.string.delete)
        db.dialogLottie.setAnimation("stirict.json")
        db.save.text = "Logout"
        db.skip.text = "Cancel"

        db.save.setOnClickListener {
            requireContext()
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", false)
                .apply()

            startActivity(Intent(requireContext(), SignIn::class.java))
            requireActivity().finish()
        }

        db.skip.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
