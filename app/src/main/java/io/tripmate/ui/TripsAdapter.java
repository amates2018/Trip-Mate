package io.tripmate.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.peanutsdk.recyclerview.Divided;
import io.peanutsdk.widget.CircularImageView;
import io.tripmate.R;
import io.tripmate.data.Bus;
import io.tripmate.data.Trip;
import io.tripmate.util.GlideApp;
import io.tripmate.util.LoginStateListener;
import io.tripmate.util.SearchableItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsViewHolder> implements
		LoginStateListener {
	
	private final Activity context;
	private final List<Trip> items;
	private final LayoutInflater inflater;
	
	public TripsAdapter(Activity context) {
		this.context = context;
		this.items = new ArrayList<>(0);
		this.inflater = LayoutInflater.from(context);
		setHasStableIds(true);
	}
	
	
	@NonNull
	@Override
	public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TripsViewHolder(inflater.inflate(R.layout.trip_item, parent, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
		//Get trip item per position
		Trip trip = items.get(position);
		
		//Set destination and origin
		holder.details.setText(String.format("%s - %s", trip.getOrigin(), trip.getDestination()));
		
		//Get bus details
		Bus bus = trip.getBus();
		if (bus == null) {
			//Default details when fields are null
			holder.number.setText("N/A");
			holder.name.setText("N/A");
		} else {
			//Get bus number plate
			holder.number.setText(bus.getNumber());
			
			//Get bus type
			holder.name.setText(bus.getType());
			
			//Load bus image
			GlideApp.with(context)
					.load(bus.getImage())
					.placeholder(R.drawable.avatar_placeholder)
					.error(R.drawable.avatar_placeholder)
					.fallback(R.drawable.avatar_placeholder)
					.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
					.transition(withCrossFade())
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.into(holder.image);
		}
		
		//Set price
		holder.price.setText(String.format("GHC %s", String.valueOf(trip.getPrice())));
		
		//Set trip duration
		holder.duration.setText(String.valueOf(trip.getDuration()));
		
		//Click action for trip details
		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(context, TripDetailsActivity.class);
			intent.putExtra(TripDetailsActivity.TRIP_DATA, trip);
			ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
					new Pair<View, String>(holder.image, "image"));
			context.startActivity(intent, options.toBundle());
		});
	}
	
	@Override
	public int getItemCount() {
		return dataItemCount();
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}
	
	int dataItemCount() {
		int size = items.size();
		if (items.isEmpty()) return 0;
		return size;
	}
	
	@Override
	public void onLogin() {
	
	}
	
	@Override
	public void onLogout() {
	
	}
	
	
	/**
	 * Add new content to the existing list
	 * Avoid duplications by running an inner loop to check the existence of the new item in the
	 * list of existing ones. If it exists already, it will be ignored else it will be added and
	 * the dataset updated respectively
	 */
	public void addAndResort(List<? extends SearchableItem> newItems) {
		if (newItems.isEmpty()) return;
		int size = items.size();
		boolean add = true;
		for (SearchableItem item : newItems) {
			for (int i = 0; i < size; i++) {
				if (Objects.equals(item.getKey(), items.get(i).getKey())) {
					add = false;
				}
			}
			
			if (add) {
				addItem(item);
				notifyItemRangeChanged(0, newItems.size());
			}
		}
	}
	
	//Add item if it is an instance of Trip
	private void addItem(SearchableItem item) {
		if (item instanceof Trip) {
			items.add((Trip) item);
		}
	}
	
	private SearchableItem getItem(int position) {
		return items.get(position);
	}
	
	/**
	 * Clear list
	 */
	public void clear() {
		if (items.isEmpty()) return;
		items.clear();
	}
	
	public static class TripsViewHolder extends RecyclerView.ViewHolder implements Divided {
		@BindView(R.id.bus_name)
		TextView name;
		@BindView(R.id.trip_details)
		TextView details;
		@BindView(R.id.bus_number)
		TextView number;
		@BindView(R.id.trip_time_details)
		TextView duration;
		@BindView(R.id.trip_price)
		TextView price;
		@BindView(R.id.bus_photo)
		CircularImageView image;
		
		
		public TripsViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
