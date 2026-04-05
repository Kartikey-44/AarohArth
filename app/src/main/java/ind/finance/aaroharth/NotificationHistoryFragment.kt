package ind.finance.aaroharth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.aaroharth.adapters.NotificationHistoryAdapter
import ind.finance.aaroharth.databinding.FragmentNotificationHistoryBinding
import ind.finance.aaroharth.viewmodels.NotificationHistoryViewModel
import ind.finance.aaroharth.viewmodels.ViewModelFactory

class NotificationHistoryFragment : Fragment() {

    private var _binding: FragmentNotificationHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationHistoryViewModel by viewModels {
        val app = requireActivity().application as MyApplication
        ViewModelFactory(
            app.transactionRepository,
            app.accountRepository,
            app.budgetRepository,
            app.notificationRepository
        )
    }

    private lateinit var adapter: NotificationHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        adapter = NotificationHistoryAdapter(emptyList())
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = adapter

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}