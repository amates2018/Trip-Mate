package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Reservations for user trips
 */
class ReservationsActivity : Activity() {
    //Main
    private val container: ViewGroup by bindView(R.id.container)
    private val toolbar: ViewGroup by bindView(R.id.back_wrapper)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)


        //Init prefs
        prefs = TripMatePrefs[this@ReservationsActivity]
        loading = TripMateUtils.getDialog(applicationContext)
    }
}
