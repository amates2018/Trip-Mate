package io.tripmate.api

import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Bus
import io.tripmate.data.Terminal
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading [Bus]es for a particular terminal
 */
abstract class BusTerminalDataManager(private val activity: Activity)
    : BaseDataManager<List<Terminal>>(activity.applicationContext) {

    private val inflight: ArrayList<Query> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns list of [Terminal]s
     */
    fun loadTerminals() {
        loadStarted()
        val query = db.collection(TripMateUtils.TERMINAL_REF)
        inflight.add(query)
        val terminals: MutableList<Terminal> = ArrayList(0)
        query.addSnapshotListener(activity, { querySnapshot, exception ->
            if (exception != null) {
                endProcess(query, terminals)
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {

                for (doc in querySnapshot.documents) {
                    if (doc.exists()) {
                        val terminal = doc.toObject(Terminal::class.java)
                        terminals.add(terminal)
                    }
                }

                endProcess(query, terminals)
            } else endProcess(query, terminals)
        })
    }

    private fun endProcess(query: Query, terminals: MutableList<Terminal>) {
        loadFinished()
        onDataLoaded(terminals)
        inflight.remove(query)
    }

}