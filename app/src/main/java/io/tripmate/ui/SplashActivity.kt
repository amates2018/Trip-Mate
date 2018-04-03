package io.tripmate.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.tripmate.R
import io.tripmate.api.UserDataManager
import io.tripmate.data.Passenger
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User
import io.tripmate.util.UserType

/**
 * Splash screen
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = TripMatePrefs[this]
        val dialog = TripMateUtils.getDialog(this@SplashActivity)

        val userDataManager: UserDataManager = object : UserDataManager(this@SplashActivity) {
            override fun onDataLoaded(data: User) {
                if (data is Passenger) {
                    //load passenger interface
                    prefs.setAccessToken(data.key, UserType.TYPE_PASSENGER)
                } else {
                    prefs.setAccessToken(data.key, UserType.TYPE_DRIVER)
                }

                if (dialog.isShowing) dialog.dismiss()
                if (prefs.isLoggedIn) {
                    val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                    intent.putExtra(HomeActivity.EXTRA_USER, data)
                    startActivity(intent)
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }

        //Show splash screen for 2.5secs and then carry out inner task
        Handler().postDelayed({
            if (prefs.isLoggedIn) {
                //Check connection
                if (prefs.isConnected) {
                    dialog.show()
                    //Refresh user data
                    userDataManager.loadCurrentUser(prefs.getAccessToken())
                } else {
                    //Just go to the Home page
                    val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                //Prompt user to login through the login interface
                startActivity(Intent(this@SplashActivity, OnBoardActivity::class.java))
                finish()
            }
        }, 2800)


    }
}
