package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.api.UserDataManager
import io.tripmate.data.Passenger
import io.tripmate.data.Reservation
import io.tripmate.data.Trip
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User
import java.util.*

/**
 * For trip details
 */
class TripDetailsActivity : Activity() {
    //Main
    private val main: ViewGroup by bindView(R.id.root)
    private val container: ViewGroup by bindView(R.id.container)

    //Toolbar
    private val toolbar: ViewGroup by bindView(R.id.back_wrapper)

    //Card #1: Trip details

    //Card #2: Bus and duration details

    //Card #3: Driver details

    //Misc
    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var dataManager: UserDataManager
    private var user: Passenger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        //Init prefs
        prefs = TripMatePrefs[this@TripDetailsActivity]
        loading = TripMateUtils.getDialog(applicationContext)

        //Setup toolbar
        val back = toolbar.findViewById<ImageButton>(R.id.toolbar_back)
        val menu = toolbar.findViewById<ImageButton>(R.id.toolbar_menu)
        val title = toolbar.findViewById<TextView>(R.id.toolbar_text)
        back.setOnClickListener({ onBackPressed() })
        title.text = getString(R.string.trip_details)
        menu.setImageDrawable(resources.getDrawable(R.drawable.ic_event_add))

        dataManager = object : UserDataManager(this@TripDetailsActivity) {
            override fun onDataLoaded(data: User) {
                user = data as Passenger
            }
        }

        //Load current user
        if (prefs.isLoggedIn) dataManager.loadCurrentUser(prefs.getAccessToken())

        val intent = intent
        if (intent.hasExtra(TRIP_DATA)) {
            val trip = intent.getParcelableExtra<Trip>(TRIP_DATA)
            bindTrip(trip)
            menu.setOnClickListener({
                addTrip(trip)
            })
        }
    }

    private fun addTrip(trip: Trip) {
        //add to reservations
        val snackbar = Snackbar.make(main, "Added successfully to reservations", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo", { _ ->
            snackbar.dismiss()
        })
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                //Push data to database if user has not yet dismissed the snackbar
                if (user == null) {
                    showError("We could not retrieve your user details. Please restart the app!")
                } else {
                    //Create document
                    val document = prefs.db.collection(TripMateUtils.RESERVATION_REF).document()
                    //Create trip reservation
                    val reservation = Reservation(document.id, trip, user!!, Date(System.currentTimeMillis()))
                    document.set(reservation)
                            .addOnCompleteListener(this@TripDetailsActivity, { task ->
                                if (task.isSuccessful) {
                                    
                                } else {
                                    showError(task.exception?.localizedMessage)
                                }
                            })
                }
            }
        })
        snackbar.show()
    }

    private fun showError(message: String?) {
        if (loading.isShowing) loading.dismiss()
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


    private fun bindTrip(trip: Trip?) {
        if (trip == null) return

        Toast.makeText(applicationContext, trip.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dataManager.cancelLoading()
        super.onDestroy()
    }

    companion object {
        const val TRIP_DATA = "TRIP_DATA"
    }
}
