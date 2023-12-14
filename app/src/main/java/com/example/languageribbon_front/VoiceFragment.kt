package com.example.languageribbon_front

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    private lateinit var soundVisualizerView1: SoundVisualizerView
    private lateinit var soundVisualizerView2: SoundVisualizerView

    private var userid: String? = null

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

        soundVisualizerView1 = view.findViewById(R.id.soundVisualizerView1)
        soundVisualizerView2 = view.findViewById(R.id.soundVisualizerView2)

        // userid 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE)
        userid = sharedPreferences.getString("userid", "")

        // userid 로그에 출력
        Log.d("Upload", "User ID: $userid")

        binding.button.setOnClickListener {
            when (position) {
                STEP_1 -> transitionToStep(STEP_2, "다음")
                STEP_2 -> {transitionToStep(STEP_3, "다음")
                    KrsendAudioToBackend()}
                STEP_3 -> transitionToStep(STEP_4, "다음")
                STEP_4 -> {transitionToStep(STEP_5, "초기 목소리 설정 완료")
                    EnsendAudioToBackend()}
                else -> {
                    val sharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("voiceEn", true)
                    editor.apply()

                    navigateToMainFragment()
                }

            }
        }

        binding.reset1.visibility = View.GONE
        binding.reset2.visibility = View.GONE

        binding.record1.setOnClickListener {
            recordButton1ClickCount++

            when {
                recordButton1ClickCount == 1 -> {
                    startRecording1()
                }
                recordButton1ClickCount == 2 -> {
                    stopRecording1()
                    binding.reset1.visibility = View.VISIBLE
                }
                recordButton1ClickCount % 2 == 1 -> { // odd number
                    startPlaying1()
                }
                recordButton1ClickCount % 2 == 0 -> { // even number
                    stopPlaying1()
                }
            }

            binding.reset1.setOnClickListener {
                stopPlaying1()
                soundVisualizerView1.clearVisualization()
                recordButton1ClickCount = 0
                binding.reset1.visibility = View.GONE
                binding.record1.setImageResource(R.drawable.startbtn)
            }
        }

        binding.record2.setOnClickListener {
            recordButton2ClickCount++
            when {
                recordButton2ClickCount == 1 -> {
                    startRecording2()
                }
                recordButton2ClickCount == 2 -> {
                    stopRecording2()
                    binding.reset2.visibility = View.VISIBLE
                }
                recordButton2ClickCount % 2 == 1 -> { // odd number
                    startPlaying2()
                }
                recordButton2ClickCount % 2 == 0 -> { // even number
                    stopPlaying2()
                }
            }

            binding.reset2.setOnClickListener {
                stopPlaying2()
                soundVisualizerView2.clearVisualization()
                recordButton2ClickCount = 0
                binding.reset2.visibility = View.GONE
                binding.record2.setImageResource(R.drawable.startbtn)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    private fun navigateToMainFragment() {
        // Here you can use FragmentManager to navigate to MainFragment.
        // Make sure to replace R.id.fragment_container with the actual container ID in your layout.
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun startPlaying1() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                binding.record1.setImageResource(R.drawable.playingbtn)

                setOnCompletionListener {
                    binding.record1.setImageResource(R.drawable.playbtn)
                    recordButton1ClickCount = 3
                }

                handler.postDelayed(updateSeekBar, 100)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        soundVisualizerView1.startVisualizing(true)
    }

    private fun startPlaying2() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                binding.record2.setImageResource(R.drawable.playingbtn)

                setOnCompletionListener {
                    binding.record2.setImageResource(R.drawable.playbtn)
                    recordButton2ClickCount = 3
                }

                handler.postDelayed(updateSeekBar, 100)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        soundVisualizerView2.startVisualizing(true)
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
            binding.record1.setImageResource(R.drawable.playbtn)
            soundVisualizerView1.stopVisualizing()
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun stopPlaying2() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
            binding.record2.setImageResource(R.drawable.playbtn)
            soundVisualizerView2.stopVisualizing()
            handler.removeCallbacks(updateSeekBar)
        }
    }
    private fun startRecording1() {
        if (checkPermissions()) {
            val fileName = "KR_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.wav"
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/$fileName"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                try {
                    prepare()
                    start()
                    isRecording = true
                    binding.record1.setImageResource(R.drawable.stopbtn)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            soundVisualizerView1.startVisualizing(false)

            soundVisualizerView1.onRequestCurrentAmplitude = {
                val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                //maxAmplitude에 값 곱해서 진폭 크기 키우기
                maxAmplitude * 3
            }
        } else {
            requestPermissions()
        }
    }

    private fun startRecording2() {
        if (checkPermissions()) {
            val fileName = "EN_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.wav"
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/$fileName"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                try {
                    prepare()
                    start()
                    isRecording = true
                    Toast.makeText(requireContext(), "녹음 시작", Toast.LENGTH_SHORT).show()

                    binding.record2.setImageResource(R.drawable.playingbtn)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            soundVisualizerView2.startVisualizing(false)

            soundVisualizerView2.onRequestCurrentAmplitude = {
                val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                maxAmplitude * 3
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
            binding.record1.setImageResource(R.drawable.playbtn)
            soundVisualizerView1.stopVisualizing()
        }
    }
    private fun stopRecording2() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
            isRecording = false
            binding.record2.setImageResource(R.drawable.playbtn)
            soundVisualizerView2.stopVisualizing()
        }
    }

    private fun onBackPressed() {
        if (position > STEP_1) {
            val previousPosition = position - 1
            val buttonText = when (previousPosition) {
                //FINAL_STEP -> "회원가입"
                else -> "다음"
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

    private fun KrsendAudioToBackend() {
        // Check if the audioFilePath is not null or empty
        if (audioFilePath.isNullOrEmpty()) {
            Log.e("FilePathError", "Audio file path is null or empty")
            Toast.makeText(requireContext(), "오디오 파일 경로가 잘못되었습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val filePathMessage = "Audio File Path: ${audioFilePath ?: "Not available"}"
        Log.d("UploadAudio", "$audioFilePath")

        val audioFile = File(audioFilePath)

        if (!audioFile.exists()) {
            Log.e("FileNotExist", "Audio file does not exist at path: $audioFilePath")
            Toast.makeText(requireContext(), "오디오 파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isNetworkConnected()) {
            Toast.makeText(requireContext(), "인터넷 연결이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val audioRequestBody = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)
        val langPart = MultipartBody.Part.createFormData("lang", "kr")
        val useridPart = MultipartBody.Part.createFormData("user.id", userid ?: "")

        val apiService = RetrofitClient.createService(MyApi::class.java)
        val call = apiService.uploadAudioFile(useridPart, audioPart, langPart)

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                val serverResponse = response.body()

                if (serverResponse != null) {
                    if (serverResponse.uploadSuccess == true) {
                        Log.d("UploadAudio", "Response: ${serverResponse.message}")
                        val metric = serverResponse.metric
                        if (metric != null) {
                            Log.d("UploadAudio", "Metric 한국어 cer: ${metric.cer}")

                            val cerValue = metric.cer
                            val cerPercentage = 100 - (cerValue * 100)
                            when {
                                cerValue >= 0.0 && cerValue < 0.2 -> {
                                    binding.krCER.setValueAnimated(
                                        cerPercentage.toFloat(),
                                        1000
                                    )
                                    binding.krCER.setBarColor(Color.parseColor("#4198FF"))
                                }
                                cerValue >= 0.2 && cerValue < 0.3 -> {
                                    binding.krCER.setValueAnimated(cerPercentage.toFloat(), 1000)
                                    binding.krCER.setBarColor(Color.parseColor("#FFD541"))
                                }
                                cerValue >= 0.3 -> {
                                    binding.krCER.setValueAnimated(cerPercentage.toFloat(), 1000)
                                    binding.krCER.setBarColor(Color.RED)
                                }
                                else -> {
                                    binding.krCER.setValueAnimated(0f, 1000)
                                    binding.krCER.setBarColor(Color.GRAY)
                                }
                            }

                        } else {
                            Log.d("UploadAudio", "Metric is null")
                        }
                    } else {
                        Log.e("UploadError", "Server response indicates failure: ${serverResponse.message}")
                    }
                } else {
                    Log.e("UploadError", "Null response from server")
                    Toast.makeText(requireContext(), "서버 응답이 없습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.e("UploadError", "Failed to upload audio", t)
                Toast.makeText(requireContext(), "오디오 전송에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // 네트워크 에러 확인
    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    private fun EnsendAudioToBackend() {
        if (audioFilePath.isNullOrEmpty()) {
            Log.e("FilePathError", "Audio file path is null or empty")
            Toast.makeText(requireContext(), "오디오 파일 경로가 잘못되었습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val filePathMessage = "Audio File Path: ${audioFilePath ?: "Not available"}"
        Log.d("UploadAudio", "$audioFilePath")

        val audioFile = File(audioFilePath)

        if (!audioFile.exists()) {
            Log.e("FileNotExist", "Audio file does not exist at path: $audioFilePath")
            Toast.makeText(requireContext(), "오디오 파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isNetworkConnected()) {
            Toast.makeText(requireContext(), "인터넷 연결이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val audioRequestBody = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)
        val langPart = MultipartBody.Part.createFormData("lang", "en")
        val useridPart = MultipartBody.Part.createFormData("user.id", userid ?: "")

        val apiService = RetrofitClient.createService(MyApi::class.java)
        val call = apiService.uploadAudioFile(useridPart, audioPart, langPart)

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                val serverResponse = response.body()

                if (serverResponse != null) {
                    if (serverResponse.uploadSuccess == true) {
                        Log.d("UploadAudio", "Response: ${serverResponse.message}")
                        val metric = serverResponse.metric
                        if (metric != null) {
                            Log.d("UploadAudio", "Metric 영어 cer: ${metric.cer}")
                            val cerValue = metric.cer
                            val cerPercentage = 100 - (cerValue * 100)
                            when {
                                cerValue >= 0.0 && cerValue < 0.2 -> {
                                    binding.enCER.setValueAnimated(
                                        cerPercentage.toFloat(),
                                        1000
                                    )
                                    binding.enCER.setBarColor(Color.parseColor("#4198FF"))
                                }
                                cerValue >= 0.2 && cerValue < 0.3 -> {
                                    binding.enCER.setValueAnimated(cerPercentage.toFloat(), 1000)
                                    binding.enCER.setBarColor(Color.parseColor("#FFD541"))
                                }
                                cerValue >= 0.3 -> {
                                    binding.enCER.setValueAnimated(cerPercentage.toFloat(), 1000)
                                    binding.enCER.setBarColor(Color.RED)
                                }
                                else -> {
                                    binding.enCER.setValueAnimated(0f, 1000)
                                    binding.enCER.setBarColor(Color.GRAY)
                                }
                            }

                        } else {
                            Log.d("UploadAudio", "Metric is null")
                        }
                    } else {
                        Log.e("UploadError", "Server response indicates failure: ${serverResponse.message}")
                    }
                } else {
                    // Handle null response
                    Log.e("UploadError", "Null response from server")
                    Toast.makeText(requireContext(), "서버 응답이 없습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.e("UploadError", "Failed to upload audio", t)
                Toast.makeText(requireContext(), "오디오 전송에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        })
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}