package io.tripmate.api

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Bus
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading [Bus] instance from the database
 */
abstract class BusDataManager(private val activity: Activity)
    : BaseDataManager<Bus>(activity.applicationContext) {

    private val inflight: ArrayList<Task<DocumentSnapshot>> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns [Bus] model
     */
    fun loadBusData(key: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.BUS_REF)
                .document(key)
                .get()
        inflight.add(query)
        query.addOnCompleteListener(activity, { task ->
            if (task.isSuccessful) {
                val bus = task.result.toObject(Bus::class.java)
                loadFinished()
                onDataLoaded(bus)
                inflight.remove(query)
            } else {
                loadFinished()
                inflight.remove(query)
            }
        }).addOnFailureListener(activity, { _ ->
            loadFinished()
            inflight.remove(query)
        })
    }

}