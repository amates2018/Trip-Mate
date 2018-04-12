package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

/**
 * Ticket purchasing interface
 */
class TicketActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)
    }

    companion object {
        const val EXTRA_TRIP = "EXTRA_TRIP"
    }
}
