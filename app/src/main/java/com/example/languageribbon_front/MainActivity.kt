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
import com.example.languageribbon_front.VoiceFragment


class MainActivity : AppCompatActivity() {
    private lateinit var mainfragment: MainFragment
    private lateinit var versionfragment: VersionFragment
    private lateinit var ratingfragment: RatingFragment
    private lateinit var guidfragment: GuidFragment
    private lateinit var voicefragment: VoiceFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                replaceFragment(MainFragment())
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
                    replaceFragment(mainfragment)
                    finishOtherFragments(mainfragment)
                }
                R.id.voice -> {
                    replaceFragment(voicefragment)
                    finishOtherFragments(voicefragment)
                }
                R.id.guid -> {
                    replaceFragment(guidfragment)
                    finishOtherFragments(guidfragment)
                }
                R.id.version -> {
                    replaceFragment(versionfragment)
                    finishOtherFragments(versionfragment)
                }
                R.id.rating -> {
                    replaceFragment(ratingfragment)
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
            super.onBackPressed()
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }
}