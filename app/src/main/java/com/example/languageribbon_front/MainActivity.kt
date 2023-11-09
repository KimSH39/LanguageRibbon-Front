package com.example.languageribbon_front

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

class MainActivity : AppCompatActivity() {
    private lateinit var mainfragment: MainFragment
    private lateinit var versionfragment: VersionFragment
    private lateinit var ratingfragment: RatingFragment
    private lateinit var guidfragment: GuidFragment
    private lateinit var voicefragment: VoiceFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainfragment=MainFragment()
        versionfragment=VersionFragment()
        ratingfragment=RatingFragment()
        voicefragment=VoiceFragment()
        guidfragment=GuidFragment()

        settingSideNavBar()

        if (savedInstanceState == null) {
            replaceFragment(MainFragment())
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
                }
                R.id.voice -> {
                    replaceFragment(voicefragment)
                }
                R.id.guid -> {
                    replaceFragment(guidfragment)
                }
                R.id.version -> {
                    replaceFragment(versionfragment)
                }
                R.id.rating -> {
                    replaceFragment(ratingfragment)
                }
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
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