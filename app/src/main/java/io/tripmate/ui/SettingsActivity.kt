package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

/**
 * Current user's settings
 */
class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}
