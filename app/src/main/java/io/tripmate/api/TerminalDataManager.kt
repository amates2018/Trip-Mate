package io.tripmate.api

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Terminal
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading a bus [Terminal] from the database
 */
abstract class TerminalDataManager(private val activity: Activity)
    : BaseDataManager<Terminal>(activity.applicationContext) {

    private val inflight: ArrayList<Task<DocumentSnapshot>> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns a bus [Terminal]
     */
    fun loadTerminal(key: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.TERMINAL_REF)
                .document(key)
                .get()
        inflight.add(query)
        query.addOnCompleteListener(activity, { task ->
            if (task.isSuccessful) {
                loadFinished()
                val terminal = task.result.toObject(Terminal::class.java)
                loadFinished()
                onDataLoaded(terminal)
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