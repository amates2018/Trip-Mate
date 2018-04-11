package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView
import io.tripmate.BuildConfig
import io.tripmate.R
import io.tripmate.api.UserDataManager
import io.tripmate.data.Passenger
import io.tripmate.data.Reservation
import io.tripmate.data.Trip
import io.tripmate.util.GlideApp
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User
import java.text.NumberFormat
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
    private val originHeader: TextView by bindView(R.id.trip_origin_header)
    private val originContent: TextView by bindView(R.id.trip_origin_content)
    private val destHeader: TextView by bindView(R.id.trip_dest_header)
    private val destContent: TextView by bindView(R.id.trip_dest_content)

    //Card #2: Bus and duration details
    private val busName: TextView by bindView(R.id.bus_name)
    private val busNumber: TextView by bindView(R.id.bus_number)
    private val tripDuration: TextView by bindView(R.id.trip_duration)

    //Card #3: Driver details
    private val driverProfile: ForegroundImageView by bindView(R.id.driver_profile)
    private val driverName: TextView by bindView(R.id.driver_username)
    private val terminalName: TextView by bindView(R.id.terminal_name)

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

        //Data manager for loading user instance
        dataManager = object : UserDataManager(this@TripDetailsActivity) {
            override fun onDataLoaded(data: User) {
                if (loading.isShowing) loading.dismiss()
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

    //Adds trip to reservations
    private fun addTrip(trip: Trip) {
        //add to reservations
        val snackbar = Snackbar.make(main, "Adding trip to your reservations", Snackbar.LENGTH_LONG)
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
                                    if (BuildConfig.DEBUG)
                                        Log.d("TripDetailsActivity", "Added trip successfully")
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


    @SuppressLint("SetTextI18n")
    private fun bindTrip(trip: Trip?) {
        if (trip == null) return

        //Set origin
        originHeader.text = trip.origin
        originContent.text = trip.origin?.substring(0, 3)?.toUpperCase()

        //Set destination
        destHeader.text = trip.destination
        destContent.text = trip.destination?.substring(0, 3)?.toUpperCase()

        //bus details
        val bus = trip.bus
        busName.text = bus?.type
        busNumber.text = bus?.number
        tripDuration.text = "${NumberFormat.getNumberInstance().format(trip.duration)} hrs"

        //Driver details
        val driver = trip.driver
        GlideApp.with(applicationContext)
                .load(driver?.profile)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(driverProfile)
        driverName.text = driver?.username
        terminalName.text = driver?.terminalKey


    }

    override fun onDestroy() {
        dataManager.cancelLoading()
        super.onDestroy()
    }

    companion object {
        const val TRIP_DATA = "TRIP_DATA"
    }
}
