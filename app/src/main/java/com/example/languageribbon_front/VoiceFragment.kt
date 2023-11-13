package com.example.languageribbon_front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.languageribbon_front.databinding.FragmentVoiceBinding

class VoiceFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private var _binding: FragmentVoiceBinding? = null
    private val binding get() = _binding!!
    private var position = 0

    companion object {
        const val STEP_1 = 0
        const val STEP_2 = 1
        const val STEP_3 = 2
        const val FINAL_STEP = 3
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

        adapter = MyAdapter(mutableListOf())

        recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.button.setOnClickListener {
            when (position) {
                STEP_1 -> transitionToStep(STEP_2, "다음")
                STEP_2 -> transitionToStep(STEP_3, "다음")
                STEP_3 -> transitionToStep(FINAL_STEP, "회원가입")
                else -> {}
            }
        }
    }
    fun updateAdapterData(newData: MutableList<MyElement>) {
        adapter.setList(newData)
        adapter.notifyDataSetChanged()
    }
    private fun transitionToStep(nextPosition: Int, buttonText: String) {
        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.GONE
            STEP_2 -> binding.STEP2.visibility = View.GONE
            STEP_3 -> binding.STEP3.visibility = View.GONE
            FINAL_STEP -> binding.FINALSTEP.visibility = View.GONE
        }

        position = nextPosition

        binding.stepView.done(false)
        binding.stepView.go(position, true)

        binding.button.text = buttonText

        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.VISIBLE
            STEP_2 -> binding.STEP2.visibility = View.VISIBLE
            STEP_3 -> binding.STEP3.visibility = View.VISIBLE
            FINAL_STEP -> binding.FINALSTEP.visibility = View.VISIBLE
        }
    }


}