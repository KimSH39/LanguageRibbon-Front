package com.example.languageribbon_front

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.languageribbon_front.databinding.ActivitySignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var position = 0
    val TAG: String = "회원가입"

    companion object {
        private const val STEP_1 = 0
        private const val STEP_2 = 1
        private const val STEP_3 = 2
        private const val FINAL_STEP = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        binding.stepView.done(false)
        binding.tologinbtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        binding.button.setOnClickListener {
            when (position) {
                STEP_1 -> transitionToStep(STEP_2, "다음")
                STEP_2 -> transitionToStep(STEP_3, "다음")
                STEP_3 -> transitionToStep(FINAL_STEP, "회원가입")
                else -> {

                    var isExistBlank = false
                    var studentNumCorrect = true
                    var emailCorrect = true

                    val email = binding.email.text.toString()
                    val password = binding.password.text.toString()
                    val name = binding.name.text.toString()
                    val passwordCheck = binding.passwordCheck.text.toString()
                    val genderRadioGroup = findViewById<RadioGroup>(R.id.gender)
                    val selectedRadioButtonId = genderRadioGroup.checkedRadioButtonId

                    if (selectedRadioButtonId != -1) {
                        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                        val selectedGender = selectedRadioButton.text.toString()

                        // Now you can use the selectedGender as needed
                    } else {
                        // No RadioButton is selected, show an error or handle it as appropriate
                    }


                    // 유저가 항목을 다 채우지 않았을 경우
                    if (email.isEmpty() && password.isEmpty() && name.isEmpty() && passwordCheck.isEmpty()
                        && genderRadioGroup.checkedRadioButtonId == -1) {
                        isExistBlank = true
                    }


                    if (!isExistBlank && emailCorrect && studentNumCorrect) {
                        // 회원가입 성공 토스트 메세지 띄우기
                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        Log.d("bitmapSignup","$email, $name, $password, ")
                        GlobalScope.launch(Dispatchers.IO) {
                            val result = performSignup(email, name, password)
                            if (result != null) {
                                runOnUiThread {
                                    //로그인 성공 시 메인 화면으로 이동
                                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                                    finish()
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
                                }
                            }
                        }
                    }
                    else{
                        // 상태에 따라 다른 다이얼로그 띄워주기
                        if(isExistBlank){   // 작성 안한 항목이 있을 경우
                            dialog("blank")
                        }
                        else if(!emailCorrect){ // 입력한 비밀번호가 다를 경우
                            dialog("emailCorrect")
                        }
//                        else if(!isAgree){ // 이용약관 동의 안한 경우
//                            dialog("Agree")
//                        }
                    }
                }
            }
        }
    }

    private suspend fun performSignup(email: String, name: String, password: String): String? {
        try {
            val url = URL("http://jsp.mjdonor.kro.kr:8888/webapp/Android/performSignup.jsp")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.requestMethod = "POST"

            val osw: OutputStream = conn.outputStream
            val writer = BufferedWriter(OutputStreamWriter(osw, "UTF-8"))

            val sendMsg = "email=$email&name=$name&password=$password"
            Log.d("bitmapPerform", "email=$email&name=$name&password=$password")
            writer.write(sendMsg)
            writer.flush()

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
                return receiveMsg
            } else {
                Log.d("TestRegisterActivity", "HTTP connection failed with response code: ${conn.responseCode}")
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TestRegisterActivity", "IOException: ${e.message}")
            Log.e("TestRegisterActivity", "IOException: $e")
        }

        return null
    }


    override fun onBackPressed() {
        when (position) {
            STEP_1 -> super.onBackPressed()
            STEP_2 -> transitionToStep(STEP_1, "Next")
            STEP_3 -> transitionToStep(STEP_2, "Next")
            else -> transitionToStep(STEP_3, "Next")
        }
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

    fun dialog(type: String){
        val dialog = AlertDialog.Builder(this)

        // 작성 안한 항목이 있을 경우
        if(type.equals("blank")){
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("입력란을 정확히 작성해주세요")
        }
        else if(type.equals("studentNumCorrect")){
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("명지대 통합로그인 아이디, 비밀번호를 입력해주세요!")
        }
        else if(type.equals("emailCorrect")){
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("이메일 형식을 맞춰주세요")
        }
        else if(type.equals("Agree")){
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("이용약관에 동의해주세요")
        }

        val dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    DialogInterface.BUTTON_POSITIVE ->
                        Log.d(TAG, "다이얼로그")
                }
            }
        }

        dialog.setPositiveButton("확인",dialog_listener)
        dialog.show()
    }
    private var isErrorResponse = false // 에러 응답 여부를 나타내는 변수


}