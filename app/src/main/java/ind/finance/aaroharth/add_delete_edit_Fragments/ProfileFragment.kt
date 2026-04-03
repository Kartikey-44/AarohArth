package ind.finance.aaroharth.add_delete_edit_Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import ind.finance.aaroharth.R
import ind.finance.aaroharth.authFragments.SignIn
import ind.finance.aaroharth.databinding.DialogDeleteConfirmationBinding
import ind.finance.aaroharth.databinding.FragmentProfileBinding
import ind.finance.aaroharth.databinding.UsernameDialogBinding
import ind.finance.aaroharth.viewmodels.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.updateProfileImage(it) }
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
        observeViewModel()

        binding.shapeableImageView.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.userTextView.setOnClickListener {
            showUsernameDialog()
        }

        binding.logoutCardView.setOnClickListener {
            showLogoutDialog()
        }

        binding.backupCardView.setOnClickListener {
            viewModel.backupProfileNow()
            Toast.makeText(
                requireContext(),
                "Profile backed up successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        setupDarkModeSwitch()
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userTextView.text = it
        }

        viewModel.email.observe(viewLifecycleOwner) {
            binding.emailTextView.text = it
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.user_png)
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

        dialogBinding.save.setOnClickListener {
            val name = dialogBinding.nameField.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener
            viewModel.updateUsername(name)
            dialog.dismiss()
        }

        dialogBinding.skip.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showLogoutDialog() {
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
        db.attention.setText(R.string.delete)
        db.dialogLottie.setAnimation("stirict.json")
        db.save.text = "Logout"
        db.skip.text = "Cancel"

        db.save.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), SignIn::class.java))
            requireActivity().finish()
        }

        db.skip.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupDarkModeSwitch() {
        val prefs = requireContext().getSharedPreferences(
            "dark_mode_prefs",
            Context.MODE_PRIVATE
        )
        val isDark =
            ((resources.configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean("is_dark_mode", checked)
                .apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
