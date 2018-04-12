package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Reservation
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading a list of [Reservation]s instance from the database
 */
abstract class ReservationsDataManager(private val activity: Activity)
    : BaseDataManager<List<Reservation>>(activity.applicationContext) {

    private val inflight: ArrayList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns a list of [Reservation]s
     */
    fun loadTripReservations(key: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.RESERVATION_REF)
                .whereEqualTo("passenger.key", key)
        inflight.add(query)
        val reservations: MutableList<Reservation> = ArrayList(0)
        query.addSnapshotListener(activity, { querySnapshot, exception ->
            if (exception != null) {
                endProcess(query, reservations)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {

                for (doc in querySnapshot.documents) {
                    if (doc.exists()) {
                        val reservation = doc.toObject(Reservation::class.java)
                        reservations.add(reservation)
                    }
                }

                endProcess(query, reservations)
                return@addSnapshotListener

            } else {
                endProcess(query, reservations)
                return@addSnapshotListener
            }
        })

    }

    private fun endProcess(query: Query, reservations: MutableList<Reservation>) {
        loadFinished()
        onDataLoaded(reservations)
        inflight.remove(query)
    }

}