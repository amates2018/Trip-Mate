package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Trip
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading a list of [Trip]s instance from the database
 */
abstract class TripsDataManager(private val activity: Activity)
    : BaseDataManager<List<Trip>>(activity.applicationContext) {

    private val inflight: ArrayList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns a list of [Trip]s pending
     */
    fun loadAllTrips() {
        loadStarted()
        val query = db.collection(TripMateUtils.TRIP_REF)
        inflight.add(query)
        val trips: MutableList<Trip> = ArrayList(0)
        query.addSnapshotListener(activity, { querySnapshot, exception ->
            if (exception != null) {
                endProcess(query, trips)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {

                for (doc in querySnapshot.documents) {
                    if (doc.exists()) {
                        val trip = doc.toObject(Trip::class.java)
                        trips.add(trip)
                    }
                }

                endProcess(query, trips)
                return@addSnapshotListener

            } else {
                endProcess(query, trips)
                return@addSnapshotListener
            }
        })

    }

    private fun endProcess(query: Query, trips: MutableList<Trip>) {
        loadFinished()
        onDataLoaded(trips)
        inflight.remove(query)
    }

}