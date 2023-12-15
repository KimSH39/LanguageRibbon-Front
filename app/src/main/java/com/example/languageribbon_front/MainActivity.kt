package com.example.languageribbon_front

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var mainfragment: MainFragment
    private lateinit var versionfragment: VersionFragment
    private lateinit var ratingfragment: RatingFragment
    private lateinit var guidfragment: GuidFragment
    private lateinit var voicefragment: VoiceFragment
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userId = intent.getStringExtra("userId")

        // isFirstTime이랑 LoginActivity의 login랑은 반대임
        // isFirstTime가 login받은 거니까 false일 때 동의한 거
        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE) // 앱 자체에 데이터 저장
        val isFirstTime = sharedPreferences.getBoolean("login", false) // Corrected to use true as the default value

        if (!isFirstTime) { // Changed to check if it's the first time
            finish()
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        } else {
            // Continue with the rest of your onCreate logic
            mainfragment = MainFragment()
            versionfragment = VersionFragment()
            ratingfragment = RatingFragment()
            voicefragment = VoiceFragment()
            guidfragment = GuidFragment()

            settingSideNavBar()

            if (savedInstanceState == null) {
                replaceFragment(mainfragment, userId)
            }
        }
    }

    fun settingSideNavBar() {
        // toolbar를 actionbar로 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // drawer 아이콘 넣기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_dehaze_24)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // drawer 넣기
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.closed
        )

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main -> {
                    replaceFragment(mainfragment, userId)
                    finishOtherFragments(mainfragment)
                }
                R.id.voice -> {
                    replaceFragment(voicefragment, userId)
                    finishOtherFragments(voicefragment)
                }
                R.id.guid -> {
                    replaceFragment(guidfragment, userId)
                    finishOtherFragments(guidfragment)
                }
                R.id.version -> {
                    replaceFragment(versionfragment, userId)
                    finishOtherFragments(versionfragment)
                }

                R.id.logout -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val url = URL("https://d197-220-66-233-107.ngrok-free.app/logout/")
                            val conn = url.openConnection() as HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.doOutput = true  // Enable output for the POST request

                            Log.d("Logout", "Data sent to server")

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

                                val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.clear()
                                editor.apply()

                                val intent = intent
                                finish()
                                startActivity(intent)
                                withContext(Dispatchers.Main) {
                                    Log.d("Logout", "Data received from server: $receiveMsg")
                                    // 여기에 UI 업데이트 로직을 추가하세요.
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Logout", "Error: ${e.message}")
                                // 여기에 에러 처리 로직을 추가하세요.
                            }
                        }
                    }
                }

                R.id.rating -> {
                    replaceFragment(ratingfragment, userId)
                    finishOtherFragments(ratingfragment)
                }
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun finishOtherFragments(currentFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment != currentFragment) {
                    fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.container)

            if (fragment is VersionFragment
                || fragment is RatingFragment || fragment is RatingFragment) {
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.container, MainFragment()).commit()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, userId: String?) {
        val bundle = Bundle()
        bundle.putString("userId", userId)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }
}