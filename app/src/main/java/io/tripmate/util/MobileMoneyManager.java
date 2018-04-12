package io.tripmate.util;

import android.content.Context;

/**
 * Base class for handling mobile money payment
 */
public class MobileMoneyManager {
	
	private Context context;
	private PaymentMethod method;
	
	//Constructor
	public MobileMoneyManager(Context context, PaymentMethod method) {
		this.context = context;
		this.method = method;
	}
	
	
}
