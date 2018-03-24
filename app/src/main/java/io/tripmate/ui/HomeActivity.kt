package io.tripmate.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.util.TripMatePrefs

/**
 * Home screen for application
 */
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
    private val navView: NavigationView by bindView(R.id.nav_view)

    private lateinit var headerView: View
    private lateinit var prefs: TripMatePrefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        //Init prefs
        prefs = TripMatePrefs[this@HomeActivity]

        navView.setNavigationItemSelectedListener(this)
        headerView = navView.getHeaderView(0)
        setupHeader()
    }

    private fun setupHeader() {
        //Load widgets from header
        val profile = headerView.findViewById<ImageView>(R.id.user_profile)
        val username = headerView.findViewById<TextView>(R.id.user_name)
        val email = headerView.findViewById<TextView>(R.id.user_email)

        if (prefs.isLoggedIn) {
            //todo: load user details
        } else {
            profile.visibility = View.INVISIBLE
            username.setText(R.string.app_name)
            email.setText(R.string.login_prompt)
        }

    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val login = menu?.findItem(R.id.action_login)
        login?.setTitle(if (prefs.isLoggedIn) R.string.logout else R.string.login)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_login -> {
                //todo: nav to login screen
                true
            }
            R.id.action_about -> {
                //todo: nav to about us screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
