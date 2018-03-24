package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Seat
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading a list [Seat]s instance from the database
 */
abstract class SeatDataManager(private val activity: Activity)
    : BaseDataManager<List<Seat>>(activity.applicationContext) {

    private val inflight: ArrayList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns list of [Seat]s for a bus
     */
    fun loadSeatsForBus(busKey: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.SEAT_REF)
                .whereEqualTo("busKey", busKey)
        inflight.add(query)
        query.addSnapshotListener(activity, { querySnapshot, exception ->
            if (exception != null) {
                endProcess(query)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val seats: MutableList<Seat> = ArrayList(0)
                for (doc in querySnapshot.documents) {
                    if (doc.exists()) {
                        val seat = doc.toObject(Seat::class.java)
                        seats.add(seat)
                    }
                }

                loadFinished()
                onDataLoaded(seats)
            } else endProcess(query)
        })
    }

    private fun endProcess(query: Query) {
        loadFinished()
        inflight.remove(query)
    }

}