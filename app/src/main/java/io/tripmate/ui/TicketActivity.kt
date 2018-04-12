package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.data.Reservation
import io.tripmate.util.PaymentMethod
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Ticket purchasing interface
 */
class TicketActivity : Activity() {
    //Main content
    private val container: ViewGroup by bindView(R.id.container)
    private val toolbar: ViewGroup by bindView(R.id.back_wrapper)

    //Layout #1: Content trip
    private val oHeader: TextView by bindView(R.id.trip_origin_header)
    private val oContent: TextView by bindView(R.id.trip_origin_content)
    private val dHeader: TextView by bindView(R.id.trip_dest_header)
    private val dContent: TextView by bindView(R.id.trip_dest_content)

    //Layout #2: Passenger details
    private val pContent: TextView by bindView(R.id.passenger_username)
    private val sContent: TextView by bindView(R.id.passenger_seat)

    //Layout #3: Trip time
    private val tTime: TextView by bindView(R.id.trip_time)
    private val tDate: TextView by bindView(R.id.trip_date)

    //Layout #4: Driver and bus details
    private val busNumber: TextView by bindView(R.id.bus_number)
    private val driverUsername: TextView by bindView(R.id.driver_username)

    //Button
    private val buy: Button by bindView(R.id.buy_ticket)


    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)

        //Init prefs
        prefs = TripMatePrefs[this@TicketActivity]
        loading = TripMateUtils.getDialog(this@TicketActivity)

        //Setup toolbar
        val back = toolbar.findViewById<ImageButton>(R.id.toolbar_back)
        val menu = toolbar.findViewById<ImageButton>(R.id.toolbar_menu)
        val title = toolbar.findViewById<TextView>(R.id.toolbar_text)
        back.setOnClickListener({ onBackPressed() })
        title.text = getString(R.string.purchase_ticket)
        menu.visibility = View.GONE

        //get content data and setup UI
        val intent = intent
        if (intent.hasExtra(EXTRA_RESERVATION)) {
            val reservation = intent.getParcelableExtra<Reservation>(EXTRA_RESERVATION)
            updateUI(reservation)
        }
    }

    //Update UI to reflect the reservation data retrieved
    @SuppressLint("SetTextI18n")
    private fun updateUI(reservation: Reservation?) {
        if (reservation == null) {
            //Show message to user indicating that they have not yet made reservations for the
            // selected trip
            showSnackbar("Sorry! you have not yet made reservations for this trip")
            return
        }

        //Update UI with reservation
        val trip = reservation.trip
        val passenger = reservation.passenger
        val timestamp = reservation.timestamp

        //Set origin for trip
        oHeader.text = trip?.origin
        oContent.text = trip?.origin?.substring(0, 3)?.toUpperCase()

        //Set destination for trip
        dHeader.text = trip?.destination
        dContent.text = trip?.destination?.substring(0, 3)?.toUpperCase()

        //Passenger details
        pContent.text = passenger?.username
        sContent.text = intent.getStringExtra(EXTRA_SEAT_NUMBER)

        //Trip details
        tTime.text = trip?.duration.toString() + " hrs"
        if (timestamp != null) {
            tDate.text = DateUtils.getRelativeTimeSpanString(timestamp.time, System
                    .currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }

        //Bus & driver details
        busNumber.text = trip?.bus?.number
        driverUsername.text = trip?.driver?.username

        //Set buy button text
        buy.text = "${getString(R.string.buy_now)} for GHC${trip?.price}"

        buy.setOnClickListener({
            if (prefs.isLoggedIn) {
                doPayment(prefs.paymentMethod())
            }
        })
    }

    private fun doPayment(paymentMethod: CharSequence?) {
        if (paymentMethod == null) {
            //Prompt user to select payment method before proceeding
            showPaymentDialog()
        }

        //Restrict users to use only M TN Mobile Money for now
        if (paymentMethod == PaymentMethod.MTN_MOMO.value) {
            loading.builder.cancelable(false).show()
            deleteTrip()
        } else {
            showSnackbar("Only MTN Mobile money is currently supported. please choose MTN from " +
                    "the options list")
            showPaymentDialog()
        }

    }

    private fun deleteTrip() {
        //Delete reservation
        val reservation = intent.getParcelableExtra<Reservation>(EXTRA_RESERVATION)
        if (reservation != null) {
            prefs.db.collection(TripMateUtils.RESERVATION_REF)
                    .document(reservation.key!!)
                    .delete()
                    .addOnFailureListener(this@TicketActivity, { exception ->
                        showSnackbar(exception.localizedMessage)
                    }).addOnCompleteListener(this@TicketActivity, { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "Your purchase was successful", Toast.LENGTH_LONG)
                                    .show()
                            setResultAndFinish()
                        } else {
                            showSnackbar("Unable to remove trip reservation")
                            setResultAndFinish()
                        }
                    })
        }
    }

    private fun setResultAndFinish() {
        setResult(SeatsActivity.CODE_TICKET_PURCHASE)
        finishAfterTransition()
    }

    //Show dialog for user to select a payment method
    private fun showPaymentDialog() {
        MaterialDialog.Builder(this@TicketActivity)
                .theme(Theme.LIGHT)
                .title("Choose your payment method")
                .items(arrayListOf<String>(
                        PaymentMethod.TIGO_CASH.value,
                        PaymentMethod.CREDIT_CARD.value,
                        PaymentMethod.AIRTEL_CASH.value,
                        PaymentMethod.MTN_MOMO.value,
                        PaymentMethod.VODAFONE_MOMO.value
                ))
                .itemsCallback({ dialog, _, _, text ->
                    dialog.dismiss()
                    doPayment(text)
                    prefs.setPaymentMethod(text.toString())
                })
                .build().show()
    }


    private fun showSnackbar(s: String) {
        if (loading.isShowing) loading.dismiss()
        Snackbar.make(container, s, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_RESERVATION = "EXTRA_RESERVATION"
        const val EXTRA_SEAT_NUMBER = "EXTRA_SEAT_NUMBER"
    }
}
