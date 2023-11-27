package com.example.languageribbon_front

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.languageribbon_front.databinding.FragmentMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private var isEnglishToKorea = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
            updateButtonImage()
        }

        binding.switchbtn.setOnClickListener {
            swapTexts()
        }
    }

    private fun swapTexts() {
        val koreaText = binding.korea.text.toString()
        val englishText = binding.english.text.toString()

        if (isEnglishToKorea) {
            binding.korea.text = "한국어"
            binding.english.text = "영어"
        } else {
            binding.korea.text = "영어"
            binding.english.text = "한국어"
        }

        isEnglishToKorea = !isEnglishToKorea
    }

    private fun updateButtonImage() {
        val imageResource = if (isRecording) {
            R.drawable.playingbtn
        } else {
            R.drawable.playbtn
        }
        binding.button.setImageResource(imageResource)
    }

    private fun startRecording() {
        if (checkPermissions()) {
            val fileName = "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp3"
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/$fileName"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                    Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()

                    // Update the button image when recording starts
                    binding.button.setBackgroundResource(R.drawable.playingbtn)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
            isRecording = false
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()

            // Update the button image when recording stops
            binding.button.setBackgroundResource(R.drawable.playbtn)

            sendAudioToBackend()
        }
    }


    private fun sendAudioToBackend() {
        audioFilePath?.let { filePath ->
            val audioFile = File(filePath)
            if (audioFile.exists()) {
                val audioRequestBody = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)

//                // Replace the following with your actual API call
//                val apiService = RetrofitClient.createService(ApiService::class.java)
//                val call = apiService.uploadAudioFile(audioPart)
//                call.enqueue(object : Callback<YourResponseClass> {
//                    override fun onResponse(call: Call<YourResponseClass>, response: Response<YourResponseClass>) {
//                        // Handle success
//                        Toast.makeText(requireContext(), "Audio sent to backend", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onFailure(call: Call<YourResponseClass>, t: Throwable) {
//                        // Handle failure
//                        Toast.makeText(requireContext(), "Failed to send audio", Toast.LENGTH_SHORT).show()
//                    }
//                })
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return (checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissions(permissions, PERMISSION_REQUEST_CODE)
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
