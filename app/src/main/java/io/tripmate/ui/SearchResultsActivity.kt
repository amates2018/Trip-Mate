package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.peanutsdk.recyclerview.GridItemDividerDecoration
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.tripmate.R
import io.tripmate.data.Trip
import io.tripmate.util.SearchableItem

/**
 * Search results page
 */
class SearchResultsActivity : Activity() {
    private val grid: RecyclerView by bindView(R.id.search_results_grid)
    private val container: ViewGroup by bindView(R.id.container)
    private val backWrapper: LinearLayout by bindView(R.id.back_wrapper)

    private lateinit var adapter: TripsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        //Setup toolbar
        val backToolbar = backWrapper.findViewById<ImageButton>(R.id.toolbar_back)
        val menuToolbar = backWrapper.findViewById<ImageButton>(R.id.toolbar_menu)
        val titleToolbar = backWrapper.findViewById<TextView>(R.id.toolbar_text)

        backToolbar.setOnClickListener({ onBackPressed() })
        menuToolbar.setOnClickListener({
            Toast.makeText(applicationContext, "Not implemented", Toast.LENGTH_SHORT).show()
        })
        titleToolbar.text = getString(R.string.search_header)

        //Add adapter and setup recyclerview
        adapter = TripsAdapter(this@SearchResultsActivity)
        grid.adapter = adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        grid.layoutManager = layoutManager
        grid.itemAnimator = SlideInItemAnimator()
        grid.addItemDecoration(GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider))
        grid.setHasFixedSize(true)

        //Get intent
        val intent = intent
        if (intent.hasExtra(SEARCH_DATA)) {
            val items = intent.extras.getParcelableArrayList<SearchableItem>(SEARCH_DATA)
            bindItems(items)
        }
    }

    //Bind items to recyclerview
    private fun bindItems(items: ArrayList<SearchableItem>?) {
        val trips = ArrayList<Trip>(0)
        if (items != null && items.isNotEmpty()) {
            for (trip in items) {
                if (trip is Trip) {
                    //Add only trips
                    trips.add(trip)
                }
            }

            adapter.addAndResort(items)
            checkEmptyState()
        }
    }

    private fun checkEmptyState() {
        if (adapter.dataItemCount() > 0) {
            //Data has been loaded successfully
        } else {
            Snackbar.make(container, "No data found", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    companion object {
        const val SEARCH_DATA = "SEARCH_DATA"
    }
}
