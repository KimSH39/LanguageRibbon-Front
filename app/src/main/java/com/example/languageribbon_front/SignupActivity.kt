package com.example.languageribbon_front

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.languageribbon_front.databinding.ActivitySignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets


class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var position = 0
    val TAG: String = "회원가입"

    companion object {
        private const val STEP_1 = 0
        private const val STEP_2 = 1
        private const val STEP_3 = 2
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
                STEP_2 -> transitionToStep(STEP_3, "회원가입")
                else -> {

                    var isExistBlank = false
                    var studentNumCorrect = true
                    var emailCorrect = true
                    var passwordCorrect = false

                    val name = binding.name.text.toString()
                    val email = binding.email.text.toString()
                    val emailDomain = binding.emaildomain.selectedItem.toString()
                    val username = email + "@" + emailDomain
                    val password = binding.password.text.toString()
                    val passwordCheck = binding.passwordCheck.text.toString()
                    val age = binding.age.selectedItem.toString()
                    val ageIndex = when (age) {
                        "10대" -> 1
                        "20대" -> 2
                        "30대" -> 3
                        else -> -1 // 예외 처리: 해당되는 값이 없을 경우 -1을 할당
                    }

                    val genderRadioGroup = findViewById<RadioGroup>(R.id.gender)
                    val selectedgenderRadioButtonId = genderRadioGroup.checkedRadioButtonId
                    val selectedgenderRadioButton = findViewById<RadioButton>(selectedgenderRadioButtonId)
                    val selectedGender = selectedgenderRadioButton.text.toString()
                    Log.d("선택된 성별", selectedGender);
                    val sex = if (selectedGender == "남성") 1 else 2


                    val jobRadioGroup = findViewById<RadioGroup>(R.id.job)
                    val selectedjobRadioButtonId = jobRadioGroup.checkedRadioButtonId
                    val selectedjobRadioButton = findViewById<RadioButton>(selectedjobRadioButtonId)
                    val selectedjob = selectedjobRadioButton.text.toString()
                    Log.d("선택된 직업", selectedjob);
                    val job = if (selectedjob == "학생") 1 else if (selectedjob == "직장인") 2 else 3

                    val levelRadioGroup = findViewById<RadioGroup>(R.id.level)
                    val selectedlevelRadioButtonId = levelRadioGroup.checkedRadioButtonId
                    val selectedlevelRadioButton = findViewById<RadioButton>(selectedlevelRadioButtonId)
                    val selectedlevel = selectedlevelRadioButton.text.toString()
                    Log.d("선택된 직업", selectedlevel);
                    val level = if (selectedlevel == "상") 1 else if (selectedlevel == "중") 2 else 3

                    binding.agreeAll.setOnCheckedChangeListener { _, isChecked ->
                        binding.agree1.isChecked = isChecked
                        binding.agree2.isChecked = isChecked
                    }

                    binding.agree1.setOnCheckedChangeListener { _, isChecked ->
                        if (!isChecked) {
                            binding.agreeAll.isChecked = false
                        } else if (isChecked && binding.agree2.isChecked) {
                            binding.agreeAll.isChecked = true
                        }
                    }

                    binding.agree2.setOnCheckedChangeListener { _, isChecked ->
                        if (!isChecked) {
                            binding.agreeAll.isChecked = false
                        } else if (isChecked && binding.agree1.isChecked) {
                            binding.agreeAll.isChecked = true
                        }
                    }

                    val Agree = binding.agreeAll.isChecked

                    // 유저가 항목을 다 채우지 않았을 경우
                    if (name.isEmpty() && email.isEmpty() && password.isEmpty() && name.isEmpty() && passwordCheck.isEmpty()
                        && genderRadioGroup.checkedRadioButtonId != -1 && jobRadioGroup.checkedRadioButtonId != -1
                        && levelRadioGroup.checkedRadioButtonId != -1 && age != "연령대 입력") {
                        isExistBlank = true
                    }

                    //이메일 유효성 체크
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                        emailCorrect = false
                    }

                    if (password.length >= 8 && password.contains("[!@#$%^&*(),.?\":{}|<>]".toRegex())) {
                        passwordCorrect = true
                    }

                    if (!isExistBlank && emailCorrect && studentNumCorrect && Agree && passwordCorrect) {
                        // 회원가입 성공 토스트 메세지 띄우기
                        Log.d("Signup","$email, $name, $password, $ageIndex, $job, $sex, $level")
                        GlobalScope.launch(Dispatchers.IO) {
                            val result = performSignup(username, name, sex, ageIndex, level, job, password, passwordCheck)
                            if (result != null) {
                                runOnUiThread {
                                    //로그인 성공 시 메인 화면으로 이동
                                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                                    finish()
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
                                }
                            }else {
                                // Signup failed, display the appropriate dialog
                                dialog("blank")
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
                        else if(!passwordCorrect){ // 입력한 비밀번호가 다를 경우
                            dialog("passwordCorrect")
                        }
                        else if(!Agree){ // 이용약관 동의 안한 경우
                            dialog("Agree")
                        }
                    }
                }
            }
        }
    }

    private suspend fun performSignup(email: String, name: String,
                                      sex: Int, age: Int, job : Int, englishLevel : Int, password1:String, password2:String): String? {
        try {
            Log.d("TestRegisterActivity", "Inside performLogin - Start")

            val url = URL("http://ec2-3-12-247-228.us-east-2.compute.amazonaws.com:8000/signup/")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true  // Enable output for the POST request
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val params = "username=$email&name=$name&sex=$sex&age=$age&job=$job&englishLevel=$englishLevel&password1=$password1&password2=$password2"
            val postData = params.toByteArray(StandardCharsets.UTF_8)
            Log.d("TestRegisterActivity", "POST 데이터: $params")
            conn.outputStream.use { os ->
                os.write(postData)
            }

            Log.d("Signup", "Data sent to server")

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

                val message = jsonData.optString("message", "")

                Log.e("Signup","${message}")

                return receiveMsg
            } else {
                Log.d("login", "HTTP connection failed with response code: ${conn.responseCode}")
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Log.e("Signup", "MalformedURLException: ${e.message}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Signup", "IOException: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Signup", "Exception: ${e.message}")
        }

        return null
    }

    override fun onBackPressed() {
        when (position) {
            STEP_1 -> super.onBackPressed()
            STEP_2 -> transitionToStep(STEP_1, "Next")
            else -> transitionToStep(STEP_2, "Next")
        }
    }

    private fun transitionToStep(nextPosition: Int, buttonText: String) {
        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.GONE
            STEP_2 -> binding.STEP2.visibility = View.GONE
            STEP_3 -> binding.STEP3.visibility = View.GONE
        }
        position = nextPosition
        binding.stepView.done(false)
        binding.stepView.go(position, true)
        binding.button.text = buttonText
        when (position) {
            STEP_1 -> binding.STEP1.visibility = View.VISIBLE
            STEP_2 -> binding.STEP2.visibility = View.VISIBLE
            STEP_3 -> binding.STEP3.visibility = View.VISIBLE
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
        else if(type.equals("passwordCorrect")){
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("비밀번호 형식을 맞춰주세요")
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