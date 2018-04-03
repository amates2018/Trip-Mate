package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.api.SearchDataManager
import io.tripmate.util.SearchableItem
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User
import java.util.*

/**
 * Search interface for querying trips and tickets
 */
class SearchActivity : Activity(), DatePickerDialog.OnDateSetListener {

    //Widgets
    private val container: ViewGroup by bindView(R.id.container)
    private val backWrapper: LinearLayout by bindView(R.id.back_wrapper)
    private val username: TextView by bindView(R.id.user_data_content)
    private val cardTripDetails: CardView by bindView(R.id.card_trip_details)
    private val cardDateDetails: CardView by bindView(R.id.card_date_content)
    private val dateContent: TextView by bindView(R.id.date_content)
    private val origin: TextInputEditText by bindView(R.id.origin_content)
    private val destination: TextInputEditText by bindView(R.id.destination_content)
    private val search: Button by bindView(R.id.search_button)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var dataManager: SearchDataManager

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //Init prefs
        prefs = TripMatePrefs[this]
        loading = TripMateUtils.getDialog(this)

        //Setup toolbar
        val backToolbar = backWrapper.findViewById<ImageButton>(R.id.toolbar_back)
        val menuToolbar = backWrapper.findViewById<ImageButton>(R.id.toolbar_menu)
        val titleToolbar = backWrapper.findViewById<TextView>(R.id.toolbar_text)

        backToolbar.setOnClickListener({ onBackPressed() })
        menuToolbar.setOnClickListener({
            Toast.makeText(applicationContext, "Not implemented", Toast.LENGTH_SHORT).show()
        })
        titleToolbar.text = getString(R.string.search_header)
        val calendar = Calendar.getInstance()
        dateContent.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)}/${calendar.get(Calendar.YEAR)}"

        //Setup data manager
        dataManager = object : SearchDataManager(this@SearchActivity) {
            override fun onDataLoaded(data: List<SearchableItem>) {
                //data loaded
                if (loading.isShowing) loading.dismiss()
                if (data.isEmpty()) {
                    Snackbar.make(container, "No results found", Snackbar.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this@SearchActivity, SearchResultsActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(SearchResultsActivity.SEARCH_DATA, data as ArrayList<out Parcelable>)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        }

        if (loading.isCancelled) {
            dataManager.clear()
        }

        //Get intent data, if any
        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            val user = intent.getParcelableExtra<User>(EXTRA_USER)
            username.text = String.format(getString(R.string.welcome_user), user.username)
        }

    }

    override fun onDestroy() {
        dataManager.cancelLoading()
        super.onDestroy()
    }

    fun searchBus(v: View) {
        val originText = origin.text.toString()
        val destinationText = destination.text.toString()

        when {
            originText.isEmpty() -> {
                showMessage("Set your origin")
                origin.requestFocus()
            }

            destinationText.isEmpty() -> {
                showMessage("Set your origin")
                destination.requestFocus()
            }

            else -> {
                loading.show()
                dataManager.searchFor(originText, destinationText)
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(container, "Oops! $message", Snackbar.LENGTH_SHORT).show()
    }

    fun pickDate(v: View) {
        val fragment = DatePickerFragment()
        fragment.show(fragmentManager, DatePickerFragment.TAG)
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        //Set date for bus trip from data received: dd/mm/yyyy
        dateContent.text = "$dayOfMonth/$month/$year"
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
    }
}
