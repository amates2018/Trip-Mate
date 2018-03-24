package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

/**
 * User profile screen
 */
class ProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }
}
