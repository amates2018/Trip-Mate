package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

/**
 * For seat selection and reservation
 */
class SeatsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seats)
    }
}
