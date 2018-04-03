package io.tripmate.util

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import io.tripmate.R

/**
 * Utils for constants and static variables
 */
object TripMateUtils {
    //Database references
    const val BUS_REF = "bus"
    const val TRIP_REF = "trip"
    const val TICKET_REF = "ticket"
    const val USER_REF = "user"
    const val SEAT_REF = "seat"
    const val TERMINAL_REF = "terminal"

    //Shared preferences
    const val TRIP_MATE_PREFS = "TRIP_MATE_PREFS"

    fun getDialog(context: Context): MaterialDialog {
        return MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .progress(true, 0)
                .content(context.getString(R.string.loading))
                .canceledOnTouchOutside(false)
                .build()
    }
}