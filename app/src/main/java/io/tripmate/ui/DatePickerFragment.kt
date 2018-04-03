package io.tripmate.ui


import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import io.tripmate.R
import java.util.*


/**
 * A simple [DialogFragment] subclass for picking date
 * All activities calling this fragment must implement [DatePickerDialog.OnDateSetListener]
 */
class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Create calendar instance
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        //Create dialog
        val dialog = DatePickerDialog(activity, activity as DatePickerDialog.OnDateSetListener, year, month.plus(1), day)

        //Set title
        dialog.setTitle(getString(R.string.date_dialog_header))

        //Get date picker
        val datePicker = dialog.datePicker

        //Set minimum date to current date selection
        datePicker.minDate = calendar.timeInMillis

        //Add 6 Days to current date
        calendar.add(Calendar.DAY_OF_MONTH, 6)

        //Set max date
        datePicker.maxDate = calendar.timeInMillis

        //Return dark theme for the dialog
        return dialog
    }

    companion object {
        val TAG: String = DatePickerFragment::class.java.simpleName
    }
}
