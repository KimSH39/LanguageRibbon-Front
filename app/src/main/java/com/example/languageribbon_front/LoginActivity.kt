package com.example.languageribbon_front

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.languageribbon_front.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var isExistBlank = false
    // 앱 상태(로그인 여부) 저장 : 앱 내부 초기 값 설정
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
    // 카카오 로그인
    // 카카오계정으로 로그인 공통 callback 구성
    // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
//    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
//        if (error != null) {
//            Log.e(TAG, "카카오계정으로 로그인 실패", error)
//        } else if (token != null) {
//            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
//            // 로그인 -> 메인
//            sharedPreferences.edit {
//                putBoolean("login", true)
//            }
//            val intent = Intent(this@LoginActivity, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // login false로 지정
        sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        val login = sharedPreferences.getBoolean("login", false)

        //로그인 버튼을 클릭했을떄
        binding.login.setOnClickListener {
            if (binding.id.text.isEmpty() || binding.password.text.isEmpty()) {
                isExistBlank = true
                Toast.makeText(this, "로그인 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
            }else {
                try {
                    sharedPreferences.edit {
                        putBoolean("login", true)
                    }
                    val id = binding.id.text.toString()
                    val pw = binding.password.text.toString()

                    // 유저가 입력한 id, pw를 쉐어드에 저장한다.
                    val sharedPreference = getSharedPreferences("Account", Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString("id", id)
                    editor.putString("pw", pw)
                    editor.apply()
                    Log.d("Login","$id, $pw")

                    GlobalScope.launch(Dispatchers.IO) {
                        val result = performLogin(id, pw)
                        // UI 업데이트 작업 등을 여기에 추가할 수 있습니다.
                        if (result != null) {
                            runOnUiThread {
                                //로그인 성공 시 메인 화면으로 이동
                                Toast.makeText(this@LoginActivity, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("userId", id)
                                startActivity(intent)
                                overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("DBtest", "${e.message}")
                }
            }
        }

        // 카카오 로그인 버튼 클릭했을 때 로그인
//        with(binding) {
//            kakaobtn.setOnClickListener {
//
//                // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
//                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
//                    UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { token, error ->
//                        if (error != null) {
//                            Log.e(TAG, "카카오톡으로 로그인 실패", error)
//
//                            // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
//                            // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
//                            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
//                                return@loginWithKakaoTalk
//                            }
//
//                            // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
//                            UserApiClient.instance.loginWithKakaoAccount(
//                                this@LoginActivity,
//                                callback = callback
//                            )
//                        } else if (token != null) {
//                            Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
//                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                            startActivity(intent)
//                            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
//                        }
//                    }
//                } else {
//                    UserApiClient.instance.loginWithKakaoAccount(
//                        this@LoginActivity,
//                        callback = callback
//                    )
//                }
//            }
//        }
    }
    private suspend fun performLogin(id: String, pw: String): String? {
        try {
            Log.d("TestRegisterActivity", "Inside performLogin - Start")

            val url = URL("http://ec2-3-12-247-228.us-east-2.compute.amazonaws.com:8000/login/")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true  // Enable output for the POST request
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val params = "username=$id&password=$pw"
            val postData = params.toByteArray(StandardCharsets.UTF_8)

            conn.outputStream.use { os ->
                os.write(postData)
            }

            Log.d("Login", "Data sent to server")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val tmp = InputStreamReader(conn.inputStream, "UTF-8")
                val reader = BufferedReader(tmp)
                val buffer = StringBuffer()

                var str: String? = null
                while (reader.readLine().also { str = it } != null) {
                    str?.let {
                        buffer.append(it)
                    }
                }
                val receiveMsg = buffer.toString()

                Log.d("Login", "Data received from server: $receiveMsg")

                // Parse the JSON data
                val jsonData = JSONObject(receiveMsg)

                // Extract values from JSON
                val voiceInfoEn = jsonData.optBoolean("voice_info_en", false)
                val voiceInfoKr = jsonData.optBoolean("voice_info_kr", false)
                val name = jsonData.optString("name", "")

                // Save values to SharedPreferences
                val sharedPreference = getSharedPreferences("Login", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putBoolean("login", true)
                editor.putBoolean("voiceKr", voiceInfoEn)
                editor.putBoolean("voiceEn", voiceInfoKr)
                editor.putString("name", name)
                editor.apply()

                return receiveMsg
            } else {
                Log.d("login", "HTTP connection failed with response code: ${conn.responseCode}")
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Log.e("Login", "MalformedURLException: ${e.message}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Login", "IOException: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Login", "Exception: ${e.message}")
        }

        return null
    }
}