package io.tripmate.api

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.peanutsdk.recyclerview.BaseDataManager
import io.tripmate.data.Driver
import io.tripmate.data.Passenger
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User

/**
 * Data manager for loading [User] instance from the database
 */
abstract class UserDataManager(private val activity: Activity)
    : BaseDataManager<User>(activity.applicationContext) {

    private val inflight: ArrayList<Task<DocumentSnapshot>> = ArrayList(0)
    private val db: FirebaseFirestore = TripMatePrefs[activity].db

    override fun cancelLoading() {
        if (inflight.size > 0) {
            inflight.clear()
        }
    }

    /**
     * Returns user data model as [Passenger] or [Driver]
     */
    fun loadCurrentUser(key: String) {
        loadStarted()
        if (key.isNotEmpty()) {
            val query = db.collection(TripMateUtils.USER_REF)
                    .document(key)
                    .get()
            inflight.add(query)
            query.addOnCompleteListener(activity, { task ->
                if (task.isSuccessful) {
                    val doc = task.result
                    if (doc.exists()) {
                        val data = doc.data
                        //Check user type and return appropriate result
                        if (data.containsKey("payment")) {
                            val passenger = doc.toObject(Passenger::class.java)
                            onDataLoaded(passenger)
                        } else {
                            val driver = doc.toObject(Driver::class.java)
                            onDataLoaded(driver)
                        }

                        endProcess(query)
                    }
                } else {
                    endProcess(query)
                }
            }).addOnFailureListener(activity, { _ ->
                endProcess(query)
            })
        }
    }

    private fun endProcess(query: Task<DocumentSnapshot>) {
        loadFinished()
        inflight.remove(query)
    }

}