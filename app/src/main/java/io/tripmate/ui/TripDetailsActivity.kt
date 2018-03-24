package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

/**
 * For trip details
 */
class TripDetailsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)
    }
}
