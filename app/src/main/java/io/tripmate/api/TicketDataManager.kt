package io.tripmate.api

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Ticket
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Data manager for loading [Ticket] instance from the database
 */
abstract class TicketDataManager(private val activity: Activity)
    : BaseDataManager<Ticket>(activity.applicationContext) {

    private val inflight: ArrayList<Task<DocumentSnapshot>> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns [Ticket]
     */
    fun loadTicket(key: String) {
        loadStarted()
        val query = db.collection(TripMateUtils.TICKET_REF)
                .document(key)
                .get()
        inflight.add(query)
        query.addOnCompleteListener(activity, { task ->
            if (task.isSuccessful) {
                val ticket = task.result.toObject(Ticket::class.java)

                //Check user type and return appropriate result
                loadFinished()
                onDataLoaded(ticket)
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