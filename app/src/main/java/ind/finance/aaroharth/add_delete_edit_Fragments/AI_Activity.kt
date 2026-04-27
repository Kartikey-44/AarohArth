package ind.finance.aaroharth.add_delete_edit_Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ind.finance.aaroharth.databinding.ActivityAiBinding
import ind.finance.aaroharth.viewmodels.AI_ViewModel
import java.util.Locale

class AI_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityAiBinding
    private val viewModel: AI_ViewModel by viewModels()

    private val SPEECH_REQ = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAiBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.mice.setOnClickListener { startSpeech() }

        binding.inputField.setOnClickListener {
            if (binding.lottie.isVisible)
                stopListeningUI()
        }

        binding.send.setOnClickListener {

            val input = binding.inputField.text.toString().trim()

            if (input.isEmpty()) {

                binding.inputLayout.error = "Enter something"

                return@setOnClickListener
            }

            viewModel.processInput(input)
        }

        observeViewModel()
    }


    private fun observeViewModel() {

        viewModel.extractedData.observe(this) { data ->

            when (data.action) {

                "TRANSACTION" -> openTransaction(data)

                "BUDGET" -> openBudget(data)

                "ACCOUNT" -> openAccount(data)

                else ->
                    Toast.makeText(
                        this,
                        "Could not understand intent",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }


    private lateinit var speechRecognizer: SpeechRecognizer

    private fun startSpeech() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                SPEECH_REQ
            )

            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                startListeningUI()
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                binding.inputField.setText(data?.get(0))
                viewModel.processInput(data?.get(0) ?: "")
                stopListeningUI()
            }

            override fun onError(error: Int) {
                stopListeningUI()
            }


            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
        })

        speechRecognizer.startListening(intent)
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        stopListeningUI()

        if (
            requestCode == SPEECH_REQ &&
            resultCode == RESULT_OK
        ) {

            val result =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            if (!result.isNullOrEmpty())
                binding.inputField.setText(result[0])
        }
    }


    private fun startListeningUI() {

        binding.lottie.visibility = View.VISIBLE

        binding.instruct.visibility = View.GONE

        binding.lottie.playAnimation()
    }


    private fun stopListeningUI() {

        binding.lottie.cancelAnimation()

        binding.lottie.visibility = View.GONE

        binding.instruct.visibility = View.VISIBLE
    }


    private fun openTransaction(
        data: AI_ViewModel.ExtractedData
    ) {

        val intent =
            Intent(
                this,
                Transaction_Action_Page::class.java
            ).apply {

                putExtra("type", data.type)

                putExtra("amount", data.amount)

                putExtra("category", data.category)

                putExtra("otherParty", data.otherParty)

                putExtra("transactionWay", data.transactionWay)

                putExtra("transactionMedium", data.transactionMedium)
            }

        startActivity(intent)
    }


    private fun openBudget(
        data: AI_ViewModel.ExtractedData
    ) {

        val intent =
            Intent(
                this,
                BudgetActions::class.java
            ).apply {

                putExtra("amount", data.amount ?: -1L)

                putExtra("category", data.category)

                putExtra("monthKey", data.monthKey)

                putExtra("note", data.originalText)
            }

        startActivity(intent)
    }


    private fun openAccount(
        data: AI_ViewModel.ExtractedData
    ) {

        val intent =
            Intent(
                this,
                AccountActions::class.java
            ).apply {

                putExtra("accountType", data.transactionMedium)

                putExtra("accountName", data.accountName)

                putExtra("balance", data.amount ?: -1L)

                putExtra("note", data.originalText)
            }

        startActivity(intent)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == SPEECH_REQ &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        )
            startSpeech()
    }


}