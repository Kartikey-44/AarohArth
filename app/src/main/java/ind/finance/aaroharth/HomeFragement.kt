package ind.finance.aaroharth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragement.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragement : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //--------------< Animation Variables >---------------------------------------------------------
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation

    private lateinit var addTransactionFab: FloatingActionButton  //Fab --> FloatingActionButton
    private lateinit var incomeFab: FloatingActionButton
    private lateinit var expenseFab: FloatingActionButton
    private var isFabOpen = false
    //----------------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //--------------< Load All Animation  >-----------------------------------------------------
        rotateOpen = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_open_animation)
        rotateClose = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_rotate_close_animation)
        fromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_from_bottom_animation)
        toBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.addtransaction_to_bottom_animation)
        //------------------------------------------------------------------------------------------


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_fragement, container, false)
    }

        //--------------<  Animation -> FindButtonID >----------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIND YOUR FABS
        addTransactionFab = view.findViewById(R.id.add_transaction_Btn)
        incomeFab = view.findViewById(R.id.income_Btn)
        expenseFab = view.findViewById(R.id.expense_Btn)

        // AddTransaction FAB CLICK - TOGGLE MENU
        addTransactionFab.setOnClickListener {
            if (isFabOpen) closeFab() else openFab()
        }
        // --------------<  Animation ->  SetOnCLickListener, fragment --> fragment(findNavController()) or Screen (Intent)
        //INCOME FAB - Navigate to Income
        incomeFab.setOnClickListener {
            //findNavController().navigate(R.id.action_homeFragment_to_incomeFragment)
            Toast.makeText(requireContext(), "Income", Toast.LENGTH_SHORT).show()
            closeFab()
        }

        //EXPENSE FAB - Navigate to Expense
        expenseFab.setOnClickListener {
            //findNavController().navigate(R.id.action_homeFragment_to_expenseFragment)
            Toast.makeText(requireContext(), "Expense", Toast.LENGTH_SHORT).show()
            closeFab()
        }
    }

    //--------------<  Animation ->  functions  >---------------------------------------------------
    private fun openFab() {
        isFabOpen = true

        // SHOW + ANIMATE INCOME FAB
        incomeFab.visibility = View.VISIBLE
        incomeFab.isClickable = true
        incomeFab.startAnimation(fromBottom)

        // SHOW + ANIMATE EXPENSE FAB
        expenseFab.visibility = View.VISIBLE
        expenseFab.isClickable = true
        expenseFab.startAnimation(fromBottom)

        // ROTATE MAIN FAB
        addTransactionFab.startAnimation(rotateOpen)
    }

    private fun closeFab() {
        isFabOpen = false

        // HIDE INCOME FAB
        incomeFab.startAnimation(toBottom)
        incomeFab.isClickable = false
        incomeFab.visibility = View.GONE

        // HIDE EXPENSE FAB
        expenseFab.startAnimation(toBottom)
        expenseFab.isClickable = false
        expenseFab.visibility = View.GONE

        // ROTATE MAIN FAB BACK
        addTransactionFab.startAnimation(rotateClose)
    }
    //----------------------------------------------------------------------------------------------


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragement.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragement().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}