package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import io.tripmate.R
import io.tripmate.util.User
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * User profile screen
 */
class ProfileActivity : Activity() {

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = TripMatePrefs[this]
        loading = TripMateUtils.getDialog(this@ProfileActivity)

        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            val user = intent.getParcelableExtra<User>(EXTRA_USER)
            bindUser(user)
        }

    }

    private fun bindUser(user: User?) {
        if (user == null) return
        //todo: bind user data here
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
    }
}
