package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import io.peanutsdk.recyclerview.Divided
import io.peanutsdk.recyclerview.GridItemDividerDecoration
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.CircularImageView
import io.tripmate.R
import io.tripmate.api.ReservationsDataManager
import io.tripmate.data.Reservation
import io.tripmate.util.GlideApp
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils

/**
 * Reservations for user trips
 */
class ReservationsActivity : Activity() {
    //Main
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val toolbar: ViewGroup by bindView(R.id.back_wrapper)

    //RecyclerView
    private val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_reservations)
    private val grid: RecyclerView by bindView(R.id.grid_reservations)
    private val empty: TextView by bindView(R.id.no_reservations)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var dataManager: ReservationsDataManager
    private lateinit var adapter: ReservationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)


        //Init prefs
        prefs = TripMatePrefs[this@ReservationsActivity]
        loading = TripMateUtils.getDialog(applicationContext)

        //Toolbar setup
        val back = toolbar.findViewById<ImageButton>(R.id.toolbar_back)
        val title = toolbar.findViewById<TextView>(R.id.toolbar_text)
        val menu = toolbar.findViewById<ImageButton>(R.id.toolbar_menu)
        back.setOnClickListener({ onBackPressed() })
        menu.visibility = View.GONE
        title.text = getString(R.string.reservations)

        //Setup grid
        dataManager = object : ReservationsDataManager(this@ReservationsActivity) {
            override fun onDataLoaded(data: List<Reservation>) {
                if (loading.isShowing) loading.dismiss()
                if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
                adapter.addAndResort(data)
                checkEmptyState()
            }
        }

        //Kick off initial load
        adapter = ReservationsAdapter()
        grid.adapter = adapter
        grid.layoutManager = LinearLayoutManager(this@ReservationsActivity)
        grid.setHasFixedSize(true)
        grid.itemAnimator = SlideInItemAnimator()
        grid.addItemDecoration(GridItemDividerDecoration(this@ReservationsActivity, R.dimen
                .divider_height, R.color.divider))
        if (prefs.isLoggedIn) dataManager.loadTripReservations(prefs.getAccessToken())
        checkEmptyState()

        swipeRefresh.setOnRefreshListener({
            if (prefs.isLoggedIn) dataManager.loadTripReservations(prefs.getAccessToken())
            checkEmptyState()
        })
    }

    private fun checkEmptyState() {
        if (adapter.itemCount > 0) {
            TransitionManager.beginDelayedTransition(container)
            empty.visibility = View.GONE
            content.visibility = View.VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(container)
            empty.visibility = View.VISIBLE
            content.visibility = View.GONE
        }
    }

    internal inner class ReservationsAdapter : RecyclerView.Adapter<ReservationsViewHolder>() {
        private val reservations: MutableList<Reservation> = ArrayList(0)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationsViewHolder {
            val view: View = LayoutInflater.from(this@ReservationsActivity)
                    .inflate(R.layout.trip_item, parent, false)
            return ReservationsViewHolder(view)
        }

        override fun getItemCount(): Int {
            return reservations.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ReservationsViewHolder, position: Int) {
            //Get trip item per position
            val trip = reservations[position].trip

            //Set destination and origin
            holder.details.text = trip?.origin + " - " + trip?.destination

            //Get bus details
            val bus = trip?.bus
            if (bus == null) {
                //Default details when fields are null
                holder.number.text = "N/A"
                holder.name.text = "N/A"
            } else {
                //Get bus number plate
                holder.number.text = bus.number

                //Get bus type
                holder.name.text = bus.type

                //Load bus image
                GlideApp.with(holder.image.context)
                        .load(bus.image)
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .fallback(R.drawable.avatar_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(holder.image)
            }

            //Set price
            holder.price.text = String.format("GHC %s", trip?.price.toString())

            //Set trip duration
            holder.duration.text = trip?.duration.toString()

            //proceed to buy ticket
            holder.itemView.setOnClickListener({
                val intent = Intent(this@ReservationsActivity, SeatsActivity::class.java)
                intent.putExtra(SeatsActivity.EXTRA_TRIP_DATA, reservations[position])
                startActivity(intent)
            })
        }

        fun clear() = reservations.clear()

        //Add new data from server
        fun addAndResort(newReservations: List<Reservation>) {
            if (newReservations.isEmpty()) return

            var add = true
            for (item in newReservations) {

                for (i in 0 until reservations.size) {
                    if (item == reservations[i]) add = false
                }

                //add new item if it is not already in the list
                if (add) {
                    reservations.add(item)
                    notifyItemRangeChanged(0, newReservations.size)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.clear()
        dataManager.loadTripReservations(prefs.getAccessToken())
    }

    internal inner class ReservationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
        val name: TextView by bindView(R.id.bus_name)
        val details: TextView by bindView(R.id.trip_details)
        val number: TextView by bindView(R.id.bus_number)
        val duration: TextView by bindView(R.id.trip_time_details)
        val price: TextView by bindView(R.id.trip_price)
        val image: CircularImageView by bindView(R.id.bus_photo)
    }
}
