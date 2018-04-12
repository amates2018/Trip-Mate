package io.tripmate.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.peanutsdk.recyclerview.Divided
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.CircularImageView
import io.tripmate.R
import io.tripmate.data.Trip
import io.tripmate.util.GlideApp
import io.tripmate.util.LoginStateListener
import io.tripmate.util.SearchableItem
import java.util.*


/**
 * Trips data adapter
 */
class TripsAdapter(private val context: Activity) : RecyclerView.Adapter<TripsAdapter.TripsViewHolder>(), LoginStateListener {
    private val items: MutableList<Trip>
    private val inflater: LayoutInflater

    init {
        this.items = ArrayList(0)
        this.inflater = LayoutInflater.from(context)
        setHasStableIds(true)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsViewHolder {
        return TripsViewHolder(inflater.inflate(R.layout.trip_item, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TripsViewHolder, position: Int) {
        //Get trip item per position
        val trip = items[position]

        //Set destination and origin
        holder.details.text = trip.origin + " - " + trip.destination

        //Get bus details
        val bus = trip.bus
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
            GlideApp.with(context)
                    .load(bus.image)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .fallback(R.drawable.avatar_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .transition(withCrossFade())
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(holder.image)
        }

        //Set price
        holder.price.text = String.format("GHC %s", trip.price.toString())

        //Set trip duration
        holder.duration.text = trip.duration.toString()

        //Click action for trip details
        holder.itemView.setOnClickListener { v ->
            val intent = Intent(context, TripDetailsActivity::class.java)
            intent.putExtra(TripDetailsActivity.TRIP_DATA, trip)
            val options = ActivityOptions.makeSceneTransitionAnimation(context,
                    Pair(holder.image, "image"))
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return dataItemCount()
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    internal fun dataItemCount(): Int {
        val size = items.size
        return if (items.isEmpty()) 0 else size
    }

    override fun onLogin() {

    }

    override fun onLogout() {

    }


    /**
     * Add new content to the existing list
     * Avoid duplications by running an inner loop to check the existence of the new item in the
     * list of existing ones. If it exists already, it will be ignored else it will be added and
     * the dataset updated respectively
     */
    fun addAndResort(newItems: List<SearchableItem>) {
        if (newItems.isEmpty()) return
        val size = items.size
        var add = true
        for (item in newItems) {
            for (i in 0 until size) {
                if (item.key == items[i].key) {
                    add = false
                }
            }

            if (add) {
                addItem(item)
                notifyItemRangeChanged(0, newItems.size)
            }
        }
    }

    //Add item if it is an instance of Trip
    private fun addItem(item: SearchableItem) {
        if (item is Trip) {
            items.add(item)
        }
    }

    private fun getItem(position: Int): SearchableItem {
        return items[position]
    }

    /**
     * Clear list
     */
    fun clear() {
        if (items.isEmpty()) return
        items.clear()
    }

    class TripsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
        val name: TextView by bindView(R.id.bus_name)
        val details: TextView by bindView(R.id.trip_details)
        val number: TextView by bindView(R.id.bus_number)
        val duration: TextView by bindView(R.id.trip_time_details)
        val price: TextView by bindView(R.id.trip_price)
        val image: CircularImageView by bindView(R.id.bus_photo)
    }
}
