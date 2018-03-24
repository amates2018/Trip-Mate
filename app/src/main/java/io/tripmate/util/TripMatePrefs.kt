package io.tripmate.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import io.tripmate.R

/**
 * Trip Mate shared preferences
 */
class TripMatePrefs private constructor(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(TripMateUtils.TRIP_MATE_PREFS, Context.MODE_PRIVATE)
    var auth: FirebaseAuth
    var db: FirebaseFirestore
    var bucket: FirebaseStorage

    private var accessToken: String? = null
    private var userType: String? = null
    private var loading: MaterialDialog
    private var loginStateListeners: MutableList<LoginStateListener>? = null

    var isLoggedIn = false
        private set

    init {
        //Init Firebase
        val firebaseApp = FirebaseApp.initializeApp(context)
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        if (firebaseApp == null) {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            bucket = FirebaseStorage.getInstance()
        } else {
            auth = FirebaseAuth.getInstance(firebaseApp)
            db = FirebaseFirestore.getInstance(firebaseApp)
            bucket = FirebaseStorage.getInstance(firebaseApp)
        }

        //Add offline settings to database references
        db.firestoreSettings = firestoreSettings

        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)

        isLoggedIn = !accessToken.isNullOrEmpty()
        if (isLoggedIn) {
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        }

        //Setup Material Dialog
        loading = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .progress(true, 0)
                .content(context.getString(R.string.loading))
                .canceledOnTouchOutside(false)
                .build()
    }

    /**
     * Set logged in user's access token
     */
    fun setAccessToken(token: String?) {
        if (token.isNullOrEmpty()) return
        accessToken = token
        isLoggedIn = true
        //Store locally
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
        dispatchLoginEvent()
    }

    fun getAccessToken(): String? {
        if (isLoggedIn) {
            return prefs.getString(KEY_ACCESS_TOKEN, null)
        }
        return null
    }

    /**
     * Logout any currently logged in user
     */
    fun logout() {
        if (auth.currentUser != null) {
            auth.signOut()

            isLoggedIn = false
            accessToken = null
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
            dispatchLogoutEvent()
        }
    }

    /**
     * Attach a listener for user's login state
     */
    fun addLoginListener(listener: LoginStateListener) {
        if (loginStateListeners == null) {
            loginStateListeners = ArrayList(0)
        }
        loginStateListeners!!.add(listener)
    }

    /**
     * Remove listeners for user's login state
     */
    fun removeLoginListener(listener: LoginStateListener) {
        if (loginStateListeners == null) return
        loginStateListeners!!.remove(listener)
    }

    private fun dispatchLoginEvent() {
        if (loginStateListeners != null && loginStateListeners!!.isNotEmpty()) {
            for (listener in loginStateListeners!!) {
                listener.onLogin()
            }
        }
    }

    private fun dispatchLogoutEvent() {
        if (loginStateListeners != null && loginStateListeners!!.isNotEmpty()) {
            for (listener in loginStateListeners!!) {
                listener.onLogout()
            }
        }
    }

    /**
     * Returns the Material Dialog
     */
    fun getDialog(): MaterialDialog {
        return loading
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: TripMatePrefs? = null

        operator fun get(context: Context): TripMatePrefs {
            if (singleton == null) {
                synchronized(TripMatePrefs::class.java) {
                    singleton = TripMatePrefs(context)
                }
            }
            return singleton!!
        }
    }
}