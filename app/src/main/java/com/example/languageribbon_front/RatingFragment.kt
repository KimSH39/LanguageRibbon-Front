package com.example.languageribbon_front

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.languageribbon_front.databinding.FragmentRatingBinding

class RatingFragment : Fragment() {
    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingBar = binding.ratingBar
        val ratingment = binding.ratingMent
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if(rating>=1 && rating<=3){
                ratingment.text="더 나은 Language Ribbon이 될 수 있도록 노력하겠습니다."
            }else{
            ratingment.text="Language Ribbon이 마음에 드셨군요! 감사합니다!"
            }

        }
    }
}