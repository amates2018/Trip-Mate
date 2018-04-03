package io.tripmate.api;


import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.peanutsdk.recyclerview.BaseDataManager;
import io.tripmate.data.Driver;
import io.tripmate.data.Passenger;
import io.tripmate.util.TripMatePrefs;
import io.tripmate.util.TripMateUtils;
import io.tripmate.util.User;

/**
 * Data manager for loading a list of all [User]s registered on the system
 */
public abstract class AllUserDataManager extends BaseDataManager<List<? extends User>> {
	
	private Activity context;
	private List<Query> inflight;
	private FirebaseFirestore db;
	
	public AllUserDataManager(Activity context) {
		super(context);
		this.context = context;
		this.inflight = new ArrayList<>(0);
		this.db = TripMatePrefs.Companion.get(context).getDb();
	}
	
	@Override
	public void cancelLoading() {
		if (!inflight.isEmpty()) inflight.clear();
	}
	
	public void loadAllUsers() {
		CollectionReference query = db.collection(TripMateUtils.USER_REF);
		inflight.add(query);
		loadStarted();
		List<User> users = new ArrayList<>(0);
		query.get().addOnCompleteListener(context, new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if (task.isSuccessful()) {
					for (DocumentSnapshot doc : task.getResult().getDocuments()) {
						//Check availability of document
						if (doc.exists()) {
							//Extract data from result
							Map<String, Object> data = doc.getData();
							//Check if data contains key: payment
							//This is to verify whether the data returned is
							// of type Passenger or Driver
							if (data.containsKey("payment")) {
								Passenger passenger = doc.toObject(Passenger.class);
								users.add(passenger);
							} else {
								Driver driver = doc.toObject(Driver.class);
								users.add(driver);
							}
							
						}
					}
					
					loadFinished();
					onDataLoaded(users);
					inflight.remove(query);
				} else {
					loadFinished();
					onDataLoaded(users);
					inflight.remove(query);
				}
			}
		}).addOnFailureListener(context, e -> {
			loadFinished();
			onDataLoaded(users);
			inflight.remove(query);
		});
	}
}
