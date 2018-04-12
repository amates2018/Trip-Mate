package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.data.Reservation
import io.tripmate.data.Seat
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

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: BusSeatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seats)

        //Init shared preferences
        prefs = TripMatePrefs[this]
        loading = TripMateUtils.getDialog(this@SeatsActivity)

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

    @SuppressLint("SetTextI18n")
    private fun bindTrip(reservation: Reservation?) {
        if (reservation == null) return

        //Get trip bus
        val trip = reservation.trip
        val bus = trip?.bus

        //Set price
        price.text = "GHC ${trip?.price}"
        if (reservation.timestamp != null) {
            booking.text = "Booking date: ${DateUtils.getRelativeTimeSpanString(reservation.timestamp!!.time, System
                    .currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)}"
        } else {
            booking.visibility = ViewGroup.INVISIBLE
        }

        //Add details
        busName.text = bus?.type
        tripDetails.text = String.format("%s - %s", trip?.origin, trip?.destination)

        //Get number of seats for the bus
        val numSeats = bus?.seats

        if (numSeats != null && numSeats > 0) {
            //Show number of seats as message for now
            Snackbar.make(container, "You have $numSeats seats", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSeatSelected(position: Int) {
        //Increase count by the position selected
        val seatNumber = TripMateUtils.generatedSeatNumber(position)
        val intent = Intent(this@SeatsActivity, TicketActivity::class.java)

        //Add intent data
        intent.putExtra(TicketActivity.EXTRA_RESERVATION, intent.getParcelableExtra<Reservation>(EXTRA_TRIP_DATA))
        intent.putExtra(TicketActivity.EXTRA_SEAT_NUMBER, seatNumber)

        //Add animation to next activity
        val options = ActivityOptions.makeSceneTransitionAnimation(this@SeatsActivity)
        startActivityForResult(intent, CODE_TICKET_PURCHASE, options.toBundle())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CODE_TICKET_PURCHASE -> {
                    loading.builder.cancelable(false)
                    loading.show()
                    val reservation = intent.getParcelableExtra<Reservation>(EXTRA_TRIP_DATA)
                    val document = prefs.db.collection(TripMateUtils.SEAT_REF).document()
                    val seat = Seat(document.id, System.currentTimeMillis(), reservation?.trip?.bus, false, prefs.getAccessToken())
                    document.set(seat).addOnCompleteListener(this@SeatsActivity, { task ->
                        if (task.isSuccessful) {
                            loading.dismiss()
                            Toast.makeText(applicationContext, "You have purchased your seat",
                                    Toast.LENGTH_LONG).show()
                            finishAfterTransition()
                        }
                    }).addOnFailureListener(this@SeatsActivity, { exception ->
                        Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                    })
                }
            }
        }
    }

    companion object {
        const val EXTRA_TRIP_DATA = "EXTRA_TRIP_DATA"
        const val CODE_TICKET_PURCHASE = 7
    }
}
