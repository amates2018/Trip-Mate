package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Trip
import io.tripmate.util.SearchableItem
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils


abstract class SearchDataManager(private val activity: Activity) : BaseDataManager<List<SearchableItem>>(activity) {
    private val inflight: MutableList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.isNotEmpty()) inflight.clear()
    }

    fun searchFor(origin: String, destination: String) {
        //Search for data
        loadStarted()
        val query = db.collection(TripMateUtils.TRIP_REF)
        inflight.add(query)
        val trips: MutableList<SearchableItem> = ArrayList(0)
        query.get().addOnCompleteListener(activity, { task ->
            if (task.isSuccessful) {
                for (doc in task.result.documents) {
                    if (doc.exists()) {
                        val trip = doc.toObject(Trip::class.java)
                        //Compare the origin and destination fields
                        if (trip.origin!!.contains(origin, true)
                                && trip.destination!!.contains(destination, true)) {
                            //if condition is true, add to list
                            trips.add(trip)
                        }
                    }
                }

                setResultAndFinish(query, trips)

            }
        }).addOnFailureListener(activity, { _ ->
            setResultAndFinish(query, trips)
        })
    }

    fun clear() {
        if (inflight.isNotEmpty()) {
            for (query in inflight) {
                inflight.remove(query)
            }
        }
    }

    private fun setResultAndFinish(query: Query, trips: MutableList<SearchableItem>) {
        loadFinished()
        onDataLoaded(trips)
        inflight.remove(query)
    }

}