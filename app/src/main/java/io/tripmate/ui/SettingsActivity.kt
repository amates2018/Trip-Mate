package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.data.Driver
import io.tripmate.data.Passenger
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User

/**
 * Current user's settings
 */
class SettingsActivity : Activity() {
    //Toolbar
    private val toolbar: ViewGroup by bindView(R.id.back_wrapper)

    //Main
    private val container: ViewGroup by bindView(R.id.container)

    //ViewGroups
    private val driverContainer: ViewGroup by bindView(R.id.settings_driver)
    private val passengerContainer: ViewGroup by bindView(R.id.settings_passenger)

    //Visibility
    private val vHeader: TextView by bindView(R.id.v_header)
    private val vContent: TextView by bindView(R.id.v_content)
    private val vToggle: Switch by bindView(R.id.v_toggle)

    //Bus number
    private val nHeader: TextView by bindView(R.id.n_header)
    private val nContent: TextView by bindView(R.id.n_content)

    //Seats
    private val sHeader: TextView by bindView(R.id.s_header)
    private val sContent: TextView by bindView(R.id.s_content)

    //Prefs
    private lateinit var prefs: TripMatePrefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = TripMatePrefs[this@SettingsActivity]

        //Setup toolbar
        val menu = toolbar.findViewById<ImageButton>(R.id.toolbar_menu)
        val back = toolbar.findViewById<ImageButton>(R.id.toolbar_back)
        val title = toolbar.findViewById<TextView>(R.id.toolbar_text)
        menu.visibility = View.GONE
        title.text = getString(R.string.action_settings)
        back.setOnClickListener({ onBackPressed() })

        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            val user = intent.getParcelableExtra<User>(EXTRA_USER)
            bindUser(user)
        }

    }

    private fun bindUser(user: User?) {
        if (user == null) {
            Snackbar.make(container, "Sorry! User cannot be found", Snackbar.LENGTH_INDEFINITE).show()
            return
        }

        if (user is Driver) {
            showDriverUI()

            //Set toggle action
            vHeader.setOnClickListener({ vToggle.toggle() })
            vContent.setOnClickListener({ vToggle.toggle() })
            vToggle.setOnCheckedChangeListener({ _, isChecked ->
                if (prefs.isLoggedIn) {
                    prefs.enableTracking(isChecked)

                    val map = hashMapOf<String, Any?>(Pair("isTracking", isChecked))
                    updateUser(map)
                }
            })

            //Bus number
            nHeader.setOnClickListener({ v -> showOptionDialog(v) })
            nContent.setOnClickListener({ v -> showOptionDialog(v) })

            //Seats
            sHeader.setOnClickListener({ v -> showOptionDialog(v) })
            sContent.setOnClickListener({ v -> showOptionDialog(v) })

        } else if (user is Passenger) {
            //todo: do in update
            hideDriverUI()
        }

    }

    private fun showOptionDialog(v: View) {
        val customView: View = layoutInflater.inflate(R.layout.custom_edit_text, null, false)
        val edtContent = customView.findViewById<EditText>(R.id.edit_text)
        val content = edtContent.text.toString()

        MaterialDialog.Builder(this@SettingsActivity)
                .theme(Theme.LIGHT)
                .customView(customView, true)
                .positiveText("Ok")
                .negativeText("Cancel")
                .onNegative({ dialog, _ ->
                    dialog.dismiss()
                })
                .onPositive({ dialog, _ ->
                    dialog.dismiss()
                    when (v.id) {
                        R.id.n_content, R.id.n_header -> {
                            nContent.text = content
                        }
                        R.id.s_content, R.id.s_header -> {
                            sContent.text = content
                        }
                        else -> return@onPositive
                    }
                })
                .build().show()
    }

    //Update user data
    private fun updateUser(map: HashMap<String, Any?>) {
        prefs.db.collection(TripMateUtils.USER_REF).document(prefs.getAccessToken())
                .update(map)
                .addOnCompleteListener(this@SettingsActivity, { task ->
                    if (task.isSuccessful) {
                        Log.d("SettingsActivity", "User update state : $map")
                    } else {
                        task.exception?.printStackTrace()
                    }
                })
    }

    //Hides the driver settings interface
    private fun hideDriverUI() {
        TransitionManager.beginDelayedTransition(container)
        driverContainer.visibility = View.GONE
        passengerContainer.visibility = View.VISIBLE
    }

    //Shows the driver settings interface
    private fun showDriverUI() {
        TransitionManager.beginDelayedTransition(container)
        driverContainer.visibility = View.GONE
        passengerContainer.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
    }
}
