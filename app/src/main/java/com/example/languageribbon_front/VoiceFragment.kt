package com.example.languageribbon_front

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.example.languageribbon_front.databinding.FragmentVoiceBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.languageribbon_front.MainFragment.Companion.PERMISSION_REQUEST_CODE
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class VoiceFragment : Fragment() {
    private var _binding: FragmentVoiceBinding? = null
    private val binding get() = _binding!!
    private var position = 0

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()

    companion object {
        const val STEP_1 = 0
        const val STEP_2 = 1
        const val STEP_3 = 2
        const val STEP_4 = 3
        const val STEP_5 = 4
        //const val FINAL_STEP = 5
    }
    private var recordButton1ClickCount = 0
    private var recordButton2ClickCount = 0

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

        binding.listening1.visibility = View.GONE
        binding.listening2.visibility = View.GONE

        binding.record1.setOnClickListener {
            recordButton1ClickCount++

            when {
                recordButton1ClickCount == 1 -> {
                    startRecording1()
                }
                recordButton1ClickCount == 2 -> {
                    stopRecording1()
                    binding.listening1.visibility = View.VISIBLE
                }
            }

            if (recordButton1ClickCount > 2) {
                recordButton1ClickCount = 0
            }

            updateRecordImage1()
        }

        binding.record2.setOnClickListener {
            recordButton2ClickCount++

            when {
                recordButton2ClickCount == 1 -> {
                    startRecording2()
                }
                recordButton2ClickCount == 2 -> {
                    stopRecording2()
                    binding.listening2.visibility = View.VISIBLE
                }
            }

            if (recordButton2ClickCount > 2) {
                recordButton2ClickCount = 0
            }
            updateRecordImage1()
        }

        binding.listening1.setOnClickListener {
            // Play recorded audio
            if (audioFilePath != null) {
                if (mediaPlayer == null) {
                    // Start playing
                    startPlaying1()
                } else {
                    // Stop playing
                    stopPlaying1()
                }
            } else {
                Toast.makeText(requireContext(), "No audio file to play", Toast.LENGTH_SHORT).show()
            }

            updateListeningImage1()
        }

        binding.listening2.setOnClickListener {
            if (audioFilePath != null) {
                if (mediaPlayer == null) {
                    startPlaying2()
                } else {
                    stopPlaying2()
                }
            } else {
                Toast.makeText(requireContext(), "No audio file to play", Toast.LENGTH_SHORT).show()
            }

            updateListeningImage2()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    private fun startPlaying1() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                updateListeningImage1()

                handler.postDelayed(updateSeekBar, 100)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun startPlaying2() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                updateListeningImage2()

                handler.postDelayed(updateSeekBar, 100)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                // Update your seek bar or other UI elements with currentPosition

                // Update seek bar progress after 100 milliseconds
                handler.postDelayed(this, 100)
            }
        }
    }

    private fun stopPlaying1() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
            updateRecordImage1()
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun stopPlaying2() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
            updateRecordImage2()
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun updateRecordImage1() {
        val imageResource = if (isRecording) {
            R.drawable.stopbtn
        } else {
            R.drawable.startbtn
        }
        binding.record1.setImageResource(imageResource)
    }

    private fun updateRecordImage2() {
        val imageResource = if (isRecording) {
            R.drawable.stopbtn
        } else {
            R.drawable.startbtn
        }
        binding.record1.setImageResource(imageResource)
    }

    private fun updateListeningImage1() {
        val imageResource = if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            R.drawable.playingbtn
        } else {
            R.drawable.playbtn
        }
        binding.listening1.setImageResource(imageResource)
    }

    private fun updateListeningImage2() {
        val imageResource = if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            R.drawable.playingbtn
        } else {
            R.drawable.playbtn
        }
        binding.listening2.setImageResource(imageResource)
    }
    private fun startRecording1() {
        if (checkPermissions()) {
            val fileName = "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.wav"
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/$fileName"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Use THREE_GPP for WAV format
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Use AMR_NB for WAV format
                setOutputFile(audioFilePath)
                mediaRecorder?.setAudioSamplingRate(44100)
                mediaRecorder?.setAudioEncodingBitRate(96000)
                try {
                    prepare()
                    start()
                    isRecording = true
                    Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()

                    binding.record1.setBackgroundResource(R.drawable.playingbtn)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun startRecording2() {
        if (checkPermissions()) {
            val fileName = "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.wav"
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/$fileName"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Use THREE_GPP for WAV format
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Use AMR_NB for WAV format
                setOutputFile(audioFilePath)
                mediaRecorder?.setAudioSamplingRate(44100)
                mediaRecorder?.setAudioEncodingBitRate(96000)
                try {
                    prepare()
                    start()
                    isRecording = true
                    Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()

                    binding.record1.setBackgroundResource(R.drawable.playingbtn)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            requestPermissions()
        }
    }


    private fun stopRecording1() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
            isRecording = false
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()

            binding.record1.setBackgroundResource(R.drawable.playbtn)

            sendAudioToBackend()
        }
    }

    private fun stopRecording2() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
            isRecording = false
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()

            binding.record2.setBackgroundResource(R.drawable.playbtn)

            sendAudioToBackend()
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
            requireActivity().onBackPressed()
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
//        binding.stepView.done(false)
//        binding.stepView.go(position, true)

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