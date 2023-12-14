package com.example.languageribbon_front

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.Charset


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private var isEnglishToKorea = false

    private var userid: String? = null
    private var targetlang = "kr"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // userid 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE)
        userid = sharedPreferences.getString("userid", "")

        // userid 로그에 출력
        Log.d("Upload", "User ID: $userid")

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
            targetlang = "kr"
        } else {
            binding.korea.text = "영어"
            binding.english.text = "한국어"
            targetlang = "en"
        }

        Log.d("Main", "$targetlang")
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

            Log.d("Upload","target lang : $targetlang")

            if(targetlang=="kr"){
                KrsendAudioToBackend()
            }else{
                EnsendAudioToBackend()
            }
        }
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
        val targetlangPart = MultipartBody.Part.createFormData("target-lang", "en")
        val useridPart = MultipartBody.Part.createFormData("user.id", userid ?: "")

        Log.d("UploadAudio", "Audio File Path: $audioFilePath")
        Log.d("UploadAudio", "User ID: ${userid ?: "Not available"}")
        Log.d("UploadAudio", "Language: kr")
        Log.d("UploadAudio", "Target Language: en")

        val apiService = RetrofitClient.createService(MyApi::class.java)
        val call = apiService.translate(useridPart, audioPart, langPart,targetlangPart)

        val krmessages = "Language Ribbon이 번역을 생성 중입니다..."
        val enmessages = "Language Ribbon is creating translation..."
        val handler = Handler(Looper.getMainLooper())

        var krIndex = 0
        var enIndex = 0

        val krRunnable = object : Runnable {
            override fun run() {
                if (krIndex <= krmessages.length) {
                    binding.me.text = krmessages.substring(0, krIndex)
                    krIndex++

                    // 0.2초마다 반복
                    handler.postDelayed(this, 200)
                } else {
                    // 메시지 전체가 출력된 후에는 공백을 출력하고 인덱스 초기화
                    binding.me.text = ""
                    krIndex = 0

                    // 1초 후에 다시 시작
                    handler.postDelayed(this, 1000)
                }
            }
        }

        val enRunnable = object : Runnable {
            override fun run() {
                if (enIndex <= enmessages.length) {
                    binding.you.text = enmessages.substring(0, enIndex)
                    enIndex++

                    // 0.2초마다 반복
                    handler.postDelayed(this, 200)
                } else {
                    // 메시지 전체가 출력된 후에는 공백을 출력하고 인덱스 초기화
                    binding.you.text = ""
                    enIndex = 0

                    // 1초 후에 다시 시작
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // 번역 시작 전에 텍스트 변경 시작
        handler.post(krRunnable)
        handler.post(enRunnable)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("TranslateResponse", "HTTP Code: ${response.code()}, Message: ${response.message()}")
                handler.removeCallbacks(krRunnable)
                handler.removeCallbacks(enRunnable)

                if (response.isSuccessful) {
                    // Process successful response
                    val responseData = response.body()?.bytes()
                    val jsonHeader = response.headers()["X-Json-Response"]

                    // 헤더에서 "=?utf-8?b?"와 "?=" 부분을 제거
                    val processedHeader = jsonHeader?.removePrefix("=?utf-8?b?")?.removeSuffix("?=")

                    // Base64 디코딩
                    val decodedBytes = Base64.decode(processedHeader, Base64.DEFAULT)
                    val decodedHeader = String(decodedBytes, Charset.forName("UTF-8"))

                    val msg = Gson().fromJson(decodedHeader, Map::class.java)

                    Log.d("TranslateResponse", "X-Json-Response Header: $jsonHeader")
                    Log.d("TranslateResponse", "Decoded and parsed message: $msg")

                    // "translation(영어 문장)"과 "original_messages(한국어 문장)"의 값을 가져옴
                    val translation = (msg["translation(영어 문장)"] as? List<*>)?.firstOrNull()?.toString()
                    val originalMessages = (msg["original_messages(한국어 문장)"] as? List<*>)?.firstOrNull()?.toString()

                    binding.you.text = translation.toString()
                    binding.me.text = originalMessages.toString()


                    // Check if responseData is not null
                    if (responseData != null) {
                        // Save the byte array to a temporary file
                        val tempFile = File.createTempFile("tempAudio", "mp3")
                        tempFile.writeBytes(responseData)

                        // Assuming you have a MediaPlayer instance
                        val mediaPlayer = MediaPlayer()

                        try {
                            // Set the data source from the temporary file
                            mediaPlayer.setDataSource(tempFile.path)

                            // Prepare and start the MediaPlayer
                            mediaPlayer.prepare()
                            mediaPlayer.start()

                            // Continue processing...
                        } catch (e: IOException) {
                            // Handle exception
                            Log.e("MediaPlayerError", "Error preparing or starting MediaPlayer", e)
                        } finally {
                            // Clean up: Delete the temporary file
                            tempFile.delete()
                        }
                    } else {
                        Log.e("TranslateError", "Server response indicates failure: Empty response data")
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("TranslateError", "Server response indicates failure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handler.removeCallbacks(krRunnable)
                handler.removeCallbacks(enRunnable)
                Log.e("TranslateError", "Failed to translate audio", t)
                Toast.makeText(requireContext(), "오디오 변환에 실패했습니다", Toast.LENGTH_SHORT).show()
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
        val langPart = MultipartBody.Part.createFormData("lang", "en")
        val targetlangPart = MultipartBody.Part.createFormData("target-lang", "kr")
        val useridPart = MultipartBody.Part.createFormData("user.id", userid ?: "")

        val apiService = RetrofitClient.createService(MyApi::class.java)
        val call = apiService.translate(useridPart, audioPart, langPart,targetlangPart)
        val krmessages = "Language Ribbon이 번역을 생성 중입니다..."
        val enmessages = "Language Ribbon is creating translation..."
        val handler = Handler(Looper.getMainLooper())

        var krIndex = 0
        var enIndex = 0

        val krRunnable = object : Runnable {
            override fun run() {
                if (krIndex <= krmessages.length) {
                    binding.you.text = krmessages.substring(0, krIndex)
                    krIndex++

                    // 0.2초마다 반복
                    handler.postDelayed(this, 200)
                } else {
                    // 메시지 전체가 출력된 후에는 공백을 출력하고 인덱스 초기화
                    binding.you.text = ""
                    krIndex = 0

                    // 1초 후에 다시 시작
                    handler.postDelayed(this, 1000)
                }
            }
        }

        val enRunnable = object : Runnable {
            override fun run() {
                if (enIndex <= enmessages.length) {
                    binding.me.text = enmessages.substring(0, enIndex)
                    enIndex++

                    // 0.2초마다 반복
                    handler.postDelayed(this, 200)
                } else {
                    // 메시지 전체가 출력된 후에는 공백을 출력하고 인덱스 초기화
                    binding.me.text = ""
                    enIndex = 0

                    // 1초 후에 다시 시작
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // 번역 시작 전에 텍스트 변경 시작
        handler.post(krRunnable)
        handler.post(enRunnable)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("TranslateResponse", "HTTP Code: ${response.code()}, Message: ${response.message()}")
                handler.removeCallbacks(krRunnable)
                handler.removeCallbacks(enRunnable)
                if (response.isSuccessful) {
                    // Process successful response
                    val responseData = response.body()?.bytes()
                    val jsonHeader = response.headers()["X-Json-Response"]

                    // 헤더에서 "=?utf-8?b?"와 "?=" 부분을 제거
                    val processedHeader = jsonHeader?.removePrefix("=?utf-8?b?")?.removeSuffix("?=")

                    // Base64 디코딩
                    val decodedBytes = Base64.decode(processedHeader, Base64.DEFAULT)
                    val decodedHeader = String(decodedBytes, Charset.forName("UTF-8"))

                    val msg = Gson().fromJson(decodedHeader, Map::class.java)

                    Log.d("TranslateResponse", "X-Json-Response Header: $jsonHeader")
                    Log.d("TranslateResponse", "Decoded and parsed message: $msg")

                    // "translation(영어 문장)"과 "original_messages(한국어 문장)"의 값을 가져옴
                    val translation = (msg["translation(한국어 문장)"] as? List<*>)?.firstOrNull()?.toString()
                    val originalMessages = (msg["original_messages(영어 문장)"] as? List<*>)?.firstOrNull()?.toString()

                    binding.you.text = translation.toString()
                    binding.me.text = originalMessages.toString()


                    // Check if responseData is not null
                    if (responseData != null) {
                        // Save the byte array to a temporary file
                        val tempFile = File.createTempFile("tempAudio", "mp3")
                        tempFile.writeBytes(responseData)

                        // Assuming you have a MediaPlayer instance
                        val mediaPlayer = MediaPlayer()

                        try {
                            // Set the data source from the temporary file
                            mediaPlayer.setDataSource(tempFile.path)

                            // Prepare and start the MediaPlayer
                            mediaPlayer.prepare()
                            mediaPlayer.start()

                            // Continue processing...
                        } catch (e: IOException) {
                            // Handle exception
                            Log.e("MediaPlayerError", "Error preparing or starting MediaPlayer", e)
                        } finally {
                            // Clean up: Delete the temporary file
                            tempFile.delete()
                        }
                    } else {
                        Log.e("TranslateError", "Server response indicates failure: Empty response data")
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("TranslateError", "Server response indicates failure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handler.removeCallbacks(krRunnable)
                handler.removeCallbacks(enRunnable)
                Log.e("TranslateError", "Failed to translate audio", t)
                Toast.makeText(requireContext(), "오디오 변환에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        })
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
