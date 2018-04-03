package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.tripmate.R
import io.tripmate.data.Trip

/**
 * For trip details
 */
class TripDetailsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        val intent = intent
        if (intent.hasExtra(TRIP_DATA)) {
            val trip = intent.getParcelableExtra<Trip>(TRIP_DATA)
            bindTrip(trip)
        }
    }

    private fun bindTrip(trip: Trip?) {
        if (trip == null) return

        Toast.makeText(applicationContext, trip.toString(), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TRIP_DATA = "TRIP_DATA"
    }
}
