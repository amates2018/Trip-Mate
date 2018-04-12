package io.tripmate.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

/**
 * Trip Mate shared preferences
 */
class TripMatePrefs private constructor(private val context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(TripMateUtils.TRIP_MATE_PREFS, Context.MODE_PRIVATE)
    var auth: FirebaseAuth
    var db: FirebaseFirestore
    var bucket: FirebaseStorage

    private var accessToken: String? = null
    private var userType: String? = null
    private var payment: String? = null
    private var isTracking: Boolean = false
    private var loginStateListeners: MutableList<LoginStateListener>? = null

    var isLoggedIn = false
        private set
    var isConnected = false
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
        payment = prefs.getString(KEY_PAYMENT, null)
        isTracking = prefs.getBoolean(KEY_DRIVER_TRACKING, false)

        isLoggedIn = !accessToken.isNullOrEmpty()
        if (isLoggedIn) {
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
            userType = prefs.getString(KEY_USER_TYPE, null)
            payment = prefs.getString(KEY_PAYMENT, null)
            isTracking = prefs.getBoolean(KEY_DRIVER_TRACKING, false)
        }

        isConnected = getConnectionState()
    }

    private fun getConnectionState(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    /**
     * Set logged in user's access token
     */
    fun setAccessToken(token: String?, userType: UserType) {
        if (token.isNullOrEmpty()) return
        this.accessToken = token
        this.userType = userType.name
        this.payment = token
        isLoggedIn = true
        this.isTracking = false
        //Store locally
        val editor = prefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, this.accessToken)
        editor.putString(KEY_USER_TYPE, this.userType)
        editor.putString(KEY_PAYMENT, payment)
        editor.putBoolean(KEY_DRIVER_TRACKING, isTracking)
        editor.apply()
        dispatchLoginEvent()
    }

    fun getAccessToken(): String {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun enableTracking(hasTracker: Boolean) {
        isTracking = hasTracker
        prefs.edit().putBoolean(KEY_DRIVER_TRACKING, hasTracker).apply()
    }

    fun paymentMethod(): String? {
        return prefs.getString(KEY_PAYMENT, null)
    }

    //Set user's payment method
    fun setPaymentMethod(method: String) {
        this.payment = method
        prefs.edit().putString(KEY_PAYMENT, method).apply()
    }

    //todo: update
    fun getUserType(): UserType {
        return UserType.valueOf(prefs.getString(KEY_USER_TYPE, UserType.TYPE_PASSENGER.type))
    }

    /**
     * Logout any currently logged in user
     */
    fun logout() {
        if (auth.currentUser != null) {
            auth.signOut()

            isLoggedIn = false
            isTracking = false
            accessToken = null
            userType = null
            val editor = prefs.edit()
            editor.putString(KEY_ACCESS_TOKEN, accessToken)
            editor.putString(KEY_USER_TYPE, userType)
            editor.putBoolean(KEY_DRIVER_TRACKING, isTracking)
            editor.apply()
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

    companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_USER_TYPE = "KEY_USER_TYPE"
        private const val KEY_DRIVER_TRACKING = "KEY_DRIVER_TRACKING"
        private const val KEY_PAYMENT = "KEY_PAYMENT"

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