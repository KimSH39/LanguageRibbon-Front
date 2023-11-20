package com.example.languageribbon_front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.example.languageribbon_front.databinding.FragmentVoiceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoiceFragment : Fragment() {
    private var _binding: FragmentVoiceBinding? = null
    private val binding get() = _binding!!
    private var position = 0

    companion object {
        const val STEP_1 = 0
        const val STEP_2 = 1
        const val STEP_3 = 2
        const val STEP_4 = 3
        const val STEP_5 = 4
        //const val FINAL_STEP = 5
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            when (position) {
                STEP_1 -> transitionToStep(STEP_2, "다음")
                STEP_2 -> transitionToStep(STEP_3, "다음")
                STEP_3 -> transitionToStep(STEP_4, "다음")
                STEP_4 -> transitionToStep(STEP_5, "초기 목소리 설정 완료")
                //STEP_5 -> transitionToStep(FINAL_STEP, "회원가입")
                else -> {}
            }
        }

        // Listen for the back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    private fun onBackPressed() {
        if (position > STEP_1) {
            val previousPosition = position - 1
            val buttonText = when (previousPosition) {
                //FINAL_STEP -> "회원가입"
                else -> "이전"
            }
            transitionToStep(previousPosition, buttonText)
        } else {
            // Handle back press when at the first step (if needed)
            requireActivity().onBackPressed()
        }
    }


    private fun transitionToStep(nextPosition: Int, buttonText: String) {
        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.GONE
            STEP_2 -> binding.STEP2.visibility = View.GONE
            STEP_3 -> binding.STEP3.visibility = View.GONE
            STEP_4 -> binding.STEP4.visibility = View.GONE
            STEP_5 -> binding.STEP5.visibility = View.GONE
            //FINAL_STEP -> binding.FINALSTEP.visibility = View.GONE
        }

        position = nextPosition

        binding.stepView.done(false)
        binding.stepView.go(position, true)

        binding.button.text = buttonText

        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.VISIBLE
            STEP_2 -> {binding.STEP2.visibility = View.VISIBLE
                updateDate() }
            STEP_3 -> binding.STEP3.visibility = View.VISIBLE
            STEP_4 -> binding.STEP4.visibility = View.VISIBLE
            STEP_5 -> binding.STEP5.visibility = View.VISIBLE
            //FINAL_STEP -> binding.FINALSTEP.visibility = View.VISIBLE
        }
    }

    private fun updateDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val krformattedString = getString(R.string.krscript, currentDate)
        val enformattedString = getString(R.string.enscript, currentDate)
        binding.enscript.text = enformattedString
        binding.krscript.text = krformattedString

    }
}