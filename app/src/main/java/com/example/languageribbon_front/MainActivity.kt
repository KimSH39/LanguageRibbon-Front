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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingSideNavBar()
    }
    fun settingSideNavBar() {
        // Set up the Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Add the navigation drawer icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_dehaze_24)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Implement the side navigation bar
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.closed
        )

        // Set a click listener for the navigation items
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main -> {
                    replaceFragment(mainfragment)
                }
                R.id.voice -> {
                    Toast.makeText(applicationContext, "메뉴아이템 2 선택", Toast.LENGTH_SHORT).show()
                }
                R.id.guid -> {
                    Toast.makeText(applicationContext, "메뉴아이템 3 선택", Toast.LENGTH_SHORT).show()
                }
                R.id.version -> {
                    Toast.makeText(applicationContext, "메뉴아이템 3 선택", Toast.LENGTH_SHORT).show()
                }
                R.id.rating -> {
                    Toast.makeText(applicationContext, "메뉴아이템 3 선택", Toast.LENGTH_SHORT).show()
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