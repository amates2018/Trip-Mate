package io.tripmate.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.transition.TransitionManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import io.peanutsdk.recyclerview.GridItemDividerDecoration
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.api.TripsDataManager
import io.tripmate.api.UserDataManager
import io.tripmate.data.Trip
import io.tripmate.util.GlideApp
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.User
import io.tripmate.util.UserType

/**
 * Home screen for application
 */
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
    private val navView: NavigationView by bindView(R.id.nav_view)
    private val swipe: SwipeRefreshLayout by bindView(R.id.swipe_home)
    private val grid: RecyclerView by bindView(R.id.grid_home)

    private lateinit var headerView: View
    private lateinit var prefs: TripMatePrefs
    private lateinit var dataManager: TripsDataManager
    private lateinit var userDataManager: UserDataManager
    private lateinit var adapter: TripsAdapter
    private var user: User? = null

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

        userDataManager = object : UserDataManager(this@HomeActivity) {
            override fun onDataLoaded(data: User) {
                user = data
                setupHeader(user)
            }
        }

        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            user = intent.getParcelableExtra<User>(EXTRA_USER)
            headerView = navView.getHeaderView(0)
            setupHeader(user)
        } else if (prefs.isLoggedIn && prefs.isConnected) {
            userDataManager.loadCurrentUser(prefs.getAccessToken())
        }

        try {
            val menu = navView.menu
            val navSettings = menu?.findItem(R.id.nav_settings)
            navSettings?.isVisible = prefs.isLoggedIn && prefs.getUserType() == UserType.TYPE_DRIVER
        } catch (e: Exception) {
            e.printStackTrace()
        }
        navView.setNavigationItemSelectedListener(this)
        setupGrid()
    }

    private fun setupGrid() {
        dataManager = object : TripsDataManager(this@HomeActivity) {
            override fun onDataLoaded(data: List<Trip>) {
                adapter.addAndResort(data)
                checkEmptyState()
            }
        }

        adapter = TripsAdapter(this@HomeActivity)
        grid.adapter = adapter
        val layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
        grid.layoutManager = layoutManager
        grid.setHasFixedSize(true)
        grid.itemAnimator = SlideInItemAnimator()
        grid.addItemDecoration(GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider))
        dataManager.loadAllTrips()  //load all trips from database
        checkEmptyState()

        //Add swipe action
        swipe.setOnRefreshListener({
            if (adapter.dataItemCount() > 0) adapter.clear()
            dataManager.loadAllTrips()
            checkEmptyState()
        })
    }

    //Return the state of the content loaded into the recyclerview's viewholder
    private fun checkEmptyState() {
        if (adapter.dataItemCount() > 0) {
            //Items exists, show loading dialog
            TransitionManager.beginDelayedTransition(drawer)
            if (swipe.isRefreshing) swipe.isRefreshing = false
        } else {
            //No items in the database, hide loading dialog
            TransitionManager.beginDelayedTransition(drawer)
            if (swipe.isRefreshing) swipe.isRefreshing = false
        }
    }

    private fun setupHeader(user: User?) {
        if (user == null) {
            if (prefs.isLoggedIn && prefs.isConnected) {
                userDataManager.loadCurrentUser(prefs.getAccessToken())
            }
        } else {
            //Load widgets from header
            val profile = headerView.findViewById<ImageView>(R.id.user_profile)
            val username = headerView.findViewById<TextView>(R.id.user_name)
            val email = headerView.findViewById<TextView>(R.id.user_email)

            if (prefs.isLoggedIn) {
                //Load profile image
                GlideApp.with(this)
                        .asBitmap()
                        .load(user.profile?.regular)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.ic_player)
                        .fallback(R.drawable.avatar_placeholder)
                        .transition(withCrossFade())
                        .into(profile)

                //Load username and email address
                username.text = user.username
                email.text = user.email

                //navigate to the profile screen
                profile.setOnClickListener({
                    val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                    intent.putExtra(ProfileActivity.EXTRA_USER, user)
                    startActivityForResult(intent, EXTRA_PROFILE)
                })
            } else {
                profile.visibility = View.INVISIBLE
                username.setText(R.string.app_name)
                email.setText(R.string.login_prompt)
            }
        }

    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        dataManager.cancelLoading()
        super.onDestroy()
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
            R.id.action_search -> {
                val searchMenuView = toolbar.findViewById<View>(R.id.action_search)
                val options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle()
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.EXTRA_USER, user)
                startActivity(intent, options)
                true
            }
            R.id.action_login -> {
                if (prefs.isLoggedIn) {
                    MaterialDialog.Builder(this@HomeActivity)
                            .theme(Theme.DARK)
                            .content("Are you sure you want to logout")
                            .positiveText("Logout")
                            .negativeText("Cancel")
                            .onPositive({ dialog, _ ->
                                //Log user out and navigate to the login screen
                                dialog.dismiss()
                                prefs.logout()
                                startActivity(Intent(this@HomeActivity, AuthActivity::class.java))
                                finishAfterTransition()
                            })
                            .onNegative({ dialog, _ ->
                                dialog.dismiss()
                            })
                            .build().show()
                } else {
                    startActivityForResult(Intent(this@HomeActivity, AuthActivity::class.java),
                            REQ_USER_AUTH)
                }
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this@HomeActivity, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_reservations -> {
                startActivity(Intent(this@HomeActivity, ReservationsActivity::class.java))
            }
            R.id.nav_settings -> {
                val intent = Intent(this@HomeActivity, SettingsActivity::class.java)
                intent.putExtra(SettingsActivity.EXTRA_USER, user)
                startActivityForResult(intent, EXTRA_PROFILE)
            }
            R.id.nav_profile -> {
                val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                intent.putExtra(ProfileActivity.EXTRA_USER, user)
                startActivityForResult(intent, EXTRA_PROFILE)
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_USER_AUTH -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data != null && data.hasExtra(EXTRA_USER)) {
                        //Get result and setup user
                        val user = data.getParcelableExtra<User>(EXTRA_USER)
                        setupHeader(user)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
        const val EXTRA_PROFILE = 2
        const val REQ_USER_AUTH = 213
    }
}
