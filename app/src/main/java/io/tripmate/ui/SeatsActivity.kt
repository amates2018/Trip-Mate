package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.data.Reservation
import io.tripmate.util.*

/**
 * For seat selection and reservation
 */
class SeatsActivity : Activity(), OnSeatSelected {

    private val container: ViewGroup by bindView(R.id.container)
    private val grid: RecyclerView by bindView(R.id.seats_grid)
    private val headerView: ViewGroup by bindView(R.id.layout_header)

    private val busName: TextView by bindView(R.id.bus_name)
    private val tripDetails: TextView by bindView(R.id.trip_details)
    private val booking: TextView by bindView(R.id.booking_date)
    private val price: TextView by bindView(R.id.trip_price)

    private var count: Int = 0

    private lateinit var prefs: TripMatePrefs
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: BusSeatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seats)

        //Init shared preferences
        prefs = TripMatePrefs[this]

        //Init database reference
        db = prefs.db

        //Setup recyclerview
        val columns = 5
        val list: MutableList<AbstractItem> = ArrayList(0)

        //Create a list of abstract seats: 25 in total
        for (i in 0..24) {
            if (i.mod(columns) == 0 || i.mod(columns) == 4) {
                list.add(EdgeItem(i.toString()))
            } else if (i.mod(columns) == 1 || i.mod(columns) == 3) {
                list.add(CenterItem(i.toString()))
            } else {
                list.add(EdgeItem(i.toString()))
            }
        }
        adapter = BusSeatAdapter(this, list)
        grid.adapter = adapter
        val layoutManager = GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false)
        grid.layoutManager = layoutManager
        grid.itemAnimator = SlideInItemAnimator()
        grid.setHasFixedSize(true)

        //Get intent and get data
        val intent = intent
        if (intent.hasExtra(EXTRA_TRIP_DATA)) {
            val reservation = intent.getParcelableExtra<Reservation>(EXTRA_TRIP_DATA)
            bindTrip(reservation)
        }
    }

    private fun bindTrip(reservation: Reservation?) {
        if (reservation == null) return

        //Get trip bus
        val trip = reservation.trip
        val bus = trip?.bus

        //Add details
        busName.text = bus?.type
        tripDetails.text = String.format("%s - %s", trip?.origin, trip?.destination)

        //Get number of seats for the bus
        val numSeats = bus?.seats?.size

        if (numSeats != null && numSeats > 0) {
            //Show number of seats as message for now
            Snackbar.make(container, "You have $numSeats seats", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSeatSelected(position: Int) {
        //Increase count by the position selected
        count += position
    }

    companion object {
        const val EXTRA_TRIP_DATA = "EXTRA_TRIP_DATA"
    }
}
