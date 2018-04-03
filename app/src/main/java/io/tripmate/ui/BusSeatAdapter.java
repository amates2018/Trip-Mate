package io.tripmate.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import io.tripmate.R;
import io.tripmate.util.AbstractItem;
import io.tripmate.util.CenterItem;
import io.tripmate.util.EdgeItem;
import io.tripmate.util.OnSeatSelected;
import io.tripmate.util.SelectableAdapter;

/**
 * Project : trip-mate
 * Created by Dennis Bilson on Mon at 10:16 AM.
 * Package name : io.tripmate.ui
 */
public class BusSeatAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {
	
	private static class EdgeViewHolder extends RecyclerView.ViewHolder {
		
		ImageView imgSeat;
		private final ImageView imgSeatSelected;
		
		
		public EdgeViewHolder(View itemView) {
			super(itemView);
			imgSeat = (ImageView) itemView.findViewById(R.id.img_seat);
			imgSeatSelected = (ImageView) itemView.findViewById(R.id.img_seat_selected);
			
		}
		
	}
	
	private static class CenterViewHolder extends RecyclerView.ViewHolder {
		
		ImageView imgSeat;
		private final ImageView imgSeatSelected;
		
		public CenterViewHolder(View itemView) {
			super(itemView);
			imgSeat = (ImageView) itemView.findViewById(R.id.img_seat);
			imgSeatSelected = (ImageView) itemView.findViewById(R.id.img_seat_selected);
			
			
		}
		
	}
	
	private static class EmptyViewHolder extends RecyclerView.ViewHolder {
		
		public EmptyViewHolder(View itemView) {
			super(itemView);
		}
		
	}
	
	
	private final OnSeatSelected mOnSeatSelected;
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	
	private final List<AbstractItem> mItems;
	
	public BusSeatAdapter(Context context, List<AbstractItem> items) {
		mOnSeatSelected = (OnSeatSelected) context;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mItems = items;
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == AbstractItem.TYPE_CENTER) {
			View itemView = mLayoutInflater.inflate(R.layout.seat_item, parent, false);
			return new CenterViewHolder(itemView);
		} else if (viewType == AbstractItem.TYPE_EDGE) {
			View itemView = mLayoutInflater.inflate(R.layout.seat_item, parent, false);
			return new EdgeViewHolder(itemView);
		} else {
			View itemView = new View(mContext);
			return new EmptyViewHolder(itemView);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
		int type = mItems.get(position).getType();
		if (type == AbstractItem.TYPE_CENTER) {
			CenterItem item = (CenterItem) mItems.get(position);
			CenterViewHolder holder = (CenterViewHolder) viewHolder;
			
			holder.imgSeat.setOnClickListener(v -> {
				toggleSelection(position);
				
				mOnSeatSelected.onSeatSelected(getSelectedItemCount());
			});
			
			holder.imgSeatSelected.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
			
		} else if (type == AbstractItem.TYPE_EDGE) {
			EdgeItem item = (EdgeItem) mItems.get(position);
			EdgeViewHolder holder = (EdgeViewHolder) viewHolder;
			
			
			holder.imgSeat.setOnClickListener(v -> {
				
				toggleSelection(position);
				mOnSeatSelected.onSeatSelected(getSelectedItemCount());
				
				
			});
			
			holder.imgSeatSelected.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
			
		}
	}
	
	@Override
	public int getItemCount() {
		return mItems.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		return mItems.get(position).getType();
	}
	
	public void addSeats(List<AbstractItem> newItems) {
		if (newItems.isEmpty()) return;
		int size = mItems.size();
		boolean add = true;
		for (AbstractItem item : newItems) {
			for (int i = 0; i < size; i++) {
				if (item.equals(mItems.get(i))) add = false;
			}
			
			if (add) {
				mItems.add(item);
				notifyItemRangeChanged(0, newItems.size());
			}
		}
	}
}
