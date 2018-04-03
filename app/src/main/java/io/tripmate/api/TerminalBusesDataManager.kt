package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Bus
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading [Bus]es for a particular terminal
 */
abstract class TerminalBusesDataManager(private val activity: Activity)
    : BaseDataManager<List<Bus>>(activity.applicationContext) {

    private val inflight: ArrayList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns list of [Bus]es
     */
    fun loadTerminalBuses(terminalKey: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.BUS_REF)
                .whereEqualTo("terminalKey", terminalKey)
        inflight.add(query)
        val buses: MutableList<Bus> = ArrayList(0)
        query.addSnapshotListener(activity, { querySnapshot, exception ->
            if (exception != null) {
                endProcess(query, buses)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {

                for (doc in querySnapshot.documents) {
                    if (doc.exists()) {
                        val bus = doc.toObject(Bus::class.java)
                        buses.add(bus)
                    }
                }

                endProcess(query, buses)
            } else endProcess(query, buses)
        })
    }

    private fun endProcess(query: Query, buses: MutableList<Bus>) {
        loadFinished()
        onDataLoaded(buses)
        inflight.remove(query)
    }

}